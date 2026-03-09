package com.concordia.qualiair;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

public class UserPreferencesIntegrationTest {

    @Test
    public void testUserPreferencesIntegration() {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // FUNC-5.1 : Save user information
        editor.putString("username", "test_user");

        // FUNC-5.2 : Save threshold values for sensors
        editor.putInt("h2s_threshold", 10);
        editor.putInt("ammonia_threshold", 25);
        editor.putInt("dust_threshold", 50);

        editor.apply();

        // FUNC-5.3 : Update one preference value
        editor = prefs.edit();
        editor.putInt("ammonia_threshold", 30);
        editor.apply();

        // FUNC-5.4 : Load stored preferences
        String username = prefs.getString("username", "");
        int h2s = prefs.getInt("h2s_threshold", 0);
        int ammonia = prefs.getInt("ammonia_threshold", 0);
        int dust = prefs.getInt("dust_threshold", 0);

        // FUNC-5.5 : Verify stored values are correct
        assertEquals("test_user", username);
        assertEquals(10, h2s);
        assertEquals(30, ammonia);
        assertEquals(50, dust);
    }
}