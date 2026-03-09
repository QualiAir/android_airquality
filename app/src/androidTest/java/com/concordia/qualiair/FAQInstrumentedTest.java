package com.concordia.qualiair;

import androidx.lifecycle.Lifecycle;
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

import static org.junit.Assert.*;

/**
 * Instrumented Integration Tests for FAQActivity
 * Location: src/androidTest/java/com/concordia/qualiair/FAQInstrumentedTest.java
 *
 * Launches MainActivity first, then navigates to FAQActivity via the bottom nav.
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class FAQInstrumentedTest {

    private ActivityScenario<MainActivity> launchFromMain() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.nav_faq))
                .perform(ViewActions.click());
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
            Espresso.onView(ViewMatchers.withText("FAQ"))
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
            // FIX: use RecyclerViewActions to target position 0 specifically,
            // avoiding AmbiguousViewMatcherException when multiple rows share the same answer text
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .perform(RecyclerViewActions.scrollToPosition(0));

            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.rvFaq)),
                            ViewMatchers.withText("Email us at elec390team1@gmail.com."),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
                    .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));
        }
    }

    // -------------------------------------------------------------------------
    // Expand / collapse interaction
    // FIX: click the first RecyclerView item directly to avoid ambiguous matches
    // -------------------------------------------------------------------------

    @Test
    public void testClickQuestion_expandsAnswer() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            // Click position 0 in the RecyclerView
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

            // Check that an answer with VISIBLE visibility now exists
            Espresso.onView(Matchers.allOf(
                            ViewMatchers.withId(R.id.tvAnswer),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testClickQuestion_twice_collapsesAnswer() {
        try (ActivityScenario<MainActivity> scenario = launchFromMain()) {
            // First click — expand
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

            // Second click — collapse
            Espresso.onView(ViewMatchers.withId(R.id.rvFaq))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

            // Answer should be GONE again
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