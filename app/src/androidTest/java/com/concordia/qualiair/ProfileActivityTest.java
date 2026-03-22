package com.concordia.qualiair;

import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    // Pre-load SharedPreferences before each test so the activity
    // finds real data when it calls loadAllPreferences() in onResume()
    @Before
    public void seedPreferences() {
        SharedPreferences prefs = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("QualiAirPreferences", android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putString("username", "Alice")
                .putString("email", "alice@example.com")
                .putFloat(ThresholdLevels.KEY_NH3_CAUTION,  25f)
                .putFloat(ThresholdLevels.KEY_NH3_ALARM,    35f)
                .putFloat(ThresholdLevels.KEY_H2S_CAUTION,  1f)
                .putFloat(ThresholdLevels.KEY_H2S_ALARM,    5f)
                .putFloat(ThresholdLevels.KEY_PM25_CAUTION, 12f)
                .putFloat(ThresholdLevels.KEY_PM25_ALARM,   35f)
                .apply();
    }

    // UI display tests

    @Test
    public void profileActivity_displaysUsername() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.lblName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Alice")));
        }
    }

    @Test
    public void profileActivity_displaysEmail() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.lblEmail))
                    .check(ViewAssertions.matches(ViewMatchers.withText("alice@example.com")));
        }
    }

    @Test
    public void profileActivity_emptyPrefs_showsDefaultName() {
        // Override with empty values
        InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("QualiAirPreferences", android.content.Context.MODE_PRIVATE)
                .edit().remove("username").apply();

        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.lblName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("User Name")));
        }
    }

    @Test
    public void profileActivity_displaysNh3CautionThreshold() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.lblNh3Caution))
                    .check(ViewAssertions.matches(ViewMatchers.withText("25.0 ppm")));
        }
    }

    @Test
    public void profileActivity_displaysPm25AlarmThreshold() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.lblPm25Alarm))
                    .check(ViewAssertions.matches(ViewMatchers.withText("35.0 µg/m³")));
        }
    }

    // Navigation test

    @Test
    public void settingsButton_navigatesToSettingsActivity() {
        try (ActivityScenario<ProfileActivity> scenario =
                     ActivityScenario.launch(ProfileActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.btnSettings))
                    .perform(androidx.test.espresso.action.ViewActions.click());

            // SettingsActivity's Save button should now be visible
            Espresso.onView(ViewMatchers.withId(R.id.btnSave))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}