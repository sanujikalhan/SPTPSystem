package com.example.sptm_systerm;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginStorage {private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_TOKEN = "authToken";
    private SharedPreferences sharedPreferences;

    public LoginStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearAuthToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply();
    }
}
