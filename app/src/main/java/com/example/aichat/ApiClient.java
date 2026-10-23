package com.example.aichat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Minimal client for OpenAI-compatible chat completion endpoints. */
public class ApiClient {

    public static String chat(String url, String key, String model, List<Message> history) throws Exception {
        JSONArray msgs = new JSONArray();
        for (Message m : history) {
            msgs.put(new JSONObject()
                    .put("role", m.fromUser ? "user" : "assistant")
                    .put("content", m.text));
        }
        JSONObject body = new JSONObject().put("model", model).put("messages", msgs);

        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(20000);
        c.setReadTimeout(60000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Authorization", "Bearer " + key);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }
        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + sb);
        }
        return new JSONObject(sb.toString())
                .getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content").trim();
    }
}
