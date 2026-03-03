package com.concordia.qualiair;

import android.content.Context;
import android.content.SharedPreferences;

/** FUNC-5.1 (Design Preferences Data Model)
 * UserPreferences

 * Data model responsible for storing:
 * 1- User account information
 * 2- Air quality threshold ranges for NH3 and CO2 gases and Dust

 *  Supports:
 * - Save the user information (username, email)
 * - Initializing default ranges  for NH3 and CO2 gases and Dust
 * - Updating ranges later
 * - Determining air quality level based on a measured value
 */

/** FUNC-5.2  (Implement Local Storage Logic)
 * handle persistent storage using SharedPreferences
 * Store small configuration data
 * The data remains saved even after the application is closed or restarted

 * Supports:
 * - Saving user account information
 * - Saving NH3, CO2, and Dust threshold ranges
 * - Loading saved preferences when the app starts
 * - Keeping data available after the application restarts
 */


public class UserPreferences {

    // User Account Information
    private String username;
    private String email;

    // NH3 (Ammonia) Threshold Ranges
    private int nh3LowMin;
    private int nh3LowMax;

    private int nh3MediumMin;
    private int nh3MediumMax;

    private int nh3HighMin;
    private int nh3HighMax;

    // CO2 Threshold Ranges
    private int co2LowMin;
    private int co2LowMax;

    private int co2MediumMin;
    private int co2MediumMax;

    private int co2HighMin;
    private int co2HighMax;

    // Dust Threshold Ranges
    private int dustLowMin;
    private int dustLowMax;

    private int dustMediumMin;
    private int dustMediumMax;

    private int dustHighMin;
    private int dustHighMax;


    //Constructor of userPreferences
    public UserPreferences(String username,
                           String email,
                           int nh3LowMin, int nh3LowMax,
                           int nh3MediumMin, int nh3MediumMax,
                           int nh3HighMin, int nh3HighMax,
                           int co2LowMin, int co2LowMax,
                           int co2MediumMin, int co2MediumMax,
                           int co2HighMin, int co2HighMax,
                           int dustLowMin, int dustLowMax,
                           int dustMediumMin, int dustMediumMax,
                           int dustHighMin, int dustHighMax) {

        // Validate ranges first
        if (!areRangesValid(nh3LowMin, nh3LowMax,
                nh3MediumMin, nh3MediumMax,
                nh3HighMin, nh3HighMax)) {
            throw new IllegalArgumentException("Invalid NH3 ranges during initialization.");
        }

        if (!areRangesValid(co2LowMin, co2LowMax,
                co2MediumMin, co2MediumMax,
                co2HighMin, co2HighMax)) {
            throw new IllegalArgumentException("Invalid CO2 ranges during initialization.");
        }

        if (!areRangesValid(dustLowMin, dustLowMax,
                dustMediumMin, dustMediumMax,
                dustHighMin, dustHighMax)) {
            throw new IllegalArgumentException("Invalid Dust ranges during initialization.");
        }

        this.username = username;
        this.email = email;

        // NH3 initialization
        this.nh3LowMin = nh3LowMin;
        this.nh3LowMax = nh3LowMax;
        this.nh3MediumMin = nh3MediumMin;
        this.nh3MediumMax = nh3MediumMax;
        this.nh3HighMin = nh3HighMin;
        this.nh3HighMax = nh3HighMax;

        // CO2 initialization
        this.co2LowMin = co2LowMin;
        this.co2LowMax = co2LowMax;
        this.co2MediumMin = co2MediumMin;
        this.co2MediumMax = co2MediumMax;
        this.co2HighMin = co2HighMin;
        this.co2HighMax = co2HighMax;

        // Dust initialization
        this.dustLowMin = dustLowMin;
        this.dustLowMax = dustLowMax;
        this.dustMediumMin = dustMediumMin;
        this.dustMediumMax = dustMediumMax;
        this.dustHighMin = dustHighMin;
        this.dustHighMax = dustHighMax;

    }

    //SharedPreferences Setup
    private static final String PREF_NAME = "QualiAirPreferences";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Keys
    // User keys
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    // NH3 keys
    private static final String KEY_NH3_LOW_MIN = "nh3LowMin";
    private static final String KEY_NH3_LOW_MAX = "nh3LowMax";
    private static final String KEY_NH3_MEDIUM_MIN = "nh3MediumMin";
    private static final String KEY_NH3_MEDIUM_MAX = "nh3MediumMax";
    private static final String KEY_NH3_HIGH_MIN = "nh3HighMin";
    private static final String KEY_NH3_HIGH_MAX = "nh3HighMax";

    // CO2 keys
    private static final String KEY_CO2_LOW_MIN = "co2LowMin";
    private static final String KEY_CO2_LOW_MAX = "co2LowMax";
    private static final String KEY_CO2_MEDIUM_MIN = "co2MediumMin";
    private static final String KEY_CO2_MEDIUM_MAX = "co2MediumMax";
    private static final String KEY_CO2_HIGH_MIN = "co2HighMin";
    private static final String KEY_CO2_HIGH_MAX = "co2HighMax";


