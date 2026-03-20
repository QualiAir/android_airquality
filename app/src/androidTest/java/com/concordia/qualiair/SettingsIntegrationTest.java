package com.concordia.qualiair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class SettingsIntegrationTest {

    @Rule
    public ActivityScenarioRule<SettingsActivity> activityRule =
            new ActivityScenarioRule<>(SettingsActivity.class);

    @Test
    public void testSaveAndLoadThreshold() {

        // Type NH3 caution value
        onView(withId(R.id.etNh3Caution))
                .perform(clearText(), typeText("30"), closeSoftKeyboard());

        // Type NH3 alarm value
        onView(withId(R.id.etNh3Alarm))
                .perform(clearText(), typeText("40"), closeSoftKeyboard());

        // Click save
        onView(withId(R.id.btnSave))
                .perform(click());

        // Recreate activity (simulate app restart)
        activityRule.getScenario().recreate();

        // Check value persisted
        onView(withId(R.id.etNh3Caution))
                .check(matches(withText("30")));

        onView(withId(R.id.etNh3Alarm))
                .check(matches(withText("40")));
    }

    // test dropdown behaviour
    @Test
    public void testSelectSensitivePreset() {

        onView(withId(R.id.dropdownSensitivity))
                .perform(click());

        onView(withText("Sensitive"))
                .perform(click());

        onView(withId(R.id.dropdownSensitivity))
                .check(matches(withText("Sensitive")));
    }
}