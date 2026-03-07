package com.concordia.qualiair;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class Reading {
    private String time;
    private double value; // Changed to double to handle decimals like 0.5

    private String level;

    public Reading() {}

    // Constructor
    public Reading(String time, double value, String level) {
        this.time = time;
        this.value = value;
        this.level = level;
    }


    public String getTime() {
        if (time == null || time.isEmpty()) return "";

        try {
            // DYNAMIC PARSING: Handle "Z", "+00:00", and varied decimal lengths
            // We standardize the timezone format to "+0000" for the parser
            String cleanTime = time.replace("Z", "+0000")
                    .replace("+00:00", "+0000")
                    .replaceAll("(\\.\\d{3})\\d+", "$1"); // Trim micros to 3 digits

            SimpleDateFormat inputFormat;
            if (cleanTime.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
            }

            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(cleanTime);

            // 2. SMART LABELING: Decide format based on data age
            SimpleDateFormat outputFormat;
            long diff = Math.abs(System.currentTimeMillis() - date.getTime());

            if (diff > 24 * 60 * 60 * 1000) {
                // Weekly/Monthly: Show "MMM dd" (Mar 06) so labels don't overlap
                outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            } else {
                // Last Hour/Daily: Show local time "15:33"
                outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            }

            // 3. MONTREAL TIMEZONE
            outputFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
            return outputFormat.format(date);

        } catch (Exception e) {
            // Fallback: If math fails, show the raw UTC hours (20:54)
            return (time.length() > 16) ? time.substring(11, 16) : time;
        }

    }

    public double getValue() {

        return value;

    }
    public String getFormattedValue() {
        // This formats the double to 1 decimal place (e.g., 35.9)
        return String.format("%.1f", value);
    }
    public String getLevel() {

        if (level != null) return level;
        if (value >= 80) return "High";
        if (value >= 40) return "Moderate";
        return "Low";
    }

}
