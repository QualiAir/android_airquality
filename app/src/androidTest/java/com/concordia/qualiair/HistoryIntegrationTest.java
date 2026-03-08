package com.concordia.qualiair;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HistoryIntegrationTest {

    @Rule
    public ActivityScenarioRule<HistoryActivity> activityRule =
            new ActivityScenarioRule<>(HistoryActivity.class);
    @Test
    public void testSpinnerIsVisible() {
        onView(withId(R.id.time_range_spinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testGasButtonsAreVisible() {
        onView(withId(R.id.button_nh3)).check(matches(isDisplayed()));
        onView(withId(R.id.button_co2)).check(matches(isDisplayed()));
        onView(withId(R.id.button_pm25)).check(matches(isDisplayed()));
    }

    @Test
    public void testHistoryTitleIsVisible() {
        onView(withId(R.id.textViewHistory)).check(matches(isDisplayed()));
    }
}