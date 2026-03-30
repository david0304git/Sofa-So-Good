package com.speed.sofasogood.models;

import java.util.List;

public class LeaderboardResponse {
    private List<LeaderboardEntry> entries;
    private int total;

    public List<LeaderboardEntry> getEntries() {
        return entries;
    }

    public int getTotal() {
        return total;
    }
}
