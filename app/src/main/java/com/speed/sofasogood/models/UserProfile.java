package com.speed.sofasogood.models;

public class UserProfile {
    private String uid;
    private String email;
    private String playerName;

    public UserProfile() {
        // required for Firestore
    }

    public UserProfile(String uid, String email, String playerName) {
        this.uid = uid;
        this.email = email;
        this.playerName = playerName;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}