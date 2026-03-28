package com.concordia.qualiair;

import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Before
    public void seedPreferences() {
        SharedPreferences prefs = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("QualiAirPreferences", android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putString("username", "Alice")
                .putString("email", "alice@example.com")
                .putString(ThresholdLevels.KEY_SENSITIVITY, "Normal")
                .apply();
    }

    // UI display tests

    @Test
    public void settingsActivity_preloadsUsername() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.etName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Alice")));
        }
    }

    @Test
    public void settingsActivity_preloadsEmail() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                    .check(ViewAssertions.matches(ViewMatchers.withText("alice@example.com")));
        }
    }

    @Test
    public void settingsActivity_preloadsSensitivityDropdown() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.dropdownSensitivity))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Normal")));
        }
    }

    // Validation tests

    @Test
    public void saveButton_emptyName_showsError() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.etName))
                    .perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.btnSave))
                    .perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.etName))
                    .check(ViewAssertions.matches(
                            ViewMatchers.hasErrorText("Name cannot be empty")));
        }
    }

    @Test
    public void saveButton_invalidEmail_showsError() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                    .perform(ViewActions.clearText(),
                            ViewActions.typeText("not-an-email"),
                            ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.btnSave))
                    .perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                    .check(ViewAssertions.matches(
                            ViewMatchers.hasErrorText("Enter a valid email address")));
        }
    }

    // SharedPreferences save test

    @Test
    public void saveButton_validInputs_persistsToSharedPreferences() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.etName))
                    .perform(ViewActions.clearText(),
                            ViewActions.typeText("Bob"),
                            ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                    .perform(ViewActions.clearText(),
                            ViewActions.typeText("bob@example.com"),
                            ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.btnSave))
                    .perform(ViewActions.click());

            SharedPreferences prefs = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getSharedPreferences("QualiAirPreferences", android.content.Context.MODE_PRIVATE);
            assertEquals("Bob", prefs.getString("username", ""));
            assertEquals("bob@example.com", prefs.getString("email", ""));
        }
    }
}
