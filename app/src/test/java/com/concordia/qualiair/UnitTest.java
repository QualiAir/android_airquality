package com.concordia.qualiair;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    //Test that the UserPreferences class exists
    @Test
    public void userPreferences_classExists() {
        assertNotNull(UserPreferences.class);
    }

    //test that the UserPreferences class name is correct
    @Test
    public void userPreferences_classNameCorrect() {
        assertEquals("UserPreferences", UserPreferences.class.getSimpleName());
    }

    @Test
    public void thresholdLevels_normalValuesCorrect() {
        ThresholdLevels.Thresholds normal = ThresholdLevels.NORMAL;

        assertEquals(25f, normal.nh3Caution, 0.01f);
        assertEquals(35f, normal.nh3Alarm, 0.01f);

        assertEquals(1f, normal.h2sCaution, 0.01f);
        assertEquals(5f, normal.h2sAlarm, 0.01f);

        assertEquals(12f, normal.pm25Caution, 0.01f);
        assertEquals(35f, normal.pm25Alarm, 0.01f);
    }

    @Test
    public void thresholdLevels_sensitiveValuesCorrect() {
        ThresholdLevels.Thresholds sensitive = ThresholdLevels.SENSITIVE;

        assertEquals(10f, sensitive.nh3Caution, 0.01f);
        assertEquals(20f, sensitive.nh3Alarm, 0.01f);

        assertEquals(0.5f, sensitive.h2sCaution, 0.01f);
        assertEquals(2f, sensitive.h2sAlarm, 0.01f);

        assertEquals(9f, sensitive.pm25Caution, 0.01f);
        assertEquals(20f, sensitive.pm25Alarm, 0.01f);
    }

    @Test
    public void fromPreference_returnsSensitive() {
        ThresholdLevels.Thresholds result =
                ThresholdLevels.fromPreference("Sensitive", 0,0,0,0,0,0);

        assertEquals(ThresholdLevels.SENSITIVE.nh3Caution, result.nh3Caution, 0.01f);
    }

    @Test
    public void fromPreference_returnsCustomValues() {
        ThresholdLevels.Thresholds result =
                ThresholdLevels.fromPreference("Custom",
                        30f, 40f,
                        2f, 6f,
                        15f, 50f);

        assertEquals(30f, result.nh3Caution, 0.01f);
        assertEquals(40f, result.nh3Alarm, 0.01f);

        assertEquals(2f, result.h2sCaution, 0.01f);
        assertEquals(6f, result.h2sAlarm, 0.01f);

        assertEquals(15f, result.pm25Caution, 0.01f);
        assertEquals(50f, result.pm25Alarm, 0.01f);
    }

    @Test
    public void fromPreference_defaultReturnsNormal() {
        ThresholdLevels.Thresholds result =
                ThresholdLevels.fromPreference("AnythingElse", 0,0,0,0,0,0);

        assertEquals(ThresholdLevels.NORMAL.nh3Caution, result.nh3Caution, 0.01f);
    }


}