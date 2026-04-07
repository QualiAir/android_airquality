package com.concordia.qualiair;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.concordia.qualiair.Device.DeviceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    private static final String TAG = "NavigationHelper";

    public static void setupBottomNavigation(AppCompatActivity activity, int currentItemId) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        // 1. Force the correct visual state without triggering the listener
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(currentItemId);

        // 2. Setup the selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int clickedId = item.getItemId();
            
            // STRICT NADA: If user clicks the icon they are already on, do nothing.
            if (clickedId == currentItemId) {
                return true; 
            }

            Intent intent = null;
            if (clickedId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (clickedId == R.id.nav_history) {
                intent = new Intent(activity, HistoryActivity.class);
            } else if (clickedId == R.id.nav_devices) {
                intent = new Intent(activity, DeviceActivity.class);
            } else if (clickedId == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            } else if (clickedId == R.id.nav_faq) {
                intent = new Intent(activity, FAQActivity.class);
            }

            if (intent != null) {
                // Reuse existing activity instances and remove animations for "Instagram-like" feel
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0); 
                return true;
            }
            return false;
        });

        // 3. Explicitly kill re-selection logic
        bottomNavigationView.setOnItemReselectedListener(item -> {
            // Do absolutely nothing on double-tap
        });
    }
}
