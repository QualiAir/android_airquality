package com.concordia.qualiair;

import org.junit.Test;
import static org.junit.Assert.*;

public class ThresholdLevelsTest {

    @Test
    public void normalPreset_hasCorrectNh3Values() {
        assertEquals(25f, ThresholdLevels.NORMAL.nh3Caution, 0.01f);
        assertEquals(35f, ThresholdLevels.NORMAL.nh3Alarm,   0.01f);
    }

    @Test
    public void normalPreset_hasCorrectH2sValues() {
        assertEquals(1f, ThresholdLevels.NORMAL.h2sCaution, 0.01f);
        assertEquals(5f, ThresholdLevels.NORMAL.h2sAlarm,   0.01f);
    }

    @Test
    public void normalPreset_hasCorrectPm25Values() {
        assertEquals(12f, ThresholdLevels.NORMAL.pm25Caution, 0.01f);
        assertEquals(35f, ThresholdLevels.NORMAL.pm25Alarm,   0.01f);
    }

    @Test
    public void sensitivePreset_isTighterThanNormal() {
        assertTrue(ThresholdLevels.SENSITIVE.nh3Caution  < ThresholdLevels.NORMAL.nh3Caution);
        assertTrue(ThresholdLevels.SENSITIVE.h2sCaution  < ThresholdLevels.NORMAL.h2sCaution);
        assertTrue(ThresholdLevels.SENSITIVE.pm25Caution < ThresholdLevels.NORMAL.pm25Caution);
    }

    @Test
    public void fromPreference_normal_returnsNormalThresholds() {
        ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference("Normal", 0,0,0,0,0,0);
        assertEquals(ThresholdLevels.NORMAL.nh3Alarm, t.nh3Alarm, 0.01f);
    }

    @Test
    public void fromPreference_sensitive_returnsSensitiveThresholds() {
        ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference("Sensitive", 0,0,0,0,0,0);
        assertEquals(ThresholdLevels.SENSITIVE.nh3Caution, t.nh3Caution, 0.01f);
    }

    @Test
    public void fromPreference_custom_returnsProvidedValues() {
        ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference("Custom", 5f, 10f, 0.5f, 2f, 8f, 20f);
        assertEquals(5f,  t.nh3Caution,  0.01f);
        assertEquals(10f, t.nh3Alarm,    0.01f);
        assertEquals(0.5f, t.h2sCaution, 0.01f);
        assertEquals(2f,  t.h2sAlarm,    0.01f);
        assertEquals(8f,  t.pm25Caution, 0.01f);
        assertEquals(20f, t.pm25Alarm,   0.01f);
    }

    @Test
    public void allPresets_cautionIsAlwaysLessThanAlarm() {
        assertTrue(ThresholdLevels.NORMAL.nh3Caution    < ThresholdLevels.NORMAL.nh3Alarm);
        assertTrue(ThresholdLevels.NORMAL.h2sCaution    < ThresholdLevels.NORMAL.h2sAlarm);
        assertTrue(ThresholdLevels.NORMAL.pm25Caution   < ThresholdLevels.NORMAL.pm25Alarm);
        assertTrue(ThresholdLevels.SENSITIVE.nh3Caution < ThresholdLevels.SENSITIVE.nh3Alarm);
        assertTrue(ThresholdLevels.SENSITIVE.h2sCaution < ThresholdLevels.SENSITIVE.h2sAlarm);
        assertTrue(ThresholdLevels.SENSITIVE.pm25Caution< ThresholdLevels.SENSITIVE.pm25Alarm);
    }
}