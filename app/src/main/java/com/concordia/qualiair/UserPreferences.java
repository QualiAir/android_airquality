package com.concordia.qualiair;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/** FUNC-5.1 (Design Preferences Data Model)
 * UserPreferences

 * Data model responsible for storing:
 * 1- User account information
 * 2- Air quality threshold ranges for NH3 and H2S gases and Dust

 *  Supports:
 * - Save the user information (username, email)
 * - Initializing default ranges  for NH3 and H2S gases and Dust
 * - Updating ranges later
 * - Determining air quality level based on a measured value
 */

/** FUNC-5.2  (Implement Local Storage Logic)
 * handle persistent storage using SharedPreferences
 * Store small configuration data
 * The data remains saved even after the application is closed or restarted

 * Supports:
 * - Saving user account information
 * - Saving NH3, H2S, and Dust threshold ranges
 * - Loading saved preferences when the app starts
 * - Keeping data available after the application restarts
 */

/** FUNC-5.3 (Update and overwrite preferences):
 * supports:
 * 1- update the threshold ranges after validating input
 * 2- The new values will overwrite the previous values and are saved using SharedPreferences
 */

/** FUNC-5.4 (Implement Auto-Load on App Start)
 * supports:
 * 1- Loading saved preferences when the app starts
 * 2- Retrieving NH3, H2S, and Dust thresholds from SharedPreferences
 * 3- Applying the loaded values when the app initializes
 * 4- Preserving user settings after the app restarts
 */

