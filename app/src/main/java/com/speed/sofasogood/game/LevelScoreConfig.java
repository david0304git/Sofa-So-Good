package com.speed.sofasogood.game;

public class LevelScoreConfig {

    public final long bestTimeMs;
    public final long worstTimeMs;
    public final int bestSteps;
    public final int worstSteps;

    public final long star3TimeMs;
    public final long star2TimeMs;
    public final long star1TimeMs;

    public final int star3Steps;
    public final int star2Steps;
    public final int star1Steps;

    public LevelScoreConfig(
            long bestTimeMs,
            long worstTimeMs,
            int bestSteps,
            int worstSteps,
            long star3TimeMs,
            long star2TimeMs,
            long star1TimeMs,
            int star3Steps,
            int star2Steps,
            int star1Steps
    ) {
        this.bestTimeMs = bestTimeMs;
        this.worstTimeMs = worstTimeMs;
        this.bestSteps = bestSteps;
        this.worstSteps = worstSteps;
        this.star3TimeMs = star3TimeMs;
        this.star2TimeMs = star2TimeMs;
        this.star1TimeMs = star1TimeMs;
        this.star3Steps = star3Steps;
        this.star2Steps = star2Steps;
        this.star1Steps = star1Steps;
    }
}