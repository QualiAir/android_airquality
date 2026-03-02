package com.concordia.qualiair;

public class Reading {
    private String time;
    private int value;
    private String level;

    //Constructor
    public Reading(String time, int value, String level) {
        this.time = time;
        this.value = value;
        this.level = level;
    }

    //Getters
    public String getTime() {

        return time;

    }
    public int getValue() {

        return value;

    }
    public String getLevel() {

        return level;

    }
}