/** FUNC-5.5 (Test and Validate Preference Functionality)
 * supports:
 * 1- testing storing threshold values in SharedPreferences
 * 2- testing overwriting existing values with new ones
 * 3- testing loading saved preferences from storage
 * 4- verifying that saved values persist after updates
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

    // H2S Threshold Ranges
    private int h2sLowMin;
    private int h2sLowMax;

    private int h2sMediumMin;
    private int h2sMediumMax;

    private int h2sHighMin;
    private int h2sHighMax;

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
                           int h2sLowMin, int h2sLowMax,
                           int h2sMediumMin, int h2sMediumMax,
                           int h2sHighMin, int h2sHighMax,
                           int dustLowMin, int dustLowMax,
                           int dustMediumMin, int dustMediumMax,
                           int dustHighMin, int dustHighMax) {

        // Validate ranges first
        if (!areRangesValid(nh3LowMin, nh3LowMax,
                nh3MediumMin, nh3MediumMax,
                nh3HighMin, nh3HighMax)) {
            throw new IllegalArgumentException("Invalid NH3 ranges during initialization.");
        }

        if (!areRangesValid(h2sLowMin, h2sLowMax,
                h2sMediumMin, h2sMediumMax,
                h2sHighMin, h2sHighMax)) {
            throw new IllegalArgumentException("Invalid H2S ranges during initialization.");
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

        // H2S initialization
        this.h2sLowMin = h2sLowMin;
        this.h2sLowMax = h2sLowMax;
        this.h2sMediumMin = h2sMediumMin;
        this.h2sMediumMax = h2sMediumMax;
        this.h2sHighMin = h2sHighMin;
        this.h2sHighMax = h2sHighMax;

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

    // H2S keys
    private static final String KEY_H2S_LOW_MIN = "h2sLowMin";
    private static final String KEY_H2S_LOW_MAX = "h2sLowMax";
    private static final String KEY_H2S_MEDIUM_MIN = "h2sMediumMin";
    private static final String KEY_H2S_MEDIUM_MAX = "h2sMediumMax";
    private static final String KEY_H2S_HIGH_MIN = "h2sHighMin";
    private static final String KEY_H2S_HIGH_MAX = "h2sHighMax";


    // Dust Keys
    private static final String KEY_DUST_LOW_MIN = "dustLowMin";
    private static final String KEY_DUST_LOW_MAX = "dustLowMax";
    private static final String KEY_DUST_MEDIUM_MIN = "dustMediumMin";
    private static final String KEY_DUST_MEDIUM_MAX = "dustMediumMax";
    private static final String KEY_DUST_HIGH_MIN = "dustHighMin";
    private static final String KEY_DUST_HIGH_MAX = "dustHighMax";


    //Constructor of UserPreferences (connecting the class to Android storage)
    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //function to Save the values to SharedPreferences
    public void saveAllPreferences() {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);

        //NH3
        editor.putInt(KEY_NH3_HIGH_MIN, nh3HighMin);
        editor.putInt(KEY_NH3_HIGH_MAX, nh3HighMax);
        editor.putInt(KEY_NH3_MEDIUM_MIN, nh3MediumMin);
        editor.putInt(KEY_NH3_MEDIUM_MAX, nh3MediumMax);
        editor.putInt(KEY_NH3_LOW_MIN, nh3LowMin);
        editor.putInt(KEY_NH3_LOW_MAX, nh3LowMax);

        //H2S
        editor.putInt(KEY_H2S_LOW_MIN, h2sLowMin);
        editor.putInt(KEY_H2S_LOW_MAX, h2sLowMax);
        editor.putInt(KEY_H2S_MEDIUM_MIN, h2sMediumMin);
        editor.putInt(KEY_H2S_MEDIUM_MAX, h2sMediumMax);
        editor.putInt(KEY_H2S_HIGH_MIN, h2sHighMin);
        editor.putInt(KEY_H2S_HIGH_MAX, h2sHighMax);

        //Dust
        editor.putInt(KEY_DUST_LOW_MIN, dustLowMin);
        editor.putInt(KEY_DUST_LOW_MAX, dustLowMax);
        editor.putInt(KEY_DUST_MEDIUM_MIN, dustMediumMin);
        editor.putInt(KEY_DUST_MEDIUM_MAX, dustMediumMax);
        editor.putInt(KEY_DUST_HIGH_MIN, dustHighMin);
        editor.putInt(KEY_DUST_HIGH_MAX, dustHighMax);

        editor.apply();
    }

    // function to load all values from SharedPreferences
    // supports automatically loading stored user preferences when the application starts
    public void loadAllPreferences() {
        //user info
        username = sharedPreferences.getString(KEY_USERNAME, "");
        email = sharedPreferences.getString(KEY_EMAIL, "");

        //nh3
        nh3LowMin = sharedPreferences.getInt(KEY_NH3_LOW_MIN, 0);
        nh3LowMax = sharedPreferences.getInt(KEY_NH3_LOW_MAX, 0);
        nh3MediumMin = sharedPreferences.getInt(KEY_NH3_MEDIUM_MIN, 0);
        nh3MediumMax = sharedPreferences.getInt(KEY_NH3_MEDIUM_MAX, 0);
        nh3HighMin = sharedPreferences.getInt(KEY_NH3_HIGH_MIN, 0);
        nh3HighMax = sharedPreferences.getInt(KEY_NH3_HIGH_MAX, 0);

        //h2s
        h2sLowMin = sharedPreferences.getInt(KEY_H2S_LOW_MIN, 0);
        h2sLowMax = sharedPreferences.getInt(KEY_H2S_LOW_MAX, 0);
        h2sMediumMin = sharedPreferences.getInt(KEY_H2S_MEDIUM_MIN, 0);
        h2sMediumMax = sharedPreferences.getInt(KEY_H2S_MEDIUM_MAX, 0);
        h2sHighMin = sharedPreferences.getInt(KEY_H2S_HIGH_MIN, 0);
        h2sHighMax = sharedPreferences.getInt(KEY_H2S_HIGH_MAX, 0);

        //dust
        dustLowMin = sharedPreferences.getInt(KEY_DUST_LOW_MIN, 0);
        dustLowMax = sharedPreferences.getInt(KEY_DUST_LOW_MAX, 0);
        dustMediumMin = sharedPreferences.getInt(KEY_DUST_MEDIUM_MIN, 0);
        dustMediumMax = sharedPreferences.getInt(KEY_DUST_MEDIUM_MAX, 0);
        dustHighMin = sharedPreferences.getInt(KEY_DUST_HIGH_MIN, 0);
        dustHighMax = sharedPreferences.getInt(KEY_DUST_HIGH_MAX, 0);

    }


    // Getters
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getNh3LowMin() {
        return nh3LowMin;
    }

    public int getNh3LowMax() {
        return nh3LowMax;
    }

    public int getNh3MediumMin() {
        return nh3MediumMin;
    }

    public int getNh3MediumMax() {
        return nh3MediumMax;
    }

    public int getNh3HighMin() {
        return nh3HighMin;
    }

    public int getNh3HighMax() {
        return nh3HighMax;
    }

    public int getH2SLowMin() {
        return h2sLowMin;
    }

    public int getH2SLowMax() {
        return h2sLowMax;
    }

    public int getH2SMediumMin() {
        return h2sMediumMin;
    }

    public int getH2SMediumMax() {
        return h2sMediumMax;
    }

    public int getH2SHighMin() {
        return h2sHighMin;
    }

    public int getH2SHighMax() {
        return h2sHighMax;
    }


    public int getDustLowMin() {
        return dustLowMin;
    }

    public int getDustLowMax() {
        return dustLowMax;
    }

    public int getDustMediumMin() {
        return dustMediumMin;
    }

    public int getDustMediumMax() {
        return dustMediumMax;
    }

    public int getDustHighMin() {
        return dustHighMin;
    }

    public int getDustHighMax() {
        return dustHighMax;
    }


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

        // overwrite stored values
        saveAllPreferences();
    }

    //Updates all H2S threshold ranges at once.
    public void updateH2SRanges(int lowMin, int lowMax,
                                int mediumMin, int mediumMax,
                                int highMin, int highMax) {

        if (!areRangesValid(lowMin, lowMax, mediumMin, mediumMax, highMin, highMax)) {
            throw new IllegalArgumentException("Invalid H2S ranges: overlapping or incorrectly ordered values.");
        }

        this.h2sLowMin = lowMin;
        this.h2sLowMax = lowMax;
        this.h2sMediumMin = mediumMin;
        this.h2sMediumMax = mediumMax;
        this.h2sHighMin = highMin;
        this.h2sHighMax = highMax;

        // overwrite stored values
        saveAllPreferences();
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

        // overwrite stored values
        saveAllPreferences();
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

    //Determines H2S air quality level based on measured value
    public String getH2SLevel(int value) {
        if (value >= h2sLowMin && value <= h2sLowMax) {
            return "Low";
        } else if (value >= h2sMediumMin && value <= h2sMediumMax) {
            return "Medium";
        } else if (value >= h2sHighMin && value <= h2sHighMax) {
            return "High";
        } else {
            return "Out of Range";
        }
    }

    //Determines the Dust air quality level based on the measured value
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

    // helper function: printing current values (for testing)
    public void printAllPreferences(String tag) {

        Log.d("FUNC5_TEST", "----- " + tag + " -----");

        Log.d("FUNC5_TEST",
                "NH3: Low(" + nh3LowMin + "-" + nh3LowMax + ") "
                        + "Med(" + nh3MediumMin + "-" + nh3MediumMax + ") "
                        + "High(" + nh3HighMin + "-" + nh3HighMax + ")");

        Log.d("FUNC5_TEST",
                "H2S: Low(" + h2sLowMin + "-" + h2sLowMax + ") "
                        + "Med(" + h2sMediumMin + "-" + h2sMediumMax + ") "
                        + "High(" + h2sHighMin + "-" + h2sHighMax + ")");

        Log.d("FUNC5_TEST",
                "Dust: Low(" + dustLowMin + "-" + dustLowMax + ") "
                        + "Med(" + dustMediumMin + "-" + dustMediumMax + ") "
                        + "High(" + dustHighMin + "-" + dustHighMax + ")");
    }

    // FUNC-5.5: Test storing, overwriting, and loading preferences
    public void runFuncTest() {

        // A) STORE (save initial values)
        updateNH3Ranges(0, 10, 11, 20, 21, 30);
        updateH2SRanges(0, 10, 11, 20, 21, 50);
        updateDustRanges(0, 50, 51, 100, 101, 200);
        printAllPreferences("AFTER STORE");

        // B) OVERWRITE (change values and save again)
        updateNH3Ranges(0, 5, 6, 15, 16, 25);
        updateH2SRanges(0, 5, 6, 15, 16, 30);
        updateDustRanges(0, 40, 41, 90, 91, 150);
        printAllPreferences("AFTER OVERWRITE");

        // C) LOAD (read from SharedPreferences again)
        loadAllPreferences();
        printAllPreferences("AFTER LOAD");
    }


}

