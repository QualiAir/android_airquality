package com.concordia.qualiair;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;


import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class UserPreferencesTest {

    private UserPreferences userPreferences;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        userPreferences = new UserPreferences(context);
    }

    @Test
    public void defaultUsername_isEmpty() {
        userPreferences.loadAllPreferences();
        assertEquals("", userPreferences.getUsername());
    }

    @Test
    public void defaultEmail_isEmpty() {
        userPreferences.loadAllPreferences();
        assertEquals("", userPreferences.getEmail());
    }

    @Test
    public void saveAndLoad_username_persistsCorrectly() {
        userPreferences.setUsername("Alice");
        userPreferences.saveAllPreferences();

        UserPreferences fresh = new UserPreferences(context);
        fresh.loadAllPreferences();
        assertEquals("Alice", fresh.getUsername());
    }

    @Test
    public void saveAndLoad_email_persistsCorrectly() {
        userPreferences.setEmail("alice@example.com");
        userPreferences.saveAllPreferences();

        UserPreferences fresh = new UserPreferences(context);
        fresh.loadAllPreferences();
        assertEquals("alice@example.com", fresh.getEmail());
    }

    @Test
    public void setUsername_doesNotPersistUntilSave() {
        userPreferences.setUsername("Bob");
        // Don't call saveAllPreferences()

        UserPreferences fresh = new UserPreferences(context);
        fresh.loadAllPreferences();
        assertNotEquals("Bob", fresh.getUsername());
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   