package com.speed.sofasogood.game;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.util.Log;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Looper;

import com.speed.sofasogood.R;

public class GameView extends View {

    // For debugging
    public static final String KEYCODE = "KeyCode";
    public static final String GYRO = "GyroEvent";
    public static final String MIC = "MicEvent";

    // Ground layer codes
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int DRAIN = 4;
    public static final int BROKE_PIPE = 5;
    public static final int TARGET_PLANT = 20;
    public static final int TARGET_TV = 22;
    public static final int TARGET_SOFA_L = 23;
    public static final int TARGET_SOFA_R = 24;
    public static final int TARGET_TUB_L = 25;
    public static final int TARGET_TUB_R = 26;

    // Object layer codes
    public static final int NONE = 0;
    public static final int PLAYER = 2;
    public static final int CAT = 7;
    public static final int BOX_WATER = 3;
    public static final int BOX_PLANT = 10;
    public static final int BOX_TV = 12;
    public static final int BOX_SOFA_L = 13;
    public static final int BOX_SOFA_R = 14;
    public static final int BOX_TUB_L = 15;
    public static final int BOX_TUB_R = 16;

    private int[][] ground;  // never changes after load
    private int[][] objects; // player + boxes, moves
    private int[][] underObjects; // objects that can exist under player (water)
    private int playerRow, playerCol;
    private int tileSize;
    private int offsetX, offsetY;

    private Bitmap bmpWall, bmpFloor, bmpPlayer;
    private Bitmap bmpCatIdle, bmpCatSleep;
    private Bitmap bmpPlant, bmpSofaL, bmpSofaR, bmpTv, bmpTubL, bmpTubR, bmpWater;
    private boolean catAwake = false;
    // Microphone monitoring
    private AudioRecord audioRecord;
    private Thread micThread;
    private volatile boolean micMonitoring = false;
    private float micThresholdDb = 50f; // default medium sensitivity
    private long micLastTriggerMs = 0;
    private static final int MIC_COOLDOWN_MS = 1000;
    public static final int MIC_REQUEST_CODE = 1234;
    private Bitmap bmpDrain;
    private Bitmap bmpDrainGhost;
    private Bitmap bmpBrokePipe;
    private Bitmap bmpPlantGhost, bmpSofaLGhost, bmpSofaRGhost, bmpTvGhost, bmpTubLGhost, bmpTubRGhost;
    // Cache decoded source bitmaps to avoid repeated decoding
    private static final android.util.SparseArray<Bitmap> srcCache = new android.util.SparseArray<>();

    private float touchStartX, touchStartY;
    private OnLevelCompleteListener completeListener;
    private SoundPool soundPool;
    private int moveSoundId;
    private float soundVolume = 1.0f;
    private int moveCount = 0;

    private float[][] dropProgress;
    private boolean animating = false;
    private java.util.List<ValueAnimator> runningAnimators = new java.util.ArrayList<>();
    private static final long TOTAL_DROP_TIME = 3000; // all tiles must land within 3s
    private static final long DROP_DURATION = 400;

    // Slide animation
    private static final long SLIDE_DURATION = 120;
    private boolean sliding = false;
    private final java.util.Map<Long, float[]> slideOffsets = new java.util.HashMap<>();
    private Runnable onSlideEnd;

    // Gyro (gravity) control
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private SensorEventListener gravityListener;
    private Handler gyroHandler;
    private Runnable gyroRunnable;
    private long gyroIntervalMs = 500; // user requested 500ms
    private float gyroThreshold = 3.5f; // default (medium) sensitivity
    private int gyroLastDr = 0, gyroLastDc = 0;
    private boolean gyroActive = false;
    // Pause control
    private boolean userPaused = false;
    // Broke-pipe spawner (creates water periodically)
    private Handler brokepipeHandler;
    private Runnable brokepipeRunnable;
    private long brokepipeIntervalMs = 3000; // 3 seconds
    private boolean brokepipeActive = false;

    public interface OnLevelCompleteListener {
        void onLevelComplete();
    }

    public interface OnDropCompleteListener {
        void onDropComplete();
    }

    private OnDropCompleteListener dropCompleteListener;

    public void setOnDropCompleteListener(OnDropCompleteListener listener) {
        this.dropCompleteListener = listener;
    }

    public void setSoundPool(SoundPool sp, int soundId) {
        this.soundPool = sp;
        this.moveSoundId = soundId;
    }

