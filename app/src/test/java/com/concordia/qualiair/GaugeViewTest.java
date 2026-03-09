package com.concordia.qualiair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GaugeViewTest {

    @Test
    public void testAngleCalculation_atMax() {
        float percentage = (50f - 0f) / (50f - 0f); // max value
        float angle = 180 + percentage * 180;
        assertEquals(360f, angle, 0.1);
    }

    @Test
    public void testAngleCalculation_atMin() {
        float percentage = (0f - 0f) / (50f - 0f); // min value
        float angle = 180 + percentage * 180;
        assertEquals(180f, angle, 0.1);
    }

    @Test
    public void testAngleCalculation_atMiddle() {
        float percentage = (25f - 0f) / (50f - 0f); // middle value
        float angle = 180 + percentage * 180;
        assertEquals(270f, angle, 0.1);
    }
}