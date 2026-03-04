package com.concordia.qualiair;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


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

                int itemId = item.getItemId();
                if (itemId == R.id.nav_history) {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                }
                //here we will add other navigation to other activity pages with else if
                return false;
            }
        });


        GaugeView gauge = findViewById(R.id.gauge_nh3);
        
        // create UserPreferences
        UserPreferences prefs = new UserPreferences(this);

        // load saved values
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