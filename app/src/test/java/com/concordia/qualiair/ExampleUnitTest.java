package com.concordia.qualiair;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    //Test that the UserPreferences class exists
    @Test
    public void userPreferences_classExists() {
        assertNotNull(UserPreferences.class);
    }

    //test that the UserPreferences class name is correct
    @Test
    public void userPreferences_classNameCorrect() {
        assertEquals("UserPreferences", UserPreferences.class.getSimpleName());
    }
}