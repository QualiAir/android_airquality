package com.concordia.qualiair;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntegrationTest {

    // This launches MainActivity before the test starts
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testUIComponentsVisible() {
        // Verify the Ammonia Gauge is displayed on the screen
        onView(withId(R.id.gauge_main)).check(matches(isDisplayed()));

        // Verify the Navigation Bar is present
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToHistory() {
        // Click the History icon in the navigation bar
        onView(withId(R.id.nav_history)).perform(click());


        onView(withId(R.id.line_chart)).check(matches(isDisplayed()));
    }
    @Test
    public void testNavigationToFaq() {
        // Click the History icon in the navigation bar
        onView(withId(R.id.nav_faq)).perform(click());


        onView(withId(R.id.rvFaq)).check(matches(isDisplayed()));
    }
    @Test
    public void testNavigationToProfile() {
        // Click the Profile icon in the navigation bar
        onView(withId(R.id.nav_profile)).perform(click());


        onView(withId(R.id.lblName)).check(matches(isDisplayed()));
    }
}