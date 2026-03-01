package com.concordia.qualiair;
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


    //Constructor
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
