package com.speed.sofasogood.game;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.speed.sofasogood.R;

public class GameView extends View {

    /*
     * Tile codes:
     *  0 = Empty       1 = Wall        2 = Player
     *
     * Single-tile boxes:
     * 10 = Plant      12 = TV
     *
     * Sofa (2-tile, always L on left, R on right):
     * 13 = Sofa Left  14 = Sofa Right
     *
     * Targets (ghost):
     * 20 = Plant target   22 = TV target
     * 23 = Sofa L target  24 = Sofa R target
     *
     * Box on target:
     * 30 = Plant on target  32 = TV on target
     * 33 = Sofa L on target 34 = Sofa R on target
     *
     * Player on target: 40/42/43/44
     */

    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int PLAYER = 2;

    public static final int BOX_PLANT = 10;
    public static final int BOX_TV = 12;
    public static final int BOX_SOFA_L = 13;
    public static final int BOX_SOFA_R = 14;

    public static final int TARGET_PLANT = 20;
    public static final int TARGET_TV = 22;
    public static final int TARGET_SOFA_L = 23;
    public static final int TARGET_SOFA_R = 24;

    public static final int BOX_ON_PLANT = 30;
    public static final int BOX_ON_TV = 32;
    public static final int BOX_ON_SOFA_L = 33;
    public static final int BOX_ON_SOFA_R = 34;

    public static final int PLAYER_ON_PLANT = 40;
    public static final int PLAYER_ON_TV = 42;
    public static final int PLAYER_ON_SOFA_L = 43;
    public static final int PLAYER_ON_SOFA_R = 44;

    private int[][] map;
    private int[][] targets; // permanent target layer, never modified
    private int playerRow, playerCol;
    private int tileSize;
    private int offsetX, offsetY;

    private Bitmap bmpWall, bmpFloor, bmpPlayer;
    private Bitmap bmpPlant, bmpSofaL, bmpSofaR, bmpTv;
    private Bitmap bmpPlantGhost, bmpSofaLGhost, bmpSofaRGhost, bmpTvGhost;

    private float touchStartX, touchStartY;
    private OnLevelCompleteListener completeListener;

    // Drop animation
    private float[][] dropProgress;
    private boolean animating = false;
    private java.util.List<ValueAnimator> runningAnimators = new java.util.ArrayList<>();
    private static final long TILE_DELAY = 50;
    private static final long DROP_DURATION = 400;

    public interface OnLevelCompleteListener {
        void onLevelComplete();
    }

    public void setOnLevelCompleteListener(OnLevelCompleteListener listener) {
        this.completeListener = listener;
    }

    public GameView(Context context) { super(context); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void loadLevel(int[][] level) {
        cancelAnimations();
        map = new int[level.length][];
        targets = new int[level.length][level[0].length];
        for (int r = 0; r < level.length; r++) {
            map[r] = level[r].clone();
            for (int c = 0; c < level[r].length; c++) {
                int t = level[r][c];
                if (isTarget(t)) {
                    targets[r][c] = t;  // store target permanently
                    map[r][c] = EMPTY;  // strip from main map
                }
                if (isPlayer(t)) { playerRow = r; playerCol = c; }
            }
        }
        dropProgress = new float[map.length][map[0].length];
        animating = true;
        if (tileSize > 0) startDropAnimation();
        invalidate();
    }

    private void cancelAnimations() {
        for (ValueAnimator a : runningAnimators) {
            a.cancel();
        }
        runningAnimators.clear();
        animating = false;
    }

    private void startDropAnimation() {
        if (map == null) return;
        cancelAnimations();
        int rows = map.length;
        int cols = map[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int fr = r, fc = c;
                long delay = (r * cols + c) * TILE_DELAY;

                ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                anim.setDuration(DROP_DURATION);
                anim.setStartDelay(delay);
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
        if (map == null) return;
        tileSize = Math.min(w / map[0].length, h / map.length);
        offsetX = (w - map[0].length * tileSize) / 2;
        offsetY = (h - map.length * tileSize) / 2;

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
        Bitmap src = BitmapFactory.decodeResource(getResources(), resId);
        return Bitmap.createScaledBitmap(src, tileSize, tileSize, true);
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
        if (map == null) return;

        int totalH = map.length * tileSize;

        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                int x = offsetX + c * tileSize;
                int targetY = offsetY + r * tileSize;

                // During animation, tile drops from above
                int y;
                if (animating || (dropProgress != null && dropProgress[r][c] < 1f)) {
                    float progress = dropProgress != null ? dropProgress[r][c] : 1f;
                    int startY = offsetY - totalH;
                    y = (int) (startY + (targetY - startY) * progress);
                } else {
                    y = targetY;
                }

                int tile = map[r][c];

                if (tile == WALL) {
                    canvas.drawBitmap(bmpWall, x, y, null);
                    continue;
                }

                canvas.drawBitmap(bmpFloor, x, y, null);

                // Always draw ghost from permanent targets layer
                Bitmap ghost = getGhostBitmap(targets[r][c]);
                if (ghost != null) canvas.drawBitmap(ghost, x, y, null);

                // Box
                Bitmap box = getBoxBitmap(tile);
                if (box != null) canvas.drawBitmap(box, x, y, null);

                // Player
                if (isPlayer(tile)) canvas.drawBitmap(bmpPlayer, x, y, null);
            }
        }
    }

