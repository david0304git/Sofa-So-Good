package com.speed.sofasogood.models;

public class LeaderboardSubmitRequest {

    private final int level;
    private final int score;
    private final String playerName;
    private final String metadata;

    public LeaderboardSubmitRequest(int level, int score, String playerName, String metadata) {
        this.level = level;
        this.score = score;
        this.playerName = playerName != null ? playerName : "";
        this.metadata = metadata;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMetadata() {
        return metadata;
    }
}