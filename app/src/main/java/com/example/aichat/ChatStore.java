package com.example.aichat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Stores chat conversations per user in SharedPreferences as JSON. */
public class ChatStore {
    private static final String PREFS = "chat_history";
    private static final int TITLE_LEN = 40;

    public static class Summary {
        public final String id;
        public final String title;

        Summary(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(Context ctx) {
        String user = UserManager.getCurrentUser(ctx);
        return "chats_" + (user == null ? "guest" : user);
    }

    private static JSONArray loadAll(Context ctx) {
        String raw = prefs(ctx).getString(key(ctx), "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private static void saveAll(Context ctx, JSONArray arr) {
        prefs(ctx).edit().putString(key(ctx), arr.toString()).apply();
    }

    /** Inserts or replaces a conversation; the most recent one is kept first. */
    public static void saveConversation(Context ctx, String id, List<Message> messages) {
        JSONArray all = loadAll(ctx);
        JSONArray msgs = new JSONArray();
        String title = "";
        try {
            for (Message m : messages) {
                JSONObject o = new JSONObject();
                o.put("t", m.text);
                o.put("u", m.fromUser);
                msgs.put(o);
                if (title.isEmpty() && m.fromUser) {
                    title = shorten(m.text);
                }
            }
            JSONObject conv = new JSONObject();
            conv.put("id", id);
            conv.put("title", title);
            conv.put("messages", msgs);

            JSONArray out = new JSONArray();
            out.put(conv);
            for (int i = 0; i < all.length(); i++) {
                JSONObject o = all.getJSONObject(i);
                if (!id.equals(o.optString("id"))) {
                    out.put(o);
                }
            }
            saveAll(ctx, out);
        } catch (JSONException ignored) {
        }
    }

    public static List<Message> loadMessages(Context ctx, String id) {
        List<Message> result = new ArrayList<>();
        JSONArray all = loadAll(ctx);
        try {
            for (int i = 0; i < all.length(); i++) {
                JSONObject conv = all.getJSONObject(i);
                if (id.equals(conv.optString("id"))) {
                    JSONArray msgs = conv.getJSONArray("messages");
                    for (int j = 0; j < msgs.length(); j++) {
                        JSONObject o = msgs.getJSONObject(j);
                        result.add(new Message(o.getString("t"), o.getBoolean("u")));
                    }
                    break;
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public static List<Summary> listConversations(Context ctx) {
        List<Summary> result = new ArrayList<>();
        JSONArray all = loadAll(ctx);
        try {
            for (int i = 0; i < all.length(); i++) {
                JSONObject conv = all.getJSONObject(i);
                result.add(new Summary(conv.getString("id"), conv.optString("title", "")));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public static void deleteConversation(Context ctx, String id) {
        JSONArray all = loadAll(ctx);
        JSONArray out = new JSONArray();
        try {
            for (int i = 0; i < all.length(); i++) {
                JSONObject o = all.getJSONObject(i);
                if (!id.equals(o.optString("id"))) {
                    out.put(o);
                }
            }
        } catch (JSONException ignored) {
        }
        saveAll(ctx, out);
    }

    private static String shorten(String s) {
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() > TITLE_LEN ? t.substring(0, TITLE_LEN) + "…" : t;
    }
}
