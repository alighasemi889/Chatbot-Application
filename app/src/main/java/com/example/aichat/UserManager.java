package com.example.aichat;

import android.content.SharedPreferences;
import android.content.Context;

public class UserManager {
    private static final String PREF_CURRENT_USER = "current_user";
    private static final String PREF_USERS_PREFIX = "user_";

    public static void register(Context ctx, String username, String password) {
        SharedPreferences prefs = ctx.getSharedPreferences("users", Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_USERS_PREFIX + username, password).apply();
    }

    public static boolean login(Context ctx, String username, String password) {
        SharedPreferences prefs = ctx.getSharedPreferences("users", Context.MODE_PRIVATE);
        String storedPassword = prefs.getString(PREF_USERS_PREFIX + username, null);
        if (storedPassword != null && storedPassword.equals(password)) {
            prefs.edit().putString(PREF_CURRENT_USER, username).apply();
            return true;
        }
        return false;
    }

    public static boolean userExists(Context ctx, String username) {
        SharedPreferences prefs = ctx.getSharedPreferences("users", Context.MODE_PRIVATE);
        return prefs.contains(PREF_USERS_PREFIX + username);
    }

    public static String getCurrentUser(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("users", Context.MODE_PRIVATE);
        return prefs.getString(PREF_CURRENT_USER, null);
    }

    public static void logout(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("users", Context.MODE_PRIVATE);
        prefs.edit().remove(PREF_CURRENT_USER).apply();
    }
}
