package com.concordia.qualiair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GaugeViewTest {

    @Test
    public void testAngleCalculation() {
        // Example: If 0 ppm is 0 degrees and 100 ppm is 180 degrees
        // Formula: (value / max) * 180
        double value = 50.0;
        double max = 100.0;
        float expectedAngle = 90.0f;

        // Replace this with your actual method name in GaugeView
        float actualAngle = (float) (value / max) * 180;

        assertEquals(expectedAngle, actualAngle, 0.1);
    }
}