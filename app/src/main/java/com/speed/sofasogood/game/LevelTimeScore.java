package com.speed.sofasogood.game;

import java.util.Locale;

/**
 * Formats level elapsed time for UI and derives a score (faster = higher).
 */
public final class LevelTimeScore {

    private static final int SCORE_BASE = 1_000_000;
    /** Lose one point per 100 ms of play time (after pauses are excluded). */
    private static final int MS_PER_SCORE_UNIT = 100;

    private LevelTimeScore() {}

    public static int scoreFromElapsedMs(long elapsedMs) {
        if (elapsedMs <= 0) return SCORE_BASE;
        long lost = elapsedMs / MS_PER_SCORE_UNIT;
        long score = SCORE_BASE - lost;
        return score > 0 ? (int) score : 0;
    }

    /** Minutes, seconds, centiseconds — e.g. {@code 3:07.45}. */
    public static String formatElapsed(long ms) {
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000;
        long m = totalSec / 60;
        long s = totalSec % 60;
        long cs = (ms % 1000) / 10;
        return String.format(Locale.US, "%d:%02d.%02d", m, s, cs);
    }
}
