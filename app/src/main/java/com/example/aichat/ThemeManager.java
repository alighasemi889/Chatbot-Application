package com.example.aichat;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_DARK_MODE = "dark_mode";

    public static void apply(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(PREF_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void toggle(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(PREF_DARK_MODE, false);
        prefs.edit().putBoolean(PREF_DARK_MODE, !isDark).apply();
        apply(activity);
        activity.recreate();
    }

    public static boolean isDarkMode(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        return prefs.getBoolean(PREF_DARK_MODE, false);
    }
}
