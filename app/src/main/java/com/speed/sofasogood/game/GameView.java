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
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.speed.sofasogood.R;

public class GameView extends View {

    // Ground layer codes
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int TARGET_PLANT = 20;
    public static final int TARGET_TV = 22;
    public static final int TARGET_SOFA_L = 23;
    public static final int TARGET_SOFA_R = 24;

    // Object layer codes
    public static final int NONE = 0;
    public static final int PLAYER = 2;
    public static final int BOX_PLANT = 10;
    public static final int BOX_TV = 12;
    public static final int BOX_SOFA_L = 13;
    public static final int BOX_SOFA_R = 14;

    private int[][] ground;  // never changes after load
    private int[][] objects; // player + boxes, moves
    private int playerRow, playerCol;
    private int tileSize;
    private int offsetX, offsetY;

    private Bitmap bmpWall, bmpFloor, bmpPlayer;
    private Bitmap bmpPlant, bmpSofaL, bmpSofaR, bmpTv;
    private Bitmap bmpPlantGhost, bmpSofaLGhost, bmpSofaRGhost, bmpTvGhost;

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

    public GameView(Context context) { super(context); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); }

    /**
     * Load level from a single combined map.
     * Codes: 0=floor, 1=wall, 2=player, 10=plant, 12=tv, 13=sofaL, 14=sofaR,
     *        20=plant target, 22=tv target, 23=sofaL target, 24=sofaR target
     */
    public void loadLevel(int[][] level) {
        cancelAnimations();
        int rows = level.length;
        int cols = level[0].length;
        ground = new int[rows][cols];
        objects = new int[rows][cols];

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
                } else if (v == BOX_PLANT || v == BOX_TV || v == BOX_SOFA_L || v == BOX_SOFA_R) {
                    ground[r][c] = FLOOR;
                    objects[r][c] = v;
                } else if (v == TARGET_PLANT || v == TARGET_TV || v == TARGET_SOFA_L || v == TARGET_SOFA_R) {
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

        bmpWall = scale(R.drawable.asset_wall);
        bmpFloor = scale(R.drawable.asset_floor);
        bmpPlayer = scale(R.drawable.asset_player);
        bmpPlant = scale(R.drawable.asset_plant);
        bmpSofaL = scale(R.drawable.asset_sofa_left);
        bmpSofaR = scale(R.drawable.asset_sofa_right);
        bmpTv = scale(R.drawable.asset_tv);

        bmpPlantGhost = makeGhost(bmpPlant);
        bmpSofaLGhost = makeGhost(bmpSofaL);
        bmpSofaRGhost = makeGhost(bmpSofaR);
        bmpTvGhost = makeGhost(bmpTv);

        startDropAnimation();
    }

    private Bitmap scale(int resId) {
        return Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), resId), tileSize, tileSize, true);
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
                canvas.drawBitmap(bmpFloor, x, y, null);

                Bitmap ghost = getGhostForGround(g);
                if (ghost != null) canvas.drawBitmap(ghost, x, y, null);
            }
        }

        // Pass 2: objects (on top of all ground)
        for (int r = 0; r < ground.length; r++) {
            for (int c = 0; c < ground[r].length; c++) {
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
        }
        return null;
    }

    private Bitmap getObjectBitmap(int o) {
        switch (o) {
            case PLAYER: return bmpPlayer;
            case BOX_PLANT: return bmpPlant;
            case BOX_TV: return bmpTv;
            case BOX_SOFA_L: return bmpSofaL;
            case BOX_SOFA_R: return bmpSofaR;
        }
        return null;
    }

    private boolean isBox(int o) {
        return o == BOX_PLANT || o == BOX_TV || o == BOX_SOFA_L || o == BOX_SOFA_R;
    }

    private boolean isSofaPart(int o) {
        return o == BOX_SOFA_L || o == BOX_SOFA_R;
    }

    private boolean isTarget(int g) {
        return g == TARGET_PLANT || g == TARGET_TV || g == TARGET_SOFA_L || g == TARGET_SOFA_R;
    }

    private int[] findSofaPair(int r, int c) {
        int o = objects[r][c];
        if (o == BOX_SOFA_L && inBounds(r, c + 1) && objects[r][c + 1] == BOX_SOFA_R)
            return new int[]{r, c + 1};
        if (o == BOX_SOFA_R && inBounds(r, c - 1) && objects[r][c - 1] == BOX_SOFA_L)
            return new int[]{r, c - 1};
        return null;
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

    private long cellKey(int r, int c) { return ((long) r << 16) | (c & 0xFFFFL); }

    private void move(int dr, int dc) {
        if (sliding) return;
        int nr = playerRow + dr, nc = playerCol + dc;
        if (!inBounds(nr, nc) || ground[nr][nc] == WALL) return;

        int nextObj = objects[nr][nc];
        boolean pushed = false;
        java.util.List<long[]> movedCells = new java.util.ArrayList<>();

        if (isSofaPart(nextObj)) {
            int[] pair = findSofaPair(nr, nc);
            if (pair == null) return;
            if (!pushSofa(nr, nc, dr, dc)) return;
            pushed = true;
            // sofa cells that moved
            movedCells.add(new long[]{cellKey(nr + dr, nc + dc), dr, dc});
            movedCells.add(new long[]{cellKey(pair[0] + dr, pair[1] + dc), dr, dc});
            if (dc != 0) {
                // horizontal: one cell stays in place visually (trail becomes lead's old pos)
                // just animate all destination cells
            }
        } else if (isBox(nextObj)) {
            int br = nr + dr, bc = nc + dc;
            if (!inBounds(br, bc) || ground[br][bc] == WALL || objects[br][bc] != NONE) return;
            objects[br][bc] = nextObj;
            objects[nr][nc] = NONE;
            pushed = true;
            movedCells.add(new long[]{cellKey(br, bc), dr, dc});
        } else if (nextObj != NONE) {
            return;
        }

        int oldR = playerRow, oldC = playerCol;
        objects[playerRow][playerCol] = NONE;
        objects[nr][nc] = PLAYER;
        playerRow = nr;
        playerCol = nc;
        moveCount++;
        movedCells.add(new long[]{cellKey(nr, nc), dr, dc});

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
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                slideOffsets.clear();
                sliding = false;
                invalidate();
                checkWin();
            }
        });
        anim.start();
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

    private void checkWin() {
        for (int r = 0; r < ground.length; r++) {
            for (int c = 0; c < ground[r].length; c++) {
                int g = ground[r][c];
                int o = objects[r][c];
                if (g == TARGET_PLANT && o != BOX_PLANT) return;
                if (g == TARGET_TV && o != BOX_TV) return;
                if (g == TARGET_SOFA_L && o != BOX_SOFA_L) return;
                if (g == TARGET_SOFA_R && o != BOX_SOFA_R) return;
            }
        }
        if (completeListener != null) completeListener.onLevelComplete();
    }
}
