package com.concordia.qualiair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class HistoryResponseTest {

    @Test
    public void testReadingsList_StoresCorrectly() {
        List<Reading> mockReadings = new ArrayList<>();
        mockReadings.add(new Reading("2024-03-08T12:00:00Z", 15.5, "Low"));
        mockReadings.add(new Reading("2024-03-08T12:00:00Z", 85.0, "High"));
        assertEquals(2, mockReadings.size());
    }

    @Test
    public void testEmptyList() {
        List<Reading> emptyList = new ArrayList<>();
        assertEquals(0, emptyList.size());
    }

    @Test
    public void testReading_ValueStoredCorrectly() {
        Reading r = new Reading("2024-03-08T12:00:00Z", 15.5, "Low");
        assertEquals(15.5, r.getValue(), 0.001);
        assertEquals("Low", r.getLevel());
    }

}