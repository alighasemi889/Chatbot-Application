package com.example.aichat;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent per-device user preferences (language, theme, assistant voice, linked GitHub user). */
public final class UserPrefs {
    private static final String FILE = "user_prefs";
    private static final String K_LANG = "language";
    private static final String K_THEME = "theme";
    private static final String K_VOICE = "assistant_voice";
    private static final String K_GITHUB = "github_user";

    private UserPrefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** "fa" or "en". Drives speech recognition and the language the assistant answers in. */
    public static String language(Context c) { return prefs(c).getString(K_LANG, "fa"); }
    public static void setLanguage(Context c, String v) { prefs(c).edit().putString(K_LANG, v).apply(); }

    /** Only "dark" exists in this app; the value is stored so the choice survives restarts. */
    public static String theme(Context c) { return prefs(c).getString(K_THEME, "dark"); }
    public static void setTheme(Context c, String v) { prefs(c).edit().putString(K_THEME, v).apply(); }

    public static boolean assistantVoice(Context c) { return prefs(c).getBoolean(K_VOICE, false); }
    public static void setAssistantVoice(Context c, boolean v) { prefs(c).edit().putBoolean(K_VOICE, v).apply(); }

    public static String githubUser(Context c) { return prefs(c).getString(K_GITHUB, ""); }
    public static void setGithubUser(Context c, String v) { prefs(c).edit().putString(K_GITHUB, v).apply(); }

    public static String speechLangTag(Context c) { return "en".equals(language(c)) ? "en-US" : "fa-IR"; }

    public static String answerLangHint(Context c) {
        return "en".equals(language(c))
                ? "\n\nAlways answer in English."
                : "\n\nهمیشه به زبان فارسی پاسخ بده.";
    }
}
