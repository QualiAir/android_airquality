package com.concordia.qualiair;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented Integration Tests for FAQActivity
 * Location: src/androidTest/java/com/concordia/qualiair/FAQInstrumentedTest.java
 *
 * Launches MainActivity first, then navigates to FAQActivity via the bottom nav.
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class FAQUnitTest {

    private ActivityScenario<MainActivity> launchFromMain() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.nav_faq))
                .perform(ViewActions.click());
        // Wait for FAQActivity to be fully resumed before any assertions
        Espresso.onIdle();
        return scenario;
    }

    // -------------------------------------------------------------------------
    // Layout rendering
    // -------------------------------------------------------------------------

    @Test
    public void testToolbarIsDisplayed() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.onView(ViewMatchers.withId(R.id.toolbar))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testToolbarShowsCorrectTitle() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            // Match the toolbar title text view specifically to avoid matching
            // other views that might contain "FAQ"
            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withText("FAQ"),
                            ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    // -------------------------------------------------------------------------
    // FAQ list content
    // -------------------------------------------------------------------------

    @Test
    public void testFirstQuestionIsVisible() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.onView(ViewMatchers.withText("Support contact?"))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testAnswerIsHiddenByDefault() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            // Verify no tvAnswer is currently visible
            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(ViewAssertions.doesNotExist());
        }
    }

    // -------------------------------------------------------------------------
    // Expand / collapse interaction
    // -------------------------------------------------------------------------

    @Test
    public void testClickQuestion_expandsAnswer() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testClickQuestion_twice_collapsesAnswer() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            // First click — expand by clicking the question text directly
            Espresso.onView(ViewMatchers.withText("Support contact?"))
                    .perform(ViewActions.click());

            // Verify it expanded
            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            // Second click — collapse by clicking the question text again
            Espresso.onView(ViewMatchers.withText("Support contact?"))
                    .perform(ViewActions.click());

            // After collapse, tvAnswer should be GONE
            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
                    .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));
        }
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @Test
    public void testBackPress_returnsToMainActivity() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.pressBack();
            Espresso.onView(ViewMatchers.withId(R.id.bottom_navigation))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testToolbarBackArrow_returnsToMainActivity() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
                    .perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.bottom_navigation))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}