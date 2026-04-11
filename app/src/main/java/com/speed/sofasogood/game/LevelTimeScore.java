package com.speed.sofasogood.game;

import java.util.Locale;

public final class LevelTimeScore {

    private LevelTimeScore() {}

    public static int calculateScore(long elapsedMs, int steps, LevelScoreConfig config) {
        int timeScore = calculateTimeScore(elapsedMs, config);
        int stepScore = calculateStepScore(steps, config);

        int finalScore = Math.round(timeScore * 0.4f + stepScore * 0.6f);
        return clamp(finalScore, 0, 100);
    }

    public static int calculateStars(long elapsedMs, int steps, LevelScoreConfig config) {
        if (elapsedMs <= config.star3TimeMs && steps <= config.star3Steps) return 3;
        if (elapsedMs <= config.star2TimeMs && steps <= config.star2Steps) return 2;
        if (elapsedMs <= config.star1TimeMs && steps <= config.star1Steps) return 1;
        return 0;
    }

    private static int calculateTimeScore(long elapsedMs, LevelScoreConfig config) {
        if (elapsedMs <= config.bestTimeMs) return 100;
        if (elapsedMs >= config.worstTimeMs) return 0;

        float ratio = (float) (elapsedMs - config.bestTimeMs)
                / (float) (config.worstTimeMs - config.bestTimeMs);

        return clamp(Math.round(100f * (1f - ratio)), 0, 100);
    }

    private static int calculateStepScore(int steps, LevelScoreConfig config) {
        if (steps <= config.bestSteps) return 100;
        if (steps >= config.worstSteps) return 0;

        float ratio = (float) (steps - config.bestSteps)
                / (float) (config.worstSteps - config.bestSteps);

        return clamp(Math.round(100f * (1f - ratio)), 0, 100);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String formatElapsed(long ms) {
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000;
        long m = totalSec / 60;
        long s = totalSec % 60;
        long cs = (ms % 1000) / 10;
        return String.format(Locale.US, "%d:%02d.%02d", m, s, cs);
    }
}