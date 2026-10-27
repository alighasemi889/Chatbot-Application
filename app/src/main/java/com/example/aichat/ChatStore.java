package com.example.aichat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Stores chat conversations and saved answers per user in SharedPreferences as JSON. */
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

    private static String userName(Context ctx) {
        String user = UserManager.getCurrentUser(ctx);
        return user == null ? "guest" : user;
    }

    private static String key(Context ctx) {
        return "chats_" + userName(ctx);
    }

    private static String savedKey(Context ctx) {
        return "saved_" + userName(ctx);
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

    /** Returns the custom title of a conversation, or null if it has none. */
    private static String findCustomTitle(JSONArray all, String id) {
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && id.equals(o.optString("id")) && o.optBoolean("custom")) {
                return o.optString("title");
            }
        }
        return null;
    }

    /** Puts conv first and removes any older entry with the same id. */
    private static void replaceConv(Context ctx, JSONArray all, String id, JSONObject conv) {
        JSONArray out = new JSONArray();
        out.put(conv);
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && !id.equals(o.optString("id"))) {
                out.put(o);
            }
        }
        saveAll(ctx, out);
    }

    /** Inserts or replaces a conversation. A custom title set by the user is kept. */
    public static void saveConversation(Context ctx, String id, List<Message> messages) {
        JSONArray all = loadAll(ctx);
        String customTitle = findCustomTitle(all, id);
        JSONArray msgs = new JSONArray();
        String derivedTitle = "";
        try {
            for (Message m : messages) {
                JSONObject o = new JSONObject();
                o.put("t", m.text);
                o.put("u", m.fromUser);
                msgs.put(o);
                if (derivedTitle.isEmpty() && m.fromUser) {
                    derivedTitle = shorten(m.text);
                }
            }
            JSONObject conv = new JSONObject();
            conv.put("id", id);
            conv.put("title", customTitle != null ? customTitle : derivedTitle);
            conv.put("custom", customTitle != null);
            conv.put("messages", msgs);
            replaceConv(ctx, all, id, conv);
        } catch (JSONException ignored) {
        }
    }

    public static List<Message> loadMessages(Context ctx, String id) {
        List<Message> result = new ArrayList<>();
        JSONArray all = loadAll(ctx);
        for (int i = 0; i < all.length(); i++) {
            JSONObject conv = all.optJSONObject(i);
            if (conv != null && id.equals(conv.optString("id"))) {
                JSONArray msgs = conv.optJSONArray("messages");
                if (msgs == null) break;
                for (int j = 0; j < msgs.length(); j++) {
                    JSONObject o = msgs.optJSONObject(j);
                    if (o != null) {
                        result.add(new Message(o.optString("t"), o.optBoolean("u")));
                    }
                }
                break;
            }
        }
        return result;
    }

    public static String getTitle(Context ctx, String id) {
        JSONArray all = loadAll(ctx);
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && id.equals(o.optString("id"))) {
                return o.optString("title", "");
            }
        }
        return "";
    }

    /** Renames a conversation. If it is not saved yet, an empty entry is created to keep the name. */
    public static void renameConversation(Context ctx, String id, String newTitle) {
        String t = newTitle.trim();
        if (t.isEmpty()) return;
        JSONArray all = loadAll(ctx);
        try {
            JSONObject conv = null;
            for (int i = 0; i < all.length(); i++) {
                JSONObject o = all.optJSONObject(i);
                if (o != null && id.equals(o.optString("id"))) {
                    conv = o;
                    break;
                }
            }
            if (conv == null) {
                conv = new JSONObject();
                conv.put("id", id);
                conv.put("messages", new JSONArray());
            }
            conv.put("title", t);
            conv.put("custom", true);
            replaceConv(ctx, all, id, conv);
        } catch (JSONException ignored) {
        }
    }

    public static List<Summary> listConversations(Context ctx) {
        List<Summary> result = new ArrayList<>();
        JSONArray all = loadAll(ctx);
        for (int i = 0; i < all.length(); i++) {
            JSONObject conv = all.optJSONObject(i);
            if (conv != null) {
                result.add(new Summary(conv.optString("id"), conv.optString("title", "")));
            }
        }
        return result;
    }

    public static void deleteConversation(Context ctx, String id) {
        JSONArray all = loadAll(ctx);
        JSONArray out = new JSONArray();
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && !id.equals(o.optString("id"))) {
                out.put(o);
            }
        }
        saveAll(ctx, out);
    }

    // ---------- Saved answers ----------

    private static JSONArray loadSaved(Context ctx) {
        try {
            return new JSONArray(prefs(ctx).getString(savedKey(ctx), "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private static void saveSaved(Context ctx, JSONArray arr) {
        prefs(ctx).edit().putString(savedKey(ctx), arr.toString()).apply();
    }

    public static boolean isSaved(Context ctx, String text) {
        JSONArray arr = loadSaved(ctx);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && text.equals(o.optString("text"))) {
                return true;
            }
        }
        return false;
    }

    public static void saveAnswer(Context ctx, String convId, String text) {
        if (isSaved(ctx, text)) return;
        JSONArray arr = loadSaved(ctx);
        try {
            arr.put(new JSONObject()
                    .put("conv", convId)
                    .put("text", text)
                    .put("time", System.currentTimeMillis()));
        } catch (JSONException ignored) {
        }
        saveSaved(ctx, arr);
    }

    public static void removeSaved(Context ctx, String text) {
        JSONArray arr = loadSaved(ctx);
        JSONArray out = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && !text.equals(o.optString("text"))) {
                out.put(o);
            }
        }
        saveSaved(ctx, out);
    }

    /** Saved answers, newest first. */
    public static List<String> listSaved(Context ctx) {
        List<String> result = new ArrayList<>();
        JSONArray arr = loadSaved(ctx);
        for (int i = arr.length() - 1; i >= 0; i--) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) {
                result.add(o.optString("text"));
            }
        }
        return result;
    }

    private static String shorten(String s) {
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() > TITLE_LEN ? t.substring(0, TITLE_LEN) + "…" : t;
    }
}
