package com.concordia.qualiair;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;

import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GaugeView gauge = findViewById(R.id.gauge_nh3);
        gauge.setMinValue(0);
        gauge.setMaxValue(50);
        gauge.setValue(18);

        // create UserPreferences
        UserPreferences prefs = new UserPreferences(this);

        // load saved values -
        prefs.loadAllPreferences();

        // get NH3 thresholds
        int nh3Min = prefs.getNh3LowMin();
        int nh3Max = prefs.getNh3HighMax();

        // if there is no saved value for NH3, use the defaults
        if (nh3Max == 0) {
            gauge.setMinValue(0);
            gauge.setMaxValue(50);
        } else {
            gauge.setMinValue(nh3Min);
            gauge.setMaxValue(nh3Max);
        }

    }
}