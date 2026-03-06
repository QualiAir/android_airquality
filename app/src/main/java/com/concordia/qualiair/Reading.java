package com.concordia.qualiair;

public class Reading {
    private String time;
    private float value;//float will allow for better precision
    private String level;

    //Constructor
    public Reading(String time, float value, String level) {
        this.time = time;
        this.value = value;
        this.level = level;
    }

    //Getters
    public String getTime() {

        return time;

    }
    public float getValue() {

        return value;

    }
    public String getLevel() {

        return level;

    }
}
