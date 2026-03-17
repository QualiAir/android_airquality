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
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected (@NonNull MenuItem item){

                    return true;
                } else if (itemId == R.id.nav_profile) { // Check your menu XML for the exact ID
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                } else if (itemId == R.id.nav_devices) {
                    startActivity(new Intent(MainActivity.this, DeviceActivity2.class));
                    return true;
                }
                //here we will add other navigation to other activity pages with else if
                return false;
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_devices) {
                startActivity(new Intent(MainActivity.this, DeviceActivity.class));
                return true;
            }
        });


        GaugeView gauge = findViewById(R.id.gauge_nh3);
        
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
        gauge.setValue(18);
    }
}