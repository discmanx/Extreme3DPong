package com.weakentroll.extreme3dpong;

/**
 * Created by Ryan on 2017-07-27.
 */

public class Player {

    private String userName;
    private String password;
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

}
