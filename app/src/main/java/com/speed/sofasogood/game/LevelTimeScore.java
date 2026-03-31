package com.speed.sofasogood.game;

import java.util.Locale;

public final class LevelTimeScore {

    private static final int MAX_SCORE = 10000;
    private static final int MIN_SCORE = 100;

    // Time thresholds
    private static final long PERFECT_TIME_MS = 30_000;   // ≤30s = max time score
    private static final long WORST_TIME_MS = 300_000;    // ≥5min = min time score

    // Steps thresholds
    private static final int PERFECT_STEPS = 15;          // ≤15 steps = max steps score
    private static final int WORST_STEPS = 150;           // ≥150 steps = min steps score

    private LevelTimeScore() {}

    /**
     * Calculate combined score from time and steps.
     * Time contributes 50%, steps contribute 50%.
     */
    public static int scoreFromElapsedMs(long elapsedMs, int steps) {
        int timeScore = calcTimeScore(elapsedMs);
        int stepsScore = calcStepsScore(steps);
        return (timeScore + stepsScore) / 2;
    }

    /** Backwards-compatible: time-only scoring */
    public static int scoreFromElapsedMs(long elapsedMs) {
        return calcTimeScore(elapsedMs);
    }

    private static int calcTimeScore(long elapsedMs) {
        if (elapsedMs <= PERFECT_TIME_MS) return MAX_SCORE;
        if (elapsedMs >= WORST_TIME_MS) return MIN_SCORE;
        float ratio = (float)(elapsedMs - PERFECT_TIME_MS) / (WORST_TIME_MS - PERFECT_TIME_MS);
        return Math.max((int)(MAX_SCORE - ratio * (MAX_SCORE - MIN_SCORE)), MIN_SCORE);
    }

    private static int calcStepsScore(int steps) {
        if (steps <= PERFECT_STEPS) return MAX_SCORE;
        if (steps >= WORST_STEPS) return MIN_SCORE;
        float ratio = (float)(steps - PERFECT_STEPS) / (WORST_STEPS - PERFECT_STEPS);
        return Math.max((int)(MAX_SCORE - ratio * (MAX_SCORE - MIN_SCORE)), MIN_SCORE);
    }

    /**
     * Star rating based on combined score.
     * 3 stars: ≥7000, 2 stars: ≥3500, 1 star: rest
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
