package com.example.aichat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Stores reminder tasks per user in SharedPreferences as JSON. */
public class TaskStore {
    private static final String PREFS = "tasks";

    public static class Task {
        public final int id;
        public final String title;
        public final long time;

        Task(int id, String title, long time) {
            this.id = id;
            this.title = title;
            this.time = time;
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(Context ctx) {
        String user = UserManager.getCurrentUser(ctx);
        return "tasks_" + (user == null ? "guest" : user);
    }

    public static List<Task> load(Context ctx) {
        List<Task> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString(key(ctx), "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                result.add(new Task(o.getInt("id"), o.getString("title"), o.getLong("time")));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    private static void saveAll(Context ctx, List<Task> tasks) {
        JSONArray arr = new JSONArray();
        try {
            for (Task t : tasks) {
                arr.put(new JSONObject()
                        .put("id", t.id)
                        .put("title", t.title)
                        .put("time", t.time));
            }
        } catch (JSONException ignored) {
        }
        prefs(ctx).edit().putString(key(ctx), arr.toString()).apply();
    }

    public static Task add(Context ctx, String title, long time) {
        List<Task> tasks = load(ctx);
        Task t = new Task(new Random().nextInt(Integer.MAX_VALUE), title, time);
        tasks.add(t);
        saveAll(ctx, tasks);
        return t;
    }

    public static void remove(Context ctx, Task task) {
        List<Task> tasks = load(ctx);
        List<Task> kept = new ArrayList<>();
        for (Task t : tasks) {
            if (t.id != task.id) kept.add(t);
        }
        saveAll(ctx, kept);
    }
}
