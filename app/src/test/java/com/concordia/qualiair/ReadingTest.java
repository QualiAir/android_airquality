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
    public void testGetTime_MontrealConversion() {
        // Get today's date in UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String today = sdf.format(new Date());

        // Create a reading for 20:24 UTC
        String timeString = today + "T20:24:00Z";
        Reading reading = new Reading(timeString, 10.0, "Low");

        //CALCULATE THE EXPECTED TIME
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.US);
        outputFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 24);
        String expectedTime = outputFormat.format(cal.getTime());

        // Now we compare the code's output to the calculated Montreal time
        assertEquals(expectedTime, reading.getTime());
    }

    @Test
    public void testGetLevel_HighThreshold() {
        // Test: 85.0ppm should be "High" based on ACGIH standards
        Reading reading = new Reading("2024-03-06T12:00:00Z", 85.0, null);
        assertEquals("High", reading.getLevel());
    }
}