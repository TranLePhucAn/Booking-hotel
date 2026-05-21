package com.example.hotelbooking.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(
                "hotel_booking",
                Context.MODE_PRIVATE
        );
    }

    public void saveRole(String role) {
        preferences.edit().putString("role", role).apply();
    }

    public String getRole() {
        return preferences.getString("role", "");
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
