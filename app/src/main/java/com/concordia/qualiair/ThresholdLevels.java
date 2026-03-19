package com.concordia.qualiair;

public class ThresholdLevels {

    public static final String KEY_NH3_CAUTION  = "custom_nh3_caution";
    public static final String KEY_NH3_ALARM    = "custom_nh3_alarm";
    public static final String KEY_H2S_CAUTION  = "custom_h2s_caution";
    public static final String KEY_H2S_ALARM    = "custom_h2s_alarm";
    public static final String KEY_PM25_CAUTION = "custom_pm25_caution";
    public static final String KEY_PM25_ALARM   = "custom_pm25_alarm";
    public static final String KEY_SENSITIVITY  = "sensitivity_preset";

    public static class Thresholds {
        public final float nh3Caution, nh3Alarm;
        public final float h2sCaution, h2sAlarm;
        public final float pm25Caution, pm25Alarm;

        public Thresholds(float nh3Caution, float nh3Alarm,
                          float h2sCaution, float h2sAlarm,
                          float pm25Caution, float pm25Alarm) {
            this.nh3Caution  = nh3Caution;
            this.nh3Alarm    = nh3Alarm;
            this.h2sCaution  = h2sCaution;
            this.h2sAlarm    = h2sAlarm;
            this.pm25Caution = pm25Caution;
            this.pm25Alarm   = pm25Alarm;
        }
    }

    // EPA / NIOSH standard values (matches HistoryActivity constants)
    public static final Thresholds NORMAL = new Thresholds(
            25f, 35f,   // NH3 ppm
            1f,  5f,    // H2S ppm
            12f, 35f // PM2.5 (µg/m³)
    );

    // Tighter thresholds for sensitive individuals
    public static final Thresholds SENSITIVE = new Thresholds(
            10f, 20f,   // NH3
            0.5f, 2f,   // H2S
            9f, 20f    // PM2.5
    );

    public static Thresholds fromPreference(String pref,
                                            float customNh3Caution, float customNh3Alarm,
                                            float customH2sCaution, float customH2sAlarm,
                                            float customPm25Caution, float customPm25Alarm) {
        switch (pref) {
            case "Sensitive": return SENSITIVE;
            case "Custom":    return new Thresholds(
                    customNh3Caution, customNh3Alarm,
                    customH2sCaution, customH2sAlarm,
                    customPm25Caution, customPm25Alarm);
            default:          return NORMAL;
        }
    }
}