    // Dust Keys
    private static final String KEY_DUST_LOW_MIN = "dustLowMin";
    private static final String KEY_DUST_LOW_MAX = "dustLowMax";
    private static final String KEY_DUST_MEDIUM_MIN= "dustMedMin";
    private static final String KEY_DUST_MEDIUM_MAX= "dustMedMax";
    private static final String KEY_DUST_HIGH_MIN = "dustHighMin";
    private static final String KEY_DUST_HIGH_MAX = "dustHighMax";


    //Constructor of UserPreferences (connecting the class to Android storage)
    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //function to Save the values to SharedPreferences
    public void saveAllPreferences(){
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL,email);

        //NH3
        editor.putInt(KEY_NH3_HIGH_MIN, nh3HighMin);
        editor.putInt(KEY_NH3_HIGH_MAX, nh3HighMax);
        editor.putInt(KEY_NH3_MEDIUM_MIN, nh3MediumMin);
        editor.putInt(KEY_NH3_MEDIUM_MAX, nh3MediumMax);
        editor.putInt(KEY_NH3_LOW_MIN, nh3LowMin);
        editor.putInt(KEY_NH3_LOW_MAX, nh3LowMax);

        //CO2
        editor.putInt(KEY_CO2_LOW_MIN, co2LowMin);
        editor.putInt(KEY_CO2_LOW_MAX, co2LowMax);
        editor.putInt(KEY_CO2_MEDIUM_MIN, co2MediumMin);
        editor.putInt(KEY_CO2_MEDIUM_MAX, co2MediumMax);
        editor.putInt(KEY_CO2_HIGH_MIN, co2HighMin);
        editor.putInt(KEY_CO2_HIGH_MAX, co2HighMax);

        //Dust
        editor.putInt(KEY_DUST_LOW_MIN, dustLowMin);
        editor.putInt(KEY_DUST_LOW_MAX, dustLowMax);
        editor.putInt(KEY_DUST_MEDIUM_MIN, dustMediumMin);
        editor.putInt(KEY_DUST_MEDIUM_MAX, dustMediumMax);
        editor.putInt(KEY_DUST_HIGH_MIN, dustHighMin);
        editor.putInt(KEY_DUST_HIGH_MAX, dustHighMax);

