package com.concordia.qualiair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1. Force the Home icon to be highlighted when this activity starts
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // We are already on Home. Just return true to keep the highlight.
                return true;
            } else if (itemId == R.id.nav_faq) {
                startActivity(new Intent(MainActivity.this, FAQActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_devices) {
                startActivity(new Intent(MainActivity.this, DeviceActivity.class));
                return true;
            }
            return false;
        });


        GaugeView gauge = findViewById(R.id.gauge_nh3);

        // get NH3 thresholds
        float nh3Min = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE).getFloat(ThresholdLevels.KEY_NH3_CAUTION, ThresholdLevels.NORMAL.nh3Caution);
        float nh3Max = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE).getFloat(ThresholdLevels.KEY_NH3_ALARM, ThresholdLevels.NORMAL.nh3Alarm);

        gauge.setMinValue(0);
        gauge.setMaxValue(nh3Max * 1.5f);//some overhead allowed, sensor can read above alarm value
        gauge.setValue(18);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}