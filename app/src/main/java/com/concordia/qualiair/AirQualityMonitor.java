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
    private static final float NH3_CAUTION = 25f;
    private static final float NH3_ALARM = 35f;
    // H2S (ppm)
    private static final float H2S_CAUTION = 1f;
    private static final float H2S_ALARM = 5f;
    // PM2.5 (µg/m³)
    private static final float PM25_CAUTION = 102f;
    private static final float PM25_ALARM = 200f;

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

}
