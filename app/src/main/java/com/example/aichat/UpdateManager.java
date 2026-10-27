package com.example.aichat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Checks GitHub releases for a newer APK, downloads it and starts the installer. */
public class UpdateManager {

    private static final String RELEASES_API =
            "https://api.github.com/repos/alighasemi889/Chatbot-Application/releases?per_page=10";

    public static class Release {
        public final String version;
        public final String apkUrl;

        Release(String version, String apkUrl) {
            this.version = version;
            this.apkUrl = apkUrl;
        }
    }

    public interface Progress {
        void onPercent(int percent);
    }

    public static String currentVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
        }
    }

    /** Returns the newest non-draft release that has an APK, or null if none. Runs on a background thread. */
    public static Release fetchLatest() throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(RELEASES_API).openConnection();
        c.setRequestProperty("Accept", "application/vnd.github+json");
        c.setRequestProperty("User-Agent", "AIChat-Android");
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        int code = c.getResponseCode();
        if (code != 200) throw new Exception("HTTP " + code);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }

        JSONArray releases = new JSONArray(sb.toString());
        for (int i = 0; i < releases.length(); i++) {
            JSONObject rel = releases.getJSONObject(i);
            if (rel.optBoolean("draft")) continue;
            JSONArray assets = rel.optJSONArray("assets");
            if (assets == null) continue;
            for (int j = 0; j < assets.length(); j++) {
                JSONObject a = assets.getJSONObject(j);
                String name = a.optString("name");
                if (name.endsWith(".apk")) {
                    String version = normalizeTag(rel.optString("tag_name"));
                    return new Release(version, a.optString("browser_download_url"));
                }
            }
        }
        return null;
    }

    static String normalizeTag(String tag) {
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    /** True if remote version (e.g. "0.2.6") is greater than local (e.g. "0.2.5-beta"). Compares major.minor.patch. */
    public static boolean isNewer(String remote, String local) {
        int[] r = parse(remote);
        int[] l = parse(local);
        for (int k = 0; k < 3; k++) {
            if (r[k] != l[k]) return r[k] > l[k];
        }
        return false;
    }

    private static int[] parse(String version) {
        int[] out = new int[3];
        String core = normalizeTag(version).split("-")[0];
        String[] parts = core.split("\\.");
        for (int k = 0; k < Math.min(3, parts.length); k++) {
            try {
                out[k] = Integer.parseInt(parts[k].replaceAll("\\D", ""));
            } catch (NumberFormatException ignored) {
                out[k] = 0;
            }
        }
        return out;
    }

    /** Downloads the APK into the app cache. Runs on a background thread. */
    public static File download(Context ctx, String url, Progress progress) throws Exception {
        File dir = new File(ctx.getCacheDir(), "updates");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        File out = new File(dir, "update.apk");

        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "AIChat-Android");
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());

        long total = c.getContentLength();
        long done = 0;
        int last = -1;
        try (InputStream in = c.getInputStream(); FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                fos.write(buf, 0, n);
                done += n;
                if (total > 0) {
                    int p = (int) (done * 100 / total);
                    if (p != last) {
                        last = p;
                        progress.onPercent(p);
                    }
                }
            }
        }
        return out;
    }

    /**
     * Opens the system installer for the APK.
     * Returns false if the user first has to allow installs from this app (settings are opened in that case).
     */
    public static boolean install(Context ctx, File apk) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ctx.getPackageManager().canRequestPackageInstalls()) {
            Intent settings = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + ctx.getPackageName()));
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(settings);
            return false;
        }
        Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", apk);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
        return true;
    }
}
