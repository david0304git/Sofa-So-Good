package com.speed.sofasogood.models;

public class LeaderboardEntry {
    private String id;
    private String timestamp;
    private String playerName;
    private String groupId;
    private int score;
    private int level;
    private String metadata;
    private int rank;

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public String getMetadata() {
        return metadata;
    }

    public int getRank() {
        return rank;
    }
}
