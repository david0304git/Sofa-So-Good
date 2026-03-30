package com.speed.sofasogood.models;

public class LeaderboardSubmitRequest {

    private final int level;
    private final int score;
    private final long timeMs;
    private final String playerName;

    public LeaderboardSubmitRequest(int level, int score, long timeMs, String playerName) {
        this.level = level;
        this.score = score;
        this.timeMs = timeMs;
        this.playerName = playerName != null ? playerName : "";
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public String getPlayerName() {
        return playerName;
    }
}
