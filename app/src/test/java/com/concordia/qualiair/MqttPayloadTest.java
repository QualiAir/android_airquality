package com.concordia.qualiair;

import org.junit.Test;
import static org.junit.Assert.*;

public class MqttPayloadTest {

    //NH3
    @Test
    public void testNH3StatusGood() {
        float value = 10f;
        assertTrue(value < 25f); // below caution
    }
    @Test
    public void testNH3StatusGoodFalse() {
        float value = 40f;
        assertFalse(value < 25f); // below caution
    }

    @Test
    public void testNH3StatusModerate() {
        float value = 28f;
        assertTrue(value >= 25f && value < 35f);
    }
    @Test
    public void testNH3StatusModerateFalseUp() {
        float value = 42f;
        assertFalse(value >= 25f && value < 35f);
    }
    @Test
    public void testNH3StatusModerateFalseDown() {
        float value = 24f;
        assertFalse(value >= 25f && value < 35f);
    }

    @Test
    public void testNH3StatusHigh() {
        float value = 40f;
        assertTrue(value >= 35f);
    }
    @Test
    public void testNH3StatusHighFalse() {
        float value = 12f;
        assertFalse(value >= 35f);
    }

    //H2S
    @Test
    public void testH2SStatusGood() {
        float value = 0.5f;
        assertTrue(value < 1f);
    }
    @Test
    public void testH2SStatusGoodFalse() {
        float value = 2.3f;
        assertFalse(value < 1f);
    }

    @Test
    public void testH2SStatusHigh() {
        float value = 6f;
        assertTrue(value >= 5f);
    }
    @Test
    public void testH2SStatusHighFalse() {
        float value = 3f;
        assertFalse(value >= 5f);
    }
    @Test
    public void testH2SStatusModerate() {
        float value = 3f;
        assertTrue(value >= 1f && value < 5f);
    }
    @Test
    public void testH2SStatusModerateFalseDown() {
        float value = 0.4f;
        assertFalse(value >= 1f && value < 5f);
    }
    @Test
    public void testH2SStatusModerateFalseUp() {
        float value = 5.4f;
        assertFalse(value >= 1f && value < 5f);
    }

    //PM25
    @Test
    public void testPM25StatusGood() {
        float value = 100f;
        assertTrue(value < 102f);
    }
    @Test
    public void testPM25StatusGoodFalse() {
        float value = 103f;
        assertFalse(value < 102f);
    }
    @Test
    public void testPM25StatusModerate() {
        float value = 150f;
        assertTrue(value >= 102f && value < 200f);
    }
    @Test
    public void testPM25StatusModerateFalseDown() {
        float value = 15f;
        assertFalse(value >= 102f && value < 200f);
    }
    @Test
    public void testPM25StatusModerateFalseUp() {
        float value = 560f;
        assertFalse(value >= 102f && value < 200f);
    }
    @Test
    public void testPM25StatusHigh() {
        float value = 250f;
        assertTrue(value >= 200f);
    }
    @Test
    public void testPM25StatusHighFalse() {
        float value = 190f;
        assertFalse(value >= 200f);
    }

}
