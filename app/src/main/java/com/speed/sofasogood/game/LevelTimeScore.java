package com.speed.sofasogood.game;

import java.util.Locale;

public final class LevelTimeScore {

    // Score range
    private static final int MAX_SCORE = 10000;
    private static final int MIN_SCORE = 100;

    // Time thresholds (in ms)
    private static final long THREE_STAR_MS = 30_000;   // ≤30s = 10000 (3 stars)
    private static final long TWO_STAR_MS = 120_000;    // ≤2min = ~5000 (2 stars)
    private static final long ONE_STAR_MS = 300_000;    // ≤5min = ~1000 (1 star)

    private LevelTimeScore() {}

    /**
     * Calculate score from elapsed play time (pauses excluded).
     * ≤30s  → 10000 (3 stars)
     * 30s–5min → linear decay from 10000 to 100
     * >5min → 100 (1 star)
     */
    public static int scoreFromElapsedMs(long elapsedMs) {
        if (elapsedMs <= THREE_STAR_MS) return MAX_SCORE;
        if (elapsedMs >= ONE_STAR_MS) return MIN_SCORE;

        // Linear interpolation between 30s and 5min
        float ratio = (float)(elapsedMs - THREE_STAR_MS) / (ONE_STAR_MS - THREE_STAR_MS);
        int score = (int)(MAX_SCORE - ratio * (MAX_SCORE - MIN_SCORE));
        return Math.max(score, MIN_SCORE);
    }

    /**
     * Star rating: 3 = excellent, 2 = good, 1 = completed.
     */
    public static int starsFromScore(int score) {
        if (score >= 7000) return 3;
        if (score >= 3500) return 2;
        return 1;
    }

    /** Format as m:ss.cc — e.g. 3:07.45 */
    public static String formatElapsed(long ms) {
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000;
        long m = totalSec / 60;
        long s = totalSec % 60;
        long cs = (ms % 1000) / 10;
        return String.format(Locale.US, "%d:%02d.%02d", m, s, cs);
    }
}
