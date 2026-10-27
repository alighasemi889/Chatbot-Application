package com.example.aichat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Stores quiz results per user for the "مسیر یادگیری من" screen. */
public class QuizStore {
    private static final String PREFS = "quiz_stats";

    public static class Stats {
        public int quizzes;
        public int answered;
        public int correct;
        public int averagePercent;
        public int bestPercent;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(Context ctx) {
        String user = UserManager.getCurrentUser(ctx);
        return "results_" + (user == null ? "guest" : user);
    }

    private static JSONArray load(Context ctx) {
        try {
            return new JSONArray(prefs(ctx).getString(key(ctx), "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static void save(Context ctx, int correct, int total) {
        JSONArray arr = load(ctx);
        try {
            arr.put(new JSONObject()
                    .put("t", System.currentTimeMillis())
                    .put("c", correct)
                    .put("n", total));
        } catch (JSONException ignored) {
        }
        prefs(ctx).edit().putString(key(ctx), arr.toString()).apply();
    }

    public static Stats stats(Context ctx) {
        Stats s = new Stats();
        JSONArray arr = load(ctx);
        long sumPercent = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            int c = o.optInt("c");
            int n = o.optInt("n");
            if (n <= 0) continue;
            int percent = c * 100 / n;
            s.quizzes++;
            s.answered += n;
            s.correct += c;
            sumPercent += percent;
            s.bestPercent = Math.max(s.bestPercent, percent);
        }
        if (s.quizzes > 0) s.averagePercent = (int) (sumPercent / s.quizzes);
        return s;
    }

    /** Most recent results first, as readable lines. */
    public static List<String> recent(Context ctx, int max) {
        List<String> out = new ArrayList<>();
        JSONArray arr = load(ctx);
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US);
        for (int i = arr.length() - 1; i >= 0 && out.size() < max; i--) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            int c = o.optInt("c");
            int n = o.optInt("n");
            int percent = n > 0 ? c * 100 / n : 0;
            out.add(fmt.format(new java.util.Date(o.optLong("t"))) + "  —  " + c + " از " + n + " (" + percent + "٪)");
        }
        return out;
    }
}
