package com.concordia.qualiair;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //setings button
        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            //settings code to come
        });

        //Logout button
        Button btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> {
            // Perform logout logic (clear prefs, etc.) and go to Login
            finishAffinity(); // Close all activities in stack
        });
    }

    // Back arrow click
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Returns to main
        return true;
    }
}