        editor.apply();
    }

    //a function to load all values from SharedPreferences
    public void loadAllPreferences(){
        //user info
        username = sharedPreferences.getString(KEY_USERNAME, "");
        email= sharedPreferences.getString(KEY_EMAIL,"");

        //nh3
        nh3LowMin = sharedPreferences.getInt(KEY_NH3_LOW_MIN, 0);
        nh3LowMax = sharedPreferences.getInt(KEY_NH3_LOW_MAX, 0);
        nh3MediumMin = sharedPreferences.getInt(KEY_NH3_MEDIUM_MIN, 0);
        nh3MediumMax = sharedPreferences.getInt(KEY_NH3_MEDIUM_MAX, 0);
        nh3HighMin = sharedPreferences.getInt(KEY_NH3_HIGH_MIN, 0);
        nh3HighMax = sharedPreferences.getInt(KEY_NH3_HIGH_MAX, 0);

        //co2
        nh3LowMin = sharedPreferences.getInt(KEY_NH3_LOW_MIN, 0);
        nh3LowMax = sharedPreferences.getInt(KEY_NH3_LOW_MAX, 0);
        nh3MediumMin = sharedPreferences.getInt(KEY_NH3_MEDIUM_MIN, 0);
        nh3MediumMax = sharedPreferences.getInt(KEY_NH3_MEDIUM_MAX, 0);
        nh3HighMin = sharedPreferences.getInt(KEY_NH3_HIGH_MIN, 0);
        nh3HighMax = sharedPreferences.getInt(KEY_NH3_HIGH_MAX, 0);

        //dust
        dustLowMin = sharedPreferences.getInt(KEY_DUST_LOW_MIN, 0);
        dustLowMax = sharedPreferences.getInt(KEY_DUST_LOW_MAX, 0);
        dustMediumMin = sharedPreferences.getInt(KEY_DUST_MEDIUM_MIN, 0);
        dustMediumMax = sharedPreferences.getInt(KEY_DUST_MEDIUM_MAX, 0);
        dustHighMin = sharedPreferences.getInt(KEY_DUST_HIGH_MIN, 0);
        dustHighMax = sharedPreferences.getInt(KEY_DUST_HIGH_MAX, 0);

    }


    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public int getNh3LowMin() { return nh3LowMin; }
    public int getNh3LowMax() { return nh3LowMax; }
    public int getNh3MediumMin() { return nh3MediumMin; }
    public int getNh3MediumMax() { return nh3MediumMax; }
    public int getNh3HighMin() { return nh3HighMin; }
    public int getNh3HighMax() { return nh3HighMax; }

    public int getCo2LowMin() { return co2LowMin; }
    public int getCo2LowMax() { return co2LowMax; }
    public int getCo2MediumMin() { return co2MediumMin; }
    public int getCo2MediumMax() { return co2MediumMax; }
    public int getCo2HighMin() { return co2HighMin; }
    public int getCo2HighMax() { return co2HighMax; }


    public int getDustLowMin() { return dustLowMin; }
    public int getDustLowMax() { return dustLowMax; }
    public int getDustMediumMin() { return dustMediumMin; }
    public int getDustMediumMax() { return dustMediumMax; }
    public int getDustHighMin() { return dustHighMin; }
    public int getDustHighMax() { return dustHighMax; }


    //Updates all NH3 threshold ranges at once
    public void updateNH3Ranges(int lowMin, int lowMax,
                                int mediumMin, int mediumMax,
                                int highMin, int highMax) {

        if (!areRangesValid(lowMin, lowMax, mediumMin, mediumMax, highMin, highMax)) {
            throw new IllegalArgumentException("Invalid NH3 ranges: overlapping or incorrectly ordered values.");
        }

        this.nh3LowMin = lowMin;
        this.nh3LowMax = lowMax;
        this.nh3MediumMin = mediumMin;
        this.nh3MediumMax = mediumMax;
        this.nh3HighMin = highMin;
        this.nh3HighMax = highMax;
    }

    //Updates all CO2 threshold ranges at once.
    public void updateCO2Ranges(int lowMin, int lowMax,
                                int mediumMin, int mediumMax,
                                int highMin, int highMax) {

        if (!areRangesValid(lowMin, lowMax, mediumMin, mediumMax, highMin, highMax)) {
            throw new IllegalArgumentException("Invalid CO2 ranges: overlapping or incorrectly ordered values.");
        }

        this.co2LowMin = lowMin;
        this.co2LowMax = lowMax;
        this.co2MediumMin = mediumMin;
        this.co2MediumMax = mediumMax;
        this.co2HighMin = highMin;
        this.co2HighMax = highMax;
    }

    public void updateDustRanges(int lowMin, int lowMax,
                                 int mediumMin, int mediumMax,
                                 int highMin, int highMax) {

        if (!areRangesValid(lowMin, lowMax, mediumMin, mediumMax, highMin, highMax)) {
            throw new IllegalArgumentException("Invalid Dust ranges: overlapping or incorrectly ordered values.");
        }

        this.dustLowMin = lowMin;
        this.dustLowMax = lowMax;
        this.dustMediumMin = mediumMin;
        this.dustMediumMax = mediumMax;
        this.dustHighMin = highMin;
        this.dustHighMax = highMax;
    }

    //Determines NH3 air quality level based on measured value
    public String getNH3Level(int value) {
        if (value >= nh3LowMin && value <= nh3LowMax) {
            return "Low";
        } else if (value >= nh3MediumMin && value <= nh3MediumMax) {
            return "Medium";
        } else if (value >= nh3HighMin && value <= nh3HighMax) {
            return "High";
        } else {
            return "Out of Range";
        }
    }

    //Determines CO2 air quality level based on measured value
    public String getCO2Level(int value) {
        if (value >= co2LowMin && value <= co2LowMax) {
            return "Low";
        } else if (value >= co2MediumMin && value <= co2MediumMax) {
            return "Medium";
        } else if (value >= co2HighMin && value <= co2HighMax) {
            return "High";
        } else {
            return "Out of Range";
        }
    }

    //Determines Dust air quality level based on measured value
    public String getDustLevel(int value) {
        if (value >= dustLowMin && value <= dustLowMax) {
            return "Low";
        } else if (value >= dustMediumMin && value <= dustMediumMax) {
            return "Medium";
        } else if (value >= dustHighMin && value <= dustHighMax) {
            return "High";
        } else {
            return "Out of Range";
        }
    }
    // Validates that ranges are ordered correctly and do not overlap
    private boolean areRangesValid(int lowMin, int lowMax,
                                   int mediumMin, int mediumMax,
                                   int highMin, int highMax) {

        //No negative values
        if (lowMin < 0 || lowMax < 0 ||
                mediumMin < 0 || mediumMax < 0 ||
                highMin < 0 || highMax < 0) {
            return false;
        }

        //Each range must be valid
        if (lowMin > lowMax ||
                mediumMin > mediumMax ||
                highMin > highMax) {
            return false;
        }

        // Increasing order (Low < Medium < High)
        if (!(lowMax < mediumMin && mediumMax < highMin)) {
            return false;
        }

        // Enforce continuous ranges (no gaps)
        if (lowMax + 1 != mediumMin) {
            return false;
        }

        if (mediumMax + 1 != highMin) {
            return false;
        }

        return true;
        }
}
