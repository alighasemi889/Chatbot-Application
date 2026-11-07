package com.example.aichat;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires the bottom navigation bar to the existing screens. */
public final class BottomNav {
    private BottomNav() {}

    public static void setup(AppCompatActivity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;
            Intent i = null;
            if (id == R.id.nav_home) {
                i = new Intent(activity, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_chat) {
                i = new Intent(activity, HistoryActivity.class);
            } else if (id == R.id.nav_tools) {
                i = new Intent(activity, TaskActivity.class);
            } else if (id == R.id.nav_settings) {
                i = new Intent(activity, SettingsActivity.class);
            }
            if (i != null) activity.startActivity(i);
            return false; // the target screen sets its own selection
        });
    }
}
