package com.concordia.qualiair;

public class ThresholdUpdateRequest {
    String device_id;
    float caution_nh3, alert_nh3;
    float caution_h2s, alert_h2s;
    float caution_dust, alert_dust;

    public ThresholdUpdateRequest(String device_id, ThresholdLevels.Thresholds t) {
        this.device_id = device_id;
        this.caution_nh3 = t.nh3Caution;
        this.alert_nh3 = t.nh3Alarm;
        this.caution_h2s = t.h2sCaution;
        this.alert_h2s = t.h2sAlarm;
        this.caution_dust = t.pm25Caution;
        this.alert_dust = t.pm25Alarm;
    }
}
