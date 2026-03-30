package com.concordia.qualiair;

public class AirQualityMonitor {

    public enum StatusLevel {
        GOOD,
        CAUTION,
        ALARM
    }
    //initialize latest value from InfluxDB
    private float latestNH3 = 0f;
    private float latestH2S = 0f;
    private float latestPM25 = 0f;

    // Thresholds
    // NH3 (ppm)
    // Thresholds (non-final so they can be updated from settings)
// NH3 (ppm)
    private float NH3_CAUTION = 25f;
    private float NH3_ALARM = 35f;
    // H2S (ppm)
    private float H2S_CAUTION = 1f;
    private float H2S_ALARM = 5f;
    // PM2.5 (µg/m³)
    private float PM25_CAUTION = 12f;
    private float PM25_ALARM = 35f;

    public void updateThresholds(ThresholdLevels.Thresholds t) {
        NH3_CAUTION  = t.nh3Caution;
        NH3_ALARM    = t.nh3Alarm;
        H2S_CAUTION  = t.h2sCaution;
        H2S_ALARM    = t.h2sAlarm;
        PM25_CAUTION = t.pm25Caution;
        PM25_ALARM   = t.pm25Alarm;
    }

    // Called from MainActivity when MQTT message arrives
    public void update(float nh3, float h2s, float pm25) {
        latestNH3  = nh3;
        latestH2S  = h2s;
        latestPM25 = pm25;
    }

    public float getLatest(String sensor) {
        switch (sensor) {
            case "nh3":
                return latestNH3;
            case "h2s":
                return latestH2S;
            case "pm25":
                return latestPM25;
            default:
                return 0f;
        }
    }

    public StatusLevel getStatus(String sensor) {
        switch (sensor) {
            case "nh3":
                return classify(latestNH3,  NH3_CAUTION,  NH3_ALARM);
            case "h2s":
                return classify(latestH2S,  H2S_CAUTION,  H2S_ALARM);
            case "pm25":
                return classify(latestPM25, PM25_CAUTION, PM25_ALARM);
            default:
                return StatusLevel.GOOD;
        }
    }

    private StatusLevel classify(float value, float caution, float alarm) {
        if (value >= alarm)   {
            return StatusLevel.ALARM;
        }
        if (value >= caution){
            return StatusLevel.CAUTION;
        }
        return StatusLevel.GOOD;
    }
    public float getCautionThreshold(String sensor) {
        switch (sensor) {
            case "nh3":  return NH3_CAUTION;
            case "h2s":  return H2S_CAUTION;
            case "pm25": return PM25_CAUTION;
            default:     return 0f;
        }
    }

    public float getAlarmThreshold(String sensor) {
        switch (sensor) {
            case "nh3":  return NH3_ALARM;
            case "h2s":  return H2S_ALARM;
            case "pm25": return PM25_ALARM;
            default:     return 0f;
        }
    }
    public boolean isAnyCaution() {
            return getStatus("nh3")  != StatusLevel.GOOD
                || getStatus("h2s")  != StatusLevel.GOOD
                || getStatus("pm25") != StatusLevel.GOOD;
    }
    public boolean isAnyAlarm() {
        return getStatus("nh3")  == StatusLevel.ALARM
                || getStatus("h2s")  == StatusLevel.ALARM
                || getStatus("pm25") == StatusLevel.ALARM;
    }


}