    private Bitmap getBoxBitmap(int t) {
        switch (t) {
            case BOX_PLANT:  return bmpPlant;
            case BOX_SOFA_L: return bmpSofaL;
            case BOX_SOFA_R: return bmpSofaR;
            case BOX_TV:     return bmpTv;
        }
        return null;
    }

    private Bitmap getGhostBitmap(int t) {
        switch (t) {
            case TARGET_PLANT:  return bmpPlantGhost;
            case TARGET_SOFA_L: return bmpSofaLGhost;
            case TARGET_SOFA_R: return bmpSofaRGhost;
            case TARGET_TV:     return bmpTvGhost;
        }
        return null;
    }

    private boolean isPlayer(int t) {
        return t == PLAYER || t == PLAYER_ON_PLANT || t == PLAYER_ON_TV
                || t == PLAYER_ON_SOFA_L || t == PLAYER_ON_SOFA_R;
    }

    private boolean isSingleBox(int t) {
        return t == BOX_PLANT || t == BOX_TV;
    }

    private boolean isSingleBoxOnTarget(int t) {
        return t == BOX_ON_PLANT || t == BOX_ON_TV;
    }

    private boolean isSofaPart(int t) {
        return t == BOX_SOFA_L || t == BOX_SOFA_R || t == BOX_ON_SOFA_L || t == BOX_ON_SOFA_R;
    }

    private boolean isTarget(int t) {
        return t == TARGET_PLANT || t == TARGET_TV
                || t == TARGET_SOFA_L || t == TARGET_SOFA_R;
    }

    private boolean isMatchingTarget(int boxTile, int targetTile) {
        switch (boxTile) {
            case BOX_PLANT: case BOX_ON_PLANT: return targetTile == TARGET_PLANT;
            case BOX_TV:    case BOX_ON_TV:    return targetTile == TARGET_TV;
        }
        return false;
    }

    private boolean isPlayerOnTarget(int t) {
        return t == PLAYER_ON_PLANT || t == PLAYER_ON_TV
                || t == PLAYER_ON_SOFA_L || t == PLAYER_ON_SOFA_R;
    }

    private boolean isAnyBox(int t) {
        return isSingleBox(t) || isSingleBoxOnTarget(t) || isSofaPart(t);
    }

    private boolean isBlocking(int t) {
        return t == WALL || isAnyBox(t);
    }

    private boolean canWalkOn(int t) {
        return t == EMPTY || isTarget(t) || isPlayerOnTarget(t);
    }

    private int tileTargetCode(int t) { return -1; }

    // What tile code to place a box of given type on a destination
    private int placeBox(int boxTile, int dest) {
        switch (boxTile) {
            case BOX_SOFA_L: case BOX_ON_SOFA_L: return BOX_SOFA_L;
            case BOX_SOFA_R: case BOX_ON_SOFA_R: return BOX_SOFA_R;
            case BOX_PLANT:  case BOX_ON_PLANT:  return BOX_PLANT;
            case BOX_TV:     case BOX_ON_TV:     return BOX_TV;
            default: return boxTile;
        }
    }

    private int removeBox(int t) { return EMPTY; }

    private int placePlayer(int dest) { return PLAYER; }

    private int removePlayer(int t) { return EMPTY; }

    // Find the other half of a sofa pair
    private int[] findSofaPair(int r, int c) {
        int t = map[r][c];
        if (t == BOX_SOFA_L || t == BOX_ON_SOFA_L) {
            // Right half should be at (r, c+1)
            if (inBounds(r, c + 1) && isSofaRight(map[r][c + 1])) return new int[]{r, c + 1};
        } else if (t == BOX_SOFA_R || t == BOX_ON_SOFA_R) {
            // Left half should be at (r, c-1)
            if (inBounds(r, c - 1) && isSofaLeft(map[r][c - 1])) return new int[]{r, c - 1};
        }
        return null;
    }

