package com.example.aichat;

import android.app.Activity;
import androidx.appcompat.app.AppCompatDelegate;

/** The app uses a single dark theme. */
public class ThemeManager {
    public static void apply(Activity activity) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