    public int getMoveCount() { return moveCount; }

    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
    }

    public void setOnLevelCompleteListener(OnLevelCompleteListener listener) {
        this.completeListener = listener;
    }

    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    // For Debugging
    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }

    /**
     * Load level from a single combined map.
     * Codes: 0=floor, 1=wall, 2=player, 3=water, 4=drain, 10=plant, 12=tv, 13=sofaL, 14=sofaR, 15=tubL, 16=tubR
     *        20=plant target, 22=tv target, 23=sofaL target, 24=sofaR target, 25=tubL target, 26=tubR target
     */
    public void loadLevel(int[][] level) {
        // reset spawner for new level
        stopBrokePipeSpawner();
        cancelAnimations();
        int rows = level.length;
        int cols = level[0].length;
        ground = new int[rows][cols];
        objects = new int[rows][cols];
        underObjects = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int v = level[r][c];
                if (v == WALL) {
                    ground[r][c] = WALL;
                    objects[r][c] = NONE;
                } else if (v == PLAYER) {
                    ground[r][c] = FLOOR;
                    objects[r][c] = PLAYER;
                    playerRow = r;
                    playerCol = c;
                } else if (v == CAT) {
                    ground[r][c] = FLOOR;
                    objects[r][c] = CAT;
                } else if (v == BOX_PLANT || v == BOX_TV || v == BOX_SOFA_L || v == BOX_SOFA_R || v == BOX_TUB_L || v == BOX_TUB_R || v == BOX_WATER) {
                    ground[r][c] = FLOOR;
                    if (v == BOX_WATER) {
                        objects[r][c] = NONE;
                        underObjects[r][c] = BOX_WATER;
                    } else {
                        objects[r][c] = v;
                    }
                } else if (v == DRAIN) {
                    ground[r][c] = DRAIN;
                    objects[r][c] = NONE;
                } else if (v == BROKE_PIPE) {
                    ground[r][c] = BROKE_PIPE;
                    objects[r][c] = NONE;
                } else if (v == TARGET_PLANT || v == TARGET_TV || v == TARGET_SOFA_L || v == TARGET_SOFA_R || v == TARGET_TUB_L || v == TARGET_TUB_R) {
                    ground[r][c] = v;
                    objects[r][c] = NONE;
                } else {
                    ground[r][c] = FLOOR;
                    objects[r][c] = NONE;
                }
            }
        }

        dropProgress = new float[rows][cols];
        animating = true;
        moveCount = 0;
        if (tileSize > 0) startDropAnimation();
        // Auto-enable gyro control if level contains water
        updateGyroState();
        startBrokePipeSpawner();
        // Start microphone monitoring for loud sound trigger
        startMicMonitoring();
        invalidate();
    }

    private void cancelAnimations() {
        for (ValueAnimator a : runningAnimators) a.cancel();
        runningAnimators.clear();
        animating = false;
    }

    private void startDropAnimation() {
        if (ground == null) return;
        cancelAnimations();
        int rows = ground.length, cols = ground[0].length;

        int totalTiles = rows * cols;
        long tileDelay = Math.max(1, (TOTAL_DROP_TIME - DROP_DURATION) / Math.max(totalTiles - 1, 1));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int fr = r, fc = c;
                ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                anim.setDuration(DROP_DURATION);
                anim.setStartDelay((long)(r * cols + c) * tileDelay);
                anim.setInterpolator(new BounceInterpolator());
                anim.addUpdateListener(a -> {
                    if (dropProgress != null && fr < dropProgress.length && fc < dropProgress[fr].length) {
                        dropProgress[fr][fc] = (float) a.getAnimatedValue();
                        invalidate();
                    }
                });
                if (r == rows - 1 && c == cols - 1) {
                    anim.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            animating = false;
                            runningAnimators.clear();
                            invalidate();
                            if (dropCompleteListener != null) dropCompleteListener.onDropComplete();
                        }
                    });
                }
                runningAnimators.add(anim);
                anim.start();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (ground == null) return;
        tileSize = Math.min(w / ground[0].length, h / ground.length);
        offsetX = (w - ground[0].length * tileSize) / 2;
        offsetY = (h - ground.length * tileSize) / 2;
        recycleBitmaps();
        loadBitmaps();
        startDropAnimation();
    }

    private void loadBitmaps() {
        bmpWall = scale(R.drawable.asset_wall);
        bmpFloor = scale(R.drawable.asset_floor);
        bmpPlayer = scale(R.drawable.asset_player);
        bmpCatIdle = scale(R.drawable.asset_cat_idle);
        bmpCatSleep = scale(R.drawable.asset_cat_sleep);
        bmpPlant = scale(R.drawable.asset_plant);
        bmpSofaL = scale(R.drawable.asset_sofa_left);
        bmpSofaR = scale(R.drawable.asset_sofa_right);
        bmpTv = scale(R.drawable.asset_tv);
        bmpTubL = scale(R.drawable.asset_tub_left);
        bmpTubR = scale(R.drawable.asset_tub_right);
        bmpWater = scale(R.drawable.asset_water);
        bmpDrain = scale(R.drawable.asset_drain);
        bmpBrokePipe = scale(R.drawable.asset_brokepipe);
        bmpPlantGhost = makeGhost(bmpPlant);
        bmpDrainGhost = makeGhost(bmpDrain);
        bmpSofaLGhost = makeGhost(bmpSofaL);
        bmpSofaRGhost = makeGhost(bmpSofaR);
        bmpTvGhost = makeGhost(bmpTv);
        bmpTubLGhost = makeGhost(bmpTubL);
        bmpTubRGhost = makeGhost(bmpTubR);
    }

    private void recycleBitmaps() {
        Bitmap[] all = { bmpWall, bmpFloor, bmpPlayer, bmpPlant, bmpSofaL, bmpSofaR, bmpTv, bmpTubL, bmpTubR, bmpWater,
            bmpPlantGhost, bmpSofaLGhost, bmpSofaRGhost, bmpTvGhost, bmpTubLGhost, bmpTubRGhost, bmpDrain, bmpDrainGhost, bmpBrokePipe, bmpCatIdle, bmpCatSleep };
        for (Bitmap b : all) {
            if (b != null && !b.isRecycled()) b.recycle();
        }
    }

    private Bitmap scale(int resId) {
        // Check for custom skin first
        String key = resIdToKey(resId);
        if (key != null) {
            android.graphics.Bitmap custom = AssetSkinManager.loadCustomBitmap(getContext(), key);
            if (custom != null) {
                return android.graphics.Bitmap.createScaledBitmap(custom, tileSize, tileSize, true);
            }
        }
        Bitmap src = srcCache.get(resId);
        if (src == null || src.isRecycled()) {
            src = BitmapFactory.decodeResource(getResources(), resId);
            srcCache.put(resId, src);
        }
        return Bitmap.createScaledBitmap(src, tileSize, tileSize, true);
    }

    private static String resIdToKey(int resId) {
        if (resId == R.drawable.asset_wall) return "asset_wall";
        if (resId == R.drawable.asset_floor) return "asset_floor";
        if (resId == R.drawable.asset_player) return "asset_player";
        if (resId == R.drawable.asset_plant) return "asset_plant";
        if (resId == R.drawable.asset_sofa_left) return "asset_sofa_left";
        if (resId == R.drawable.asset_sofa_right) return "asset_sofa_right";
        if (resId == R.drawable.asset_tub_left) return "asset_tub_left";
        if (resId == R.drawable.asset_tub_right) return "asset_tub_right";
        if (resId == R.drawable.asset_water) return "asset_water";
        if (resId == R.drawable.asset_drain) return "asset_drain";
        if (resId == R.drawable.asset_brokepipe) return "asset_brokepipe";
        if (resId == R.drawable.asset_tv) return "asset_tv";
        if (resId == R.drawable.asset_cat_idle) return "asset_cat_idle";
        if (resId == R.drawable.asset_cat_sleep) return "asset_cat_sleep";
        return null;
    }

    private Bitmap makeGhost(Bitmap src) {
        Bitmap ghost = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(ghost);
        Paint p = new Paint();
        p.setAlpha(80);
        c.drawBitmap(src, 0, 0, p);
        return ghost;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ground == null) return;

        int totalH = ground.length * tileSize;

        // Pass 1: ground + ghost
        for (int r = 0; r < ground.length; r++) {
            for (int c = 0; c < ground[r].length; c++) {
                int x = offsetX + c * tileSize;
                int targetY = offsetY + r * tileSize;
                int y;
                if (animating || (dropProgress != null && dropProgress[r][c] < 1f)) {
                    float progress = dropProgress != null ? dropProgress[r][c] : 1f;
                    y = (int) ((offsetY - totalH) + (targetY - (offsetY - totalH)) * progress);
                } else {
                    y = targetY;
                }

                int g = ground[r][c];
                if (g == WALL) {
                    canvas.drawBitmap(bmpWall, x, y, null);
                    continue;
                }
                // Draw floor for all non-wall ground by default
                if (g == BROKE_PIPE) {
                    canvas.drawBitmap(bmpBrokePipe, x, y, null);
                } else {
                    canvas.drawBitmap(bmpFloor, x, y, null);
                }

                Bitmap ghost = getGhostForGround(g);
                if (ghost != null) canvas.drawBitmap(ghost, x, y, null);
            }
        }

        // Pass 2: objects (on top of all ground)
        for (int r = 0; r < ground.length; r++) {
            for (int c = 0; c < ground[r].length; c++) {
                // Draw under-objects first (water)
                if (underObjects != null && underObjects[r][c] == BOX_WATER) {
                    int x = offsetX + c * tileSize;
                    int targetY = offsetY + r * tileSize;
                    int y;
                    if (animating || (dropProgress != null && dropProgress[r][c] < 1f)) {
                        float progress = dropProgress != null ? dropProgress[r][c] : 1f;
                        y = (int) ((offsetY - totalH) + (targetY - (offsetY - totalH)) * progress);
                    } else {
                        y = targetY;
                    }
                    float ox = 0, oy = 0;
                    float[] off = slideOffsets.get(cellKey(r, c));
                    if (off != null) { ox = off[0]; oy = off[1]; }
                    canvas.drawBitmap(bmpWater, x + ox, y + oy, null);
                }

                int o = objects[r][c];
                Bitmap obj = getObjectBitmap(o);
                if (obj == null) continue;

                int x = offsetX + c * tileSize;
                int targetY = offsetY + r * tileSize;
                int y;
                if (animating || (dropProgress != null && dropProgress[r][c] < 1f)) {
                    float progress = dropProgress != null ? dropProgress[r][c] : 1f;
                    y = (int) ((offsetY - totalH) + (targetY - (offsetY - totalH)) * progress);
                } else {
                    y = targetY;
                }

                float ox = 0, oy = 0;
                float[] off = slideOffsets.get(cellKey(r, c));
                if (off != null) { ox = off[0]; oy = off[1]; }
                canvas.drawBitmap(obj, x + ox, y + oy, null);
            }
        }
    }

    private Bitmap getGhostForGround(int g) {
        switch (g) {
            case TARGET_PLANT: return bmpPlantGhost;
            case TARGET_TV: return bmpTvGhost;
            case TARGET_SOFA_L: return bmpSofaLGhost;
            case TARGET_SOFA_R: return bmpSofaRGhost;
            case TARGET_TUB_L: return bmpTubLGhost;
            case TARGET_TUB_R: return bmpTubRGhost;
            case DRAIN: return bmpDrainGhost;
        }
        return null;
    }

    private Bitmap getObjectBitmap(int o) {
        switch (o) {
            case PLAYER: return bmpPlayer;
            case CAT: return catAwake ? bmpCatIdle : bmpCatSleep;
            case BOX_PLANT: return bmpPlant;
            case BOX_TV: return bmpTv;
            case BOX_WATER: return bmpWater;
            case BOX_SOFA_L: return bmpSofaL;
            case BOX_SOFA_R: return bmpSofaR;
            case BOX_TUB_L: return bmpTubL;
            case BOX_TUB_R: return bmpTubR;
        }
        return null;
    }

    private boolean isBox(int o) {
        // Treat water separately — not pushable by the player. Only moveWater() changes water.
        return o == BOX_PLANT || o == BOX_TV || o == BOX_SOFA_L || o == BOX_SOFA_R || o == BOX_TUB_L || o == BOX_TUB_R;
    }

    private boolean isSofaPart(int o) {
        return o == BOX_SOFA_L || o == BOX_SOFA_R;
    }

    private boolean isTarget(int g) {
        return g == TARGET_PLANT || g == TARGET_TV || g == TARGET_SOFA_L || g == TARGET_SOFA_R || g == TARGET_TUB_L || g == TARGET_TUB_R;
    }

    private int[] findSofaPair(int r, int c) {
        int o = objects[r][c];
        if (o == BOX_SOFA_L && inBounds(r, c + 1) && objects[r][c + 1] == BOX_SOFA_R)
            return new int[]{r, c + 1};
        if (o == BOX_SOFA_R && inBounds(r, c - 1) && objects[r][c - 1] == BOX_SOFA_L)
            return new int[]{r, c - 1};
        return null;
    }

    // Generic paired-part support (sofa, tub, etc.)
    private boolean isPairedPart(int o) {
        return o == BOX_SOFA_L || o == BOX_SOFA_R || o == BOX_TUB_L || o == BOX_TUB_R;
    }

    private int getPairedPartner(int o) {
        if (o == BOX_SOFA_L) return BOX_SOFA_R;
        if (o == BOX_SOFA_R) return BOX_SOFA_L;
        if (o == BOX_TUB_L) return BOX_TUB_R;
        if (o == BOX_TUB_R) return BOX_TUB_L;
        return -1;
    }

    private int[] findPairedPair(int r, int c) {
        int o = objects[r][c];
        int partner = getPairedPartner(o);
        if (partner == -1) return null;
        if (inBounds(r, c + 1) && objects[r][c + 1] == partner) return new int[]{r, c + 1};
        if (inBounds(r, c - 1) && objects[r][c - 1] == partner) return new int[]{r, c - 1};
        return null;
    }

    private boolean pushPaired(int pr, int pc, int dr, int dc) {
        int[] pair = findPairedPair(pr, pc);
        if (pair == null) return false;
        int pR = pair[0], pC = pair[1];

        if (dc != 0) {
            // Horizontal push
            int leadR, leadC, trailR, trailC;
            if (dc > 0) {
                if (pc > pC) { leadR = pr; leadC = pc; trailR = pR; trailC = pC; }
                else { leadR = pR; leadC = pC; trailR = pr; trailC = pc; }
            } else {
                if (pc < pC) { leadR = pr; leadC = pc; trailR = pR; trailC = pC; }
                else { leadR = pR; leadC = pC; trailR = pr; trailC = pc; }
            }
            int destR = leadR + dr, destC = leadC + dc;
            if (!inBounds(destR, destC) || ground[destR][destC] == WALL || objects[destR][destC] != NONE)
                return false;

            int leadObj = objects[leadR][leadC];
            int trailObj = objects[trailR][trailC];
            objects[destR][destC] = leadObj;
            objects[leadR][leadC] = trailObj;
            objects[trailR][trailC] = NONE;
        } else {
            // Vertical push
            int dSR = pr + dr, dSC = pc;
            int dPR = pR + dr, dPC = pC;
            if (!inBounds(dSR, dSC) || !inBounds(dPR, dPC)) return false;
            if (ground[dSR][dSC] == WALL || ground[dPR][dPC] == WALL) return false;
            if (objects[dSR][dSC] != NONE || objects[dPR][dPC] != NONE) return false;

            objects[dSR][dSC] = objects[pr][pc];
            objects[dPR][dPC] = objects[pR][pC];
            objects[pr][pc] = NONE;
            objects[pR][pC] = NONE;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animating || sliding) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                float min = tileSize * 0.5f;
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > min)
                    move(0, dx > 0 ? 1 : -1);
                else if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > min)
                    move(dy > 0 ? 1 : -1, 0);
                return true;
        }
        return super.onTouchEvent(event);
    }

    // For debugging
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        requestFocusFromTouch();
        post(() -> requestFocus());
    }

    // For debugging
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(KEYCODE, "Key pressed: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                moveWater(-1, 0);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                moveWater(1, 0);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                moveWater(0, -1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                moveWater(0, 1);
                return true;
            case KeyEvent.KEYCODE_SPACE:
                runCatsAway();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long cellKey(int r, int c) { return ((long) r << 16) | (c & 0xFFFFL); }

    private void move(int dr, int dc) {
        if (sliding) return;
        int nr = playerRow + dr, nc = playerCol + dc;
        if (!inBounds(nr, nc) || ground[nr][nc] == WALL) return;

        int nextObj = objects[nr][nc];
        boolean pushed = false;
        java.util.List<long[]> movedCells = new java.util.ArrayList<>();

        boolean handled = false;
        if (isPairedPart(nextObj)) {
            int[] pair = findPairedPair(nr, nc);
            if (pair == null) return;
            if (!pushPaired(nr, nc, dr, dc)) return;
            pushed = true;
            // paired cells that moved
            movedCells.add(new long[]{cellKey(nr + dr, nc + dc), dr, dc});
            movedCells.add(new long[]{cellKey(pair[0] + dr, pair[1] + dc), dr, dc});
            if (dc != 0) {
                // horizontal: one cell stays in place visually (trail becomes lead's old pos)
                // just animate all destination cells
            }
        } else if (isBox(nextObj)) {
            int br = nr + dr, bc = nc + dc;
            // Normal box push: block if destination invalid or occupied.
            if (!inBounds(br, bc) || ground[br][bc] == WALL || objects[br][bc] != NONE) return;
            objects[br][bc] = nextObj;
            objects[nr][nc] = NONE;
            pushed = true;
            movedCells.add(new long[]{cellKey(br, bc), dr, dc});
        } else if (underObjects != null && underObjects[nr][nc] == BOX_WATER) {
            // Player steps into water: attempt to slide extra steps equal to consecutive water count.
            int chain = 0;
            int r = nr, c = nc;
            while (inBounds(r, c) && underObjects[r][c] == BOX_WATER) {
                chain++;
                r += dr; c += dc;
            }
            // final destination after walking through waters
            int finalR = playerRow + dr * (1 + chain);
            int finalC = playerCol + dc * (1 + chain);
            // Check final destination validity: in-bounds, not wall, and empty (no object). Do NOT push.
            if (!inBounds(finalR, finalC) || ground[finalR][finalC] == WALL || objects[finalR][finalC] != NONE) {
                // blocked -> cancel entire move (player stays)
                return;
            }
            // Also ensure intermediate non-water cells are not blocking (shouldn't be, but check safety)
            boolean blocked = false;
            int checkR = playerRow + dr, checkC = playerCol + dc;
            for (int i = 0; i < chain; i++) {
                if (!inBounds(checkR, checkC) || underObjects[checkR][checkC] != BOX_WATER) { blocked = true; break; }
                checkR += dr; checkC += dc;
            }
            if (blocked) return;

            // perform movement: clear old player, set new player position
            objects[playerRow][playerCol] = NONE;
            objects[finalR][finalC] = PLAYER;
            playerRow = finalR; playerCol = finalC;
            moveCount++;
            movedCells.add(new long[]{cellKey(finalR, finalC), dr, dc});
            pushed = true; // treat as moved for sound
            handled = true;
        } else if (nextObj != NONE) {
            return;
        }

        if (!handled) {
            int oldR = playerRow, oldC = playerCol;
            objects[playerRow][playerCol] = NONE;
            objects[nr][nc] = PLAYER;
            playerRow = nr;
            playerCol = nc;
            moveCount++;
            movedCells.add(new long[]{cellKey(nr, nc), dr, dc});
        }

        // Set cat image to idle for this player move (will revert after animations)
        catAwake = true;
        invalidate();

        // Possibly move cats after player's move (every 2 player moves)
        java.util.List<long[]> catMoves = maybeMoveCats();
        for (long[] mc : catMoves) movedCells.add(mc);

        if (pushed && soundPool != null) soundPool.play(moveSoundId, soundVolume * 0.5f, soundVolume * 0.5f, 1, 0, 1f);

        // Setup slide offsets (start from -1 tile in move direction, animate to 0)
        slideOffsets.clear();
        for (long[] mc : movedCells) {
            slideOffsets.put(mc[0], new float[]{-mc[2] * tileSize, -mc[1] * tileSize});
        }
        sliding = true;
        invalidate();

        ValueAnimator anim = ValueAnimator.ofFloat(1f, 0f);
        anim.setDuration(SLIDE_DURATION);
        anim.addUpdateListener(a -> {
            float f = (float) a.getAnimatedValue();
            for (long[] mc : movedCells) {
                float[] off = slideOffsets.get(mc[0]);
                if (off != null) {
                    off[0] = -mc[2] * tileSize * f;
                    off[1] = -mc[1] * tileSize * f;
                }
            }
            invalidate();
        });
        final boolean catsMoved = catMoves.size() > 0;
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                slideOffsets.clear();
                sliding = false;
                // After animation, set cat image based on move parity: awake if odd, sleep if even
                catAwake = (moveCount % 2 == 1);
                invalidate();
                checkWin();
            }
        });
        anim.start();
    }

    /**
     * Debug helper: move all water boxes in the given direction.
     * Waters only disappear when they move into a drain (ground==DRAIN).
     */
    public void moveWater(int dr, int dc) {
        if (sliding) return;
        if (ground == null || underObjects == null) return;
        java.util.List<long[]> movedCells = new java.util.ArrayList<>();

        int rows = ground.length, cols = ground[0].length;
        int[][] origObjects = new int[rows][cols];
        int[][] origUnder = new int[rows][cols];
        int[][] result = new int[rows][cols];
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) { origObjects[r][c] = objects[r][c]; origUnder[r][c] = underObjects[r][c]; result[r][c] = NONE; }

        java.util.List<int[]> waters = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) if (origUnder[r][c] == BOX_WATER) waters.add(new int[]{r, c});

        java.util.Map<Long, Long> intent = new java.util.HashMap<>(); // src -> dest
        java.util.Map<Long, java.util.List<Long>> reverse = new java.util.HashMap<>(); // dest -> list(src)

        // Build intents. If destination is wall or out-of-bounds, water cannot move (remains).
        // If destination is a drain (DRAIN) we still create an intent so water can move and disappear.
        for (int[] w : waters) {
            int sr = w[0], sc = w[1];
            int tr = sr + dr, tc = sc + dc;
            long srcKey = cellKey(sr, sc);
            if (!inBounds(tr, tc) || ground[tr][tc] == WALL) {
                // cannot move -> remain in place
                continue;
            }
            // If destination has an object (boxes/player), this water cannot move
            if (origObjects[tr][tc] != NONE) {
                // will remain in place
                continue;
            }
            long destKey = cellKey(tr, tc);
            intent.put(srcKey, destKey);
            reverse.computeIfAbsent(destKey, k -> new java.util.ArrayList<>()).add(srcKey);
        }

        // Kahn-like processing: nodes whose destination is not occupied by water are ready
        java.util.ArrayDeque<Long> q = new java.util.ArrayDeque<>();
        java.util.Map<Long, Integer> indeg = new java.util.HashMap<>();

        for (java.util.Map.Entry<Long, Long> e : intent.entrySet()) {
            Long src = e.getKey();
            Long dest = e.getValue();
            int dR = (int) (dest >> 16);
            int dC = (int) (dest & 0xFFFFL);
            // if destination currently does NOT contain water, it's ready
            if (origUnder[dR][dC] != BOX_WATER) {
                indeg.put(src, 0);
                q.add(src);
            } else {
                // destination contains water: this src depends on the source at that cell
                long occupant = cellKey(dR, dC);
                indeg.put(src, indeg.getOrDefault(src, 0) + 1);
                // ensure reverse mapping (preds) already built above
            }
        }

        java.util.Set<Long> allowed = new java.util.HashSet<>();

        while (!q.isEmpty()) {
            Long s = q.poll();
            allowed.add(s);
            // when s moves, it vacates its cell; any src targeting s's cell may decrement indeg
            java.util.List<Long> preds = reverse.get(s);
            if (preds == null) continue;
            for (Long p : preds) {
                int v = indeg.getOrDefault(p, 0) - 1;
                indeg.put(p, v);
                if (v == 0) q.add(p);
            }
        }

        // Handle cycles: remaining nodes in intent with indeg>0 form cycles. Allow cycles if every dest is targeted by exactly one source and none of their destinations are blocked by objects/walls
        java.util.Set<Long> remaining = new java.util.HashSet<>();
        for (Long s : intent.keySet()) if (!allowed.contains(s)) remaining.add(s);

        // Group remaining into components and attempt to allow whole cycles
        while (!remaining.isEmpty()) {
            Long start = remaining.iterator().next();
            // follow cycle/component
            java.util.Set<Long> comp = new java.util.HashSet<>();
            java.util.ArrayDeque<Long> stack = new java.util.ArrayDeque<>();
            stack.add(start);
            while (!stack.isEmpty()) {
                Long cur = stack.pop();
                if (!remaining.contains(cur)) continue;
                remaining.remove(cur);
                comp.add(cur);
                Long dest = intent.get(cur);
                if (dest != null) {
                    // if dest is occupied by a source node, add that source
                    int dR = (int) (dest >> 16);
                    int dC = (int) (dest & 0xFFFFL);
                    long occKey = cellKey(dR, dC);
                    if (intent.containsKey(occKey) && remaining.contains(occKey)) stack.add(occKey);
                }
            }

            boolean ok = true;
            for (Long s : comp) {
                Long dest = intent.get(s);
                if (dest == null) { ok = false; break; }
                int dR = (int) (dest >> 16);
                int dC = (int) (dest & 0xFFFFL);
                if (!inBounds(dR, dC) || ground[dR][dC] == WALL) { ok = false; break; }
                // destination must be targeted by exactly one source AND that source must be inside this component
                java.util.List<Long> preds = reverse.get(dest);
                if (preds == null || preds.size() != 1) { ok = false; break; }
                long occKey = cellKey(dR, dC);
                if (!intent.containsKey(occKey) || !comp.contains(occKey)) { ok = false; break; }
                // and destination not blocked by object
                if (origObjects[dR][dC] != NONE) { ok = false; break; }
            }
            if (ok) {
                for (Long s : comp) allowed.add(s);
            }
        }

        // Apply moves
        for (int[] w : waters) {
            long srcKey = cellKey(w[0], w[1]);
            if (allowed.contains(srcKey)) {
                long destKey = intent.get(srcKey);
                int destR = (int) (destKey >> 16);
                int destC = (int) (destKey & 0xFFFFL);
                if (ground[destR][destC] == DRAIN) {
                    // water moves into drain and disappears (do not place in result)
                    movedCells.add(new long[]{cellKey(destR, destC), dr, dc});
                } else {
                    result[destR][destC] = BOX_WATER;
                    movedCells.add(new long[]{cellKey(destR, destC), dr, dc});
                }
            } else {
                // remain
                result[w[0]][w[1]] = BOX_WATER;
            }
        }

        // Commit to underObjects
        underObjects = result;

        // Auto start/stop gyro depending on water existence
        updateGyroState();

        // Animate moved water cells
        slideOffsets.clear();
        for (long[] mc : movedCells) {
            slideOffsets.put(mc[0], new float[]{-mc[2] * tileSize, -mc[1] * tileSize});
        }
        if (movedCells.size() > 0) {
            sliding = true;
            invalidate();

            ValueAnimator anim = ValueAnimator.ofFloat(1f, 0f);
            anim.setDuration(SLIDE_DURATION);
            anim.addUpdateListener(a -> {
                float f = (float) a.getAnimatedValue();
                for (long[] mc : movedCells) {
                    float[] off = slideOffsets.get(mc[0]);
                    if (off != null) {
                        off[0] = -mc[2] * tileSize * f;
                        off[1] = -mc[1] * tileSize * f;
                    }
                }
                invalidate();
            });
            anim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    slideOffsets.clear();
                    sliding = false;
                    invalidate();
                }
            });
            anim.start();
        } else {
            invalidate();
        }
    }

    // Check whether any water exists in underObjects
    private boolean hasAnyWater() {
        if (underObjects == null) return false;
        for (int r = 0; r < underObjects.length; r++) {
            for (int c = 0; c < underObjects[r].length; c++) {
                if (underObjects[r][c] == BOX_WATER) return true;
            }
        }
        return false;
    }

    // Move cats: every 2 player moves, each cat randomly moves 1 step (if possible).
    private java.util.List<long[]> maybeMoveCats() {
        java.util.List<long[]> res = new java.util.ArrayList<>();
        if (ground == null || objects == null) return res;
        // Only move cats on every 2nd player move
        if (moveCount % 2 != 0) return res;
        java.util.Random rnd = new java.util.Random();
        int rows = ground.length, cols = ground[0].length;
        java.util.List<int[]> moves = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (objects[r][c] == CAT) {
                    java.util.List<int[]> opts = new java.util.ArrayList<>();
                    int[][] dirs = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
                    for (int[] d : dirs) {
                        int tr = r + d[0], tc = c + d[1];
                        if (!inBounds(tr, tc)) continue;
                        if (ground[tr][tc] == WALL) continue;
                        if (objects[tr][tc] != NONE) continue;
                        opts.add(new int[]{tr, tc, d[0], d[1]});
                    }
                    if (!opts.isEmpty()) {
                        int[] pick = opts.get(rnd.nextInt(opts.size()));
                        moves.add(new int[]{r, c, pick[0], pick[1], pick[2], pick[3]});
                    }
                }
            }
        }
        for (int[] m : moves) {
            int sr = m[0], sc = m[1], tr = m[2], tc = m[3], dr = m[4], dc = m[5];
            if (objects[tr][tc] == NONE) {
                objects[tr][tc] = CAT;
                objects[sr][sc] = NONE;
                res.add(new long[]{cellKey(tr, tc), dr, dc});
            }
        }
        return res;
    }

    // When space pressed: nearest cat(s) around player run away along a straight line to the farthest reachable empty cell.
    private void runCatsAway() {
        if (sliding) return;
        if (ground == null || objects == null) return;

        // show awake image while cats are about to run
        catAwake = true;
        invalidate();

        // find nearest distance
        int rows = ground.length, cols = ground[0].length;
        int bestDist = Integer.MAX_VALUE;
        java.util.List<int[]> candidates = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (objects[r][c] == CAT) {
                    int dist = Math.abs(r - playerRow) + Math.abs(c - playerCol);
                    if (dist < bestDist) {
                        bestDist = dist;
                        candidates.clear();
                        candidates.add(new int[]{r, c});
                    } else if (dist == bestDist) {
                        candidates.add(new int[]{r, c});
                    }
                }
            }
        }
        if (candidates.isEmpty()) return;

        java.util.List<long[]> movedCats = new java.util.ArrayList<>();

        for (int[] cat : candidates) {
            int sr = cat[0], sc = cat[1];
            int dR = sr - playerRow;
            int dC = sc - playerCol;
            int dr = 0, dc = 0;
            if (Math.abs(dR) >= Math.abs(dC)) {
                dr = Integer.signum(dR);
                if (dr == 0) dc = Integer.signum(dC == 0 ? 1 : dC);
            } else {
                dc = Integer.signum(dC);
            }
            if (dr == 0 && dc == 0) {
                // fallback: choose up
                dr = -1;
            }

            // Step along direction until blocked; record farthest valid cell
            int tr = sr, tc = sc;
            int lastR = sr, lastC = sc;
            while (true) {
                int nr = tr + dr, nc = tc + dc;
                if (!inBounds(nr, nc) || ground[nr][nc] == WALL) break;
                if (objects[nr][nc] != NONE) break; // blocked by object (player/box/cat)
                lastR = nr; lastC = nc;
                tr = nr; tc = nc;
            }
            // If cannot move at least one cell, skip
            if (lastR == sr && lastC == sc) continue;

            // Move cat
            objects[lastR][lastC] = CAT;
            objects[sr][sc] = NONE;
            int stepsR = lastR - sr;
            int stepsC = lastC - sc;
            // For animation, provide total dr,dc distance
            movedCats.add(new long[]{cellKey(lastR, lastC), stepsR, stepsC});
            //Log.d(KEYCODE, "Cat ran from (" + sr + "," + sc + ") to (" + lastR + "," + lastC + ")");
        }

        if (movedCats.isEmpty()) {
            invalidate();
            return;
        }

        slideOffsets.clear();
        for (long[] mc : movedCats) {
            slideOffsets.put(mc[0], new float[]{-mc[2] * tileSize, -mc[1] * tileSize});
        }
        sliding = true;
        invalidate();

        ValueAnimator anim = ValueAnimator.ofFloat(1f, 0f);
        anim.setDuration(SLIDE_DURATION);
        anim.addUpdateListener(a -> {
            float f = (float) a.getAnimatedValue();
            for (long[] mc : movedCats) {
                float[] off = slideOffsets.get(mc[0]);
                if (off != null) {
                    off[0] = -mc[2] * tileSize * f;
                    off[1] = -mc[1] * tileSize * f;
                }
            }
            invalidate();
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                slideOffsets.clear();
                sliding = false;
                // restore cat image based on player move parity
                catAwake = (moveCount % 2 == 1);
                invalidate();
            }
        });
        anim.start();
    }

    /**
     * Start listening to gravity sensor and repeatedly call moveWater() while tilt is held.
     * Uses Sensor.TYPE_GRAVITY (falls back to ACCELEROMETER).
     */
    public void startGyroControl() {
        if (gyroActive) return;
        Log.d(GYRO,"startGyroControl called");
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.d(GYRO,"sensorManager null");
            return;
        }
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        boolean usingGravity = true;
        if (gravitySensor == null) {
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            usingGravity = false;
        }
        if (gravitySensor == null) {
            Log.d(GYRO,"no sensor found");
            return;
        }
        final String sensorMsg = usingGravity ? "Gravity sensor enabled" : "Using accelerometer for tilt";
        Log.d(GYRO, usingGravity ? "Using TYPE_GRAVITY" : "Using ACCELEROMETER");

        gyroHandler = new Handler(Looper.getMainLooper());
        gravityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float gx = event.values[0];
                float gy = event.values[1];
                int dr = 0, dc = 0;
                if (Math.abs(gx) > Math.abs(gy)) {
                    if (gx > gyroThreshold) dc = -1;
                    else if (gx < -gyroThreshold) dc = 1;
                } else {
                    if (gy > gyroThreshold) dr = 1;
                    else if (gy < -gyroThreshold) dr = -1;
                }
                gyroLastDr = dr; gyroLastDc = dc;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };

        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        gyroRunnable = new Runnable() {
            @Override
            public void run() {
                // If tilt present, and there is water, attempt moves until water disappears
                if ((gyroLastDr != 0 || gyroLastDc != 0) && hasAnyWater()) {
                    moveWater(gyroLastDr, gyroLastDc);
                    if (gyroHandler != null && gyroActive) {
                        gyroHandler.postDelayed(this, gyroIntervalMs);
                    }
                } else {
                    // keep polling to detect new tilt; if no water, stop
                    if (hasAnyWater()) {
                        if (gyroHandler != null && gyroActive) {
                            gyroHandler.postDelayed(this, gyroIntervalMs);
                        }
                    } else {
                        stopGyroControl();
                    }
                }
            }
        };

        gyroHandler.postDelayed(gyroRunnable, gyroIntervalMs);
        gyroActive = true;
    }

    public void stopGyroControl() {
        gyroActive = false;
        if (sensorManager != null && gravityListener != null) sensorManager.unregisterListener(gravityListener);
        gravityListener = null;
        if (gyroHandler != null && gyroRunnable != null) gyroHandler.removeCallbacks(gyroRunnable);
        gyroRunnable = null;
        gyroHandler = null;
        Log.d(GYRO,"stopGyroControl called");
    }

    private void startMicMonitoring() {
        if (micMonitoring) return;
        Context ctx = getContext();
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ctx instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST_CODE);
            }
            Log.d(MIC, "Microphone permission not granted; skipping mic monitoring");
            return;
        }

        final int sampleRate = 8000;
        final int bufSize = Math.max(AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT), sampleRate);
        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufSize);
            audioRecord.startRecording();
        } catch (Exception e) {
            Log.d(MIC, "Failed to start AudioRecord: " + e.getMessage());
            return;
        }

        micMonitoring = true;
        micThread = new Thread(() -> {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            short[] buffer = new short[bufSize];
            while (micMonitoring && audioRecord != null) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    double sum = 0;
                    for (int i = 0; i < read; i++) {
                        sum += buffer[i] * (double) buffer[i];
                    }
                    double rms = Math.sqrt(sum / read);
                    double amp = rms / 32768.0;
                    double db = amp > 0 ? 20.0 * Math.log10(amp) + 90.0 : 0.0; // offset so ambient values are > 0
                    if (db >= micThresholdDb) {
                        long now = System.currentTimeMillis();
                        if (now - micLastTriggerMs > MIC_COOLDOWN_MS) {
                            micLastTriggerMs = now;
                            Log.d(MIC, "Mic loud detected: " + db + " dB, triggering cats");
                            post(() -> runCatsAway());
                        }
                    }
                }
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }, "MicMonitor");
        micThread.start();
    }

    private void stopMicMonitoring() {
        micMonitoring = false;
        if (micThread != null) {
            try { micThread.join(200); } catch (InterruptedException ignored) {}
            micThread = null;
        }
        if (audioRecord != null) {
            try { audioRecord.stop(); } catch (Exception ignored) {}
            try { audioRecord.release(); } catch (Exception ignored) {}
            audioRecord = null;
        }
    }

    // Called by hosting Activity when permission result is available
    public void onMicPermissionResult(boolean granted) {
        if (granted) {
            startMicMonitoring();
        } else {
            Log.d(MIC, "Microphone permission denied by user");
        }
    }

    // Start brokepipe spawner: every brokepipeIntervalMs try to create water at each BROKE_PIPE cell
    public void startBrokePipeSpawner() {
        if (brokepipeActive) return;
        if (ground == null) return;
        // ensure there's at least one brokepipe
        boolean found = false;
        for (int r = 0; r < ground.length && !found; r++) for (int c = 0; c < ground[r].length; c++) if (ground[r][c] == BROKE_PIPE) { found = true; break; }
        if (!found) return;

        brokepipeHandler = new Handler(Looper.getMainLooper());
        brokepipeActive = true;
        brokepipeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!brokepipeActive || ground == null || underObjects == null || objects == null) return;
                java.util.List<long[]> newWaterCells = new java.util.ArrayList<>();
                for (int r = 0; r < ground.length; r++) {
                    for (int c = 0; c < ground[r].length; c++) {
                        if (ground[r][c] != BROKE_PIPE) continue;
                        // Only create water if cell not blocked: no object and no existing water
                        if (objects[r][c] == NONE && underObjects[r][c] != BOX_WATER) {
                            underObjects[r][c] = BOX_WATER;
                            updateGyroState();
                            newWaterCells.add(new long[]{cellKey(r, c), 0, 0});
                        }
                    }
                }
                if (newWaterCells.size() > 0) {
                    // trigger visuals
                    slideOffsets.clear();
                    for (long[] mc : newWaterCells) slideOffsets.put(mc[0], new float[]{0f, 0f});
                    invalidate();
                }
                // schedule next tick
                if (brokepipeHandler != null && brokepipeActive) brokepipeHandler.postDelayed(this, brokepipeIntervalMs);
            }
        };
        brokepipeHandler.postDelayed(brokepipeRunnable, brokepipeIntervalMs);
    }

    public void stopBrokePipeSpawner() {
        brokepipeActive = false;
        if (brokepipeHandler != null && brokepipeRunnable != null) brokepipeHandler.removeCallbacks(brokepipeRunnable);
        brokepipeRunnable = null;
        brokepipeHandler = null;
        stopMicMonitoring();
    }

    // Start or stop gyro depending on whether any water exists
    private void updateGyroState() {
        if (hasAnyWater()) startGyroControl();
        else stopGyroControl();
    }

    private boolean pushSofa(int sofaR, int sofaC, int dr, int dc) {
        int[] pair = findSofaPair(sofaR, sofaC);
        if (pair == null) return false;
        int pR = pair[0], pC = pair[1];

        if (dc != 0) {
            // Horizontal push
            int leadR, leadC, trailR, trailC;
            if (dc > 0) {
                if (sofaC > pC) { leadR = sofaR; leadC = sofaC; trailR = pR; trailC = pC; }
                else { leadR = pR; leadC = pC; trailR = sofaR; trailC = sofaC; }
            } else {
                if (sofaC < pC) { leadR = sofaR; leadC = sofaC; trailR = pR; trailC = pC; }
                else { leadR = pR; leadC = pC; trailR = sofaR; trailC = sofaC; }
            }
            int destR = leadR + dr, destC = leadC + dc;
            if (!inBounds(destR, destC) || ground[destR][destC] == WALL || objects[destR][destC] != NONE)
                return false;

            int leadObj = objects[leadR][leadC];
            int trailObj = objects[trailR][trailC];
            objects[destR][destC] = leadObj;
            objects[leadR][leadC] = trailObj;
            objects[trailR][trailC] = NONE;
        } else {
            // Vertical push
            int dSR = sofaR + dr, dSC = sofaC;
            int dPR = pR + dr, dPC = pC;
            if (!inBounds(dSR, dSC) || !inBounds(dPR, dPC)) return false;
            if (ground[dSR][dSC] == WALL || ground[dPR][dPC] == WALL) return false;
            if (objects[dSR][dSC] != NONE || objects[dPR][dPC] != NONE) return false;

            objects[dSR][dSC] = objects[sofaR][sofaC];
            objects[dPR][dPC] = objects[pR][pC];
            objects[sofaR][sofaC] = NONE;
            objects[pR][pC] = NONE;
        }
        return true;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ground.length && c >= 0 && c < ground[0].length;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGyroControl();
        stopBrokePipeSpawner();
        cancelAnimations();
        recycleBitmaps();
    }

    private void checkWin() {
        for (int r = 0; r < ground.length; r++) {
            for (int c = 0; c < ground[r].length; c++) {
                int g = ground[r][c];
                int o = objects[r][c];
                if (g == TARGET_PLANT && o != BOX_PLANT) return;
                if (g == TARGET_TV && o != BOX_TV) return;
                if (g == TARGET_SOFA_L && o != BOX_SOFA_L) return;
                if (g == TARGET_SOFA_R && o != BOX_SOFA_R) return;
                if (g == TARGET_TUB_L && o != BOX_TUB_L) return;
                if (g == TARGET_TUB_R && o != BOX_TUB_R) return;
            }
        }
        // Stop brokepipe spawner and gyro when level finished
        stopGyroControl();
        stopBrokePipeSpawner();
        if (completeListener != null) completeListener.onLevelComplete();
    }
}
