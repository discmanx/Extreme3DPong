package com.weakentroll.extreme3dpong;

/**
 * Created by Ryan on 2017-07-27.
 */

public class Player {

    public Player(String username, String password, int playerid, int score, float locationX, float locationY, float locationZ, int highScore) {
        this.username = username;
        this.password = password;
        this.playerid = playerid;
        this.score = score;
        this.locationX = locationX;
        this.locationY = locationY;
        this.locationZ = locationZ;
        this.highScore = highScore;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    private String username;
    private String password;

    public int getPlayerid() {
        return playerid;
    }

    public void setPlayerid(int playerid) {
        this.playerid = playerid;
    }

    private int playerid;
    private int sessionId;
    private int score;
    private float locationX, locationY, locationZ;
    private int highScore;


    public void setLocation(float X, float Y, float Z) {
        locationX = X;
        locationY = Y;
        locationZ = Z;

    }

    public float[] getLocation() {
        float[] location = new float[3];
        location[0] = locationX;
        location[1] = locationY;
        location[2] = locationZ;

        return location;
    }

    public String getUsername() {
        return username;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

}
