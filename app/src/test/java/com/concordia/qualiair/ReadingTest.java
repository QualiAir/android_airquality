package com.concordia.qualiair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.util.TimeZone;

public class ReadingTest {

    @Test
    public void testLevel_High() {
        Reading r = new Reading("2024-03-08T12:00:00Z", 85.0, null);
        assertEquals("High", r.getLevel());
    }

    @Test
    public void testLevel_Moderate() {
        Reading r = new Reading("2024-03-08T12:00:00Z", 50.0, null);
        assertEquals("Moderate", r.getLevel());
    }

    @Test
    public void testLevel_Low() {
        Reading r = new Reading("2024-03-08T12:00:00Z", 20.0, null);
        assertEquals("Low", r.getLevel());
    }

    @Test
    public void testFormattedValue() {
        Reading r = new Reading("2024-03-08T12:00:00Z", 35.9123, null);
        assertEquals("35.9", r.getFormattedValue());
    }
}