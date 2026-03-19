package com.concordia.qualiair;

import android.content.Context;
import android.content.SharedPreferences;

// separeted the thresholds to a separate class to make things smoother and not have one large class
public class UserPreferences {

    private static final String PREF_NAME  = "QualiAirPreferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL    = "email";

    private String username = "";
    private String email    = "";

    private final SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void loadAllPreferences() {
        username = sharedPreferences.getString(KEY_USERNAME, "");
        email    = sharedPreferences.getString(KEY_EMAIL,    "");
    }

    public void saveAllPreferences() {
        sharedPreferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL,    email)
                .apply();
    }

    public String getUsername() { return username; }
    public String getEmail()    { return email;    }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email)       { this.email    = email;    }
}