    private boolean isSofaLeft(int t) { return t == BOX_SOFA_L || t == BOX_ON_SOFA_L; }
    private boolean isSofaRight(int t) { return t == BOX_SOFA_R || t == BOX_ON_SOFA_R; }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animating) return true; // Block input during animation
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                float minSwipe = tileSize * 0.5f;
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                    move(0, dx > 0 ? 1 : -1);
                } else if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > minSwipe) {
                    move(dy > 0 ? 1 : -1, 0);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void move(int dr, int dc) {
        int newR = playerRow + dr;
        int newC = playerCol + dc;
        if (!inBounds(newR, newC)) return;

        int next = map[newR][newC];
        if (next == WALL) return;

        // Pushing a sofa part
        if (isSofaPart(next)) {
            if (!pushSofa(newR, newC, dr, dc)) return;
            next = map[newR][newC];
        }
        // Pushing a single box
        else if (isSingleBox(next) || isSingleBoxOnTarget(next)) {
            int behindR = newR + dr;
            int behindC = newC + dc;
            if (!inBounds(behindR, behindC)) return;
            int behind = map[behindR][behindC];
            if (isBlocking(behind)) return;

            map[behindR][behindC] = placeBox(next, behind);
            map[newR][newC] = removeBox(next);
            next = map[newR][newC];
        }

        if (isBlocking(next)) return;

        // Move player
        map[playerRow][playerCol] = removePlayer(map[playerRow][playerCol]);
        map[newR][newC] = placePlayer(next);
        playerRow = newR;
        playerCol = newC;

        invalidate();
        checkWin();
    }

    private boolean pushSofa(int sofaR, int sofaC, int dr, int dc) {
        int[] pair = findSofaPair(sofaR, sofaC);
        if (pair == null) return false;

        int pairR = pair[0], pairC = pair[1];
        int sofaTile = map[sofaR][sofaC];
        int pairTile = map[pairR][pairC];

        if (dc != 0) {
            // Horizontal push
            // Determine the leading piece (the one in the push direction)
            int leadR, leadC, trailR, trailC;
            int leadTile, trailTile;
            if (dc > 0) {
                // Pushing right: rightmost piece leads
                if (sofaC > pairC) { leadR = sofaR; leadC = sofaC; leadTile = sofaTile; trailR = pairR; trailC = pairC; trailTile = pairTile; }
                else { leadR = pairR; leadC = pairC; leadTile = pairTile; trailR = sofaR; trailC = sofaC; trailTile = sofaTile; }
            } else {
                // Pushing left: leftmost piece leads
                if (sofaC < pairC) { leadR = sofaR; leadC = sofaC; leadTile = sofaTile; trailR = pairR; trailC = pairC; trailTile = pairTile; }
                else { leadR = pairR; leadC = pairC; leadTile = pairTile; trailR = sofaR; trailC = sofaC; trailTile = sofaTile; }
            }

            int destR = leadR + dr;
            int destC = leadC + dc;
            if (!inBounds(destR, destC)) return false;
            int dest = map[destR][destC];
            if (isBlocking(dest)) return false;

            // Move lead to dest
            map[destR][destC] = placeBox(leadTile, dest);
            // Move trail to where lead was
            map[leadR][leadC] = placeBox(trailTile, removeBox(leadTile));
            // Clear trail original position
            map[trailR][trailC] = removeBox(trailTile);

        } else {
            // Vertical push: both halves move in same direction
            int destSofaR = sofaR + dr;
            int destSofaC = sofaC + dc;
            int destPairR = pairR + dr;
            int destPairC = pairC + dc;

            if (!inBounds(destSofaR, destSofaC) || !inBounds(destPairR, destPairC)) return false;
            int destSofa = map[destSofaR][destSofaC];
            int destPair = map[destPairR][destPairC];
            if (isBlocking(destSofa) || isBlocking(destPair)) return false;

            // Clear both old positions first
            map[sofaR][sofaC] = removeBox(sofaTile);
            map[pairR][pairC] = removeBox(pairTile);

            // Place both at new positions
            map[destSofaR][destSofaC] = placeBox(sofaTile, destSofa);
            map[destPairR][destPairC] = placeBox(pairTile, destPair);
        }

        return true;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < map.length && c >= 0 && c < map[0].length;
    }

    private void checkWin() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                int target = targets[r][c];
                if (target == TARGET_PLANT  && map[r][c] != BOX_PLANT)  return;
                if (target == TARGET_TV     && map[r][c] != BOX_TV)     return;
                if (target == TARGET_SOFA_L && map[r][c] != BOX_SOFA_L) return;
                if (target == TARGET_SOFA_R && map[r][c] != BOX_SOFA_R) return;
            }
        }
        if (completeListener != null) completeListener.onLevelComplete();
    }

    private boolean hasBox(int r, int c, int boxType) {
        return map[r][c] == boxType;
    }
}
