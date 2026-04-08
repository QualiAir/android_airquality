package com.concordia.qualiair;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class Reading {
    private String time;
    private double value; // Changed to double to handle decimals like 0.5
    private String level;
    private String sensor;
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
        String cleanTime = time.replace("Z", "+0000")
                .replace("+00:00", "+0000")
                .replaceAll("(\\.\\d{3})\\d+", "$1");

        SimpleDateFormat inputFormat;
        if (cleanTime.contains(".")) {
            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        } else {
            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        }

        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = inputFormat.parse(cleanTime);

        // Always show both date and time
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd\nHH:mm", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        return outputFormat.format(date);

    } catch (Exception e) {
        return (time.length() > 16) ? time.substring(11, 16) : time;
    }
}

    public double getValue() {

        return value;

    }
    public String getFormattedValue() {
        if("dust".equals(sensor)){
            return String.format("%.4f", value);//dust has 4 significant figure
        }
        return String.format("%.4f", value);//2 significant figure for nh3/h2s
    }
    public String getLevel() {

        if (level != null) return level;
        if (value >= 80) return "High";
        if (value >= 40) return "Moderate";
        return "Low";
    }

    public void setLevel(String level) {

        this.level = level;

    }
    public void setSensor(String sensor){
        this.sensor=sensor;
    }
}
