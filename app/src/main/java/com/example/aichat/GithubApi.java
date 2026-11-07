package com.example.aichat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** Minimal client for the official GitHub REST API (public endpoints, no token required). */
public final class GithubApi {
    private static final String BASE = "https://api.github.com";
    private static final int MAX_REPO_PAGES = 3; // up to 300 repositories for the star total

    private GithubApi() {}

    public static final class GithubException extends Exception {
        private final int messageRes;

        GithubException(int messageRes) {
            super("github");
            this.messageRes = messageRes;
        }

        public int messageRes() {
            return messageRes;
        }
    }

    /** Accepts "octocat", "github.com/octocat" or "https://github.com/octocat/". Returns null if invalid. */
    public static String parseUsername(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        int idx = s.toLowerCase(Locale.ROOT).indexOf("github.com/");
        if (idx >= 0) s = s.substring(idx + "github.com/".length());
        int cut = s.indexOf('/');
        if (cut >= 0) s = s.substring(0, cut);
        cut = s.indexOf('?');
        if (cut >= 0) s = s.substring(0, cut);
        s = s.replace("@", "").trim();
        return s.matches("[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?") ? s : null;
    }

    public static GithubProfile fetch(String user) throws GithubException {
        JSONObject u = getObject(BASE + "/users/" + user);
        GithubProfile p = new GithubProfile();
        p.login = u.optString("login", user);
        p.name = u.optString("name", "");
        p.bio = u.optString("bio", "");
        p.avatarUrl = u.optString("avatar_url", "");
        p.htmlUrl = u.optString("html_url", "");
        p.createdAt = u.optString("created_at", "");
        p.publicRepos = u.optInt("public_repos", 0);
        p.followers = u.optInt("followers", 0);

        long stars = 0;
        boolean partial = false;
        try {
            for (int page = 1; page <= MAX_REPO_PAGES; page++) {
                JSONArray repos = getArray(BASE + "/users/" + user + "/repos?type=owner&per_page=100&page=" + page);
                for (int i = 0; i < repos.length(); i++) {
                    stars += repos.getJSONObject(i).optInt("stargazers_count", 0);
                }
                if (repos.length() < 100) break;
                if (page == MAX_REPO_PAGES) partial = true;
            }
        } catch (Exception e) {
            throw new GithubException(R.string.github_network);
        }
        p.stars = stars;
        p.starsPartial = partial;
        p.commits = commitCount(user);
        return p;
    }

    /** Total commits the search API reports for this author, or -1 if unavailable. */
    private static long commitCount(String user) {
        try {
            JSONObject s = getObject(BASE + "/search/commits?q=author:" + user + "&per_page=1");
            return s.optLong("total_count", -1);
        } catch (GithubException e) {
            return -1;
        }
    }

    private static JSONObject getObject(String url) throws GithubException {
        try {
            return new JSONObject(get(url));
        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException(R.string.github_network);
        }
    }

    private static JSONArray getArray(String url) throws GithubException {
        try {
            return new JSONArray(get(url));
        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException(R.string.github_network);
        }
    }

    private static String get(String url) throws GithubException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            conn.setRequestProperty("User-Agent", "AbdollahAI-Android");
            int code = conn.getResponseCode();
            if (code == 404) throw new GithubException(R.string.github_not_found);
            if (code == 403 || code == 429) throw new GithubException(R.string.github_rate);
            if (code == 422) throw new GithubException(R.string.github_commits_unavailable);
            if (code != 200) throw new GithubException(R.string.github_network);
            try (InputStream in = conn.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                return out.toString("UTF-8");
            }
        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException(R.string.github_network);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
