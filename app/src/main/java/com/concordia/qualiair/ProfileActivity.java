package com.concordia.qualiair;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView lblName, lblEmail;
    private TextView lblNh3Caution, lblNh3Alarm;
    private TextView lblH2sCaution, lblH2sAlarm;
    private TextView lblPm25Caution, lblPm25Alarm;
    private ShapeableImageView profilePic;
    private UserPreferences userPreferences;
    private TextView ModeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        ModeValue = findViewById(R.id.lblModeValue);

        // back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }

        lblName        = findViewById(R.id.lblName);
        lblEmail       = findViewById(R.id.lblEmail);
        profilePic     = findViewById(R.id.profilePic);
        lblNh3Caution  = findViewById(R.id.lblNh3Caution);
        lblNh3Alarm    = findViewById(R.id.lblNh3Alarm);
        lblH2sCaution  = findViewById(R.id.lblH2sCaution);
        lblH2sAlarm    = findViewById(R.id.lblH2sAlarm);
        lblPm25Caution = findViewById(R.id.lblPm25Caution);
        lblPm25Alarm   = findViewById(R.id.lblPm25Alarm);
        userPreferences = new UserPreferences(this);

        //settings button
        MaterialButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        //Logout button
        MaterialButton btnDefaultSettings = findViewById(R.id.btnDefaultSettings);
        btnDefaultSettings.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Reset Settings")
                    .setMessage("This will restore all thresholds to their default values. Continue?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putFloat(ThresholdLevels.KEY_NH3_CAUTION,  ThresholdLevels.NORMAL.nh3Caution);
                        editor.putFloat(ThresholdLevels.KEY_NH3_ALARM,    ThresholdLevels.NORMAL.nh3Alarm);
                        editor.putFloat(ThresholdLevels.KEY_H2S_CAUTION,  ThresholdLevels.NORMAL.h2sCaution);
                        editor.putFloat(ThresholdLevels.KEY_H2S_ALARM,    ThresholdLevels.NORMAL.h2sAlarm);
                        editor.putFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution);
                        editor.putFloat(ThresholdLevels.KEY_PM25_ALARM,   ThresholdLevels.NORMAL.pm25Alarm);
                        editor.putString(ThresholdLevels.KEY_SENSITIVITY, "Normal");
                        editor.apply();

                        // Refresh the displayed values immediately
                        onResume();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                return true; // already here
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_devices) {
                startActivity(new Intent(ProfileActivity.this, DeviceActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(ProfileActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_faq) {
                startActivity(new Intent(ProfileActivity.this, FAQActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        userPreferences.loadAllPreferences();
        String name  = userPreferences.getUsername();
        String email = userPreferences.getEmail();
        lblName.setText(name.isEmpty()   ? "User Name"        : name);
        lblEmail.setText(email.isEmpty() ? "user@example.com" : email);

        String savedUri = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE)
                .getString("profile_pic_uri", null);
        if (savedUri != null) {
            try {
                Uri uri = Uri.parse(savedUri);
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                profilePic.setImageURI(uri);
            } catch (Exception e) {
                // URI permission expired — clear it and show default
                getSharedPreferences("QualiAirPreferences", MODE_PRIVATE)
                        .edit().remove("profile_pic_uri").apply();
                profilePic.setImageResource(R.drawable.temp_profile);
            }
        }

        SharedPreferences prefs = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE);
        float nh3C  = prefs.getFloat(ThresholdLevels.KEY_NH3_CAUTION,  ThresholdLevels.NORMAL.nh3Caution);
        float nh3A  = prefs.getFloat(ThresholdLevels.KEY_NH3_ALARM,    ThresholdLevels.NORMAL.nh3Alarm);
        float h2sC  = prefs.getFloat(ThresholdLevels.KEY_H2S_CAUTION,  ThresholdLevels.NORMAL.h2sCaution);
        float h2sA  = prefs.getFloat(ThresholdLevels.KEY_H2S_ALARM,    ThresholdLevels.NORMAL.h2sAlarm);
        float pm25C = prefs.getFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution);
        float pm25A = prefs.getFloat(ThresholdLevels.KEY_PM25_ALARM,   ThresholdLevels.NORMAL.pm25Alarm);

        lblNh3Caution.setText(nh3C  + " ppm");
        lblNh3Alarm.setText(nh3A    + " ppm");
        lblH2sCaution.setText(h2sC  + " ppm");
        lblH2sAlarm.setText(h2sA    + " ppm");
        lblPm25Caution.setText(pm25C + " µg/m³");
        lblPm25Alarm.setText(pm25A   + " µg/m³");

       String mode = prefs.getString(ThresholdLevels.KEY_SENSITIVITY, "Normal");
        ModeValue.setText(" — " + mode);
    }

    // Back arrow click
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Returns to main
        return true;
    }
}