package com.example.aichat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/** Public GitHub profile data. Values come only from the GitHub REST API. */
public final class GithubProfile {
    public String login = "";
    public String name = "";
    public String bio = "";
    public String avatarUrl = "";
    public String htmlUrl = "";
    public String createdAt = "";
    public int publicRepos;
    public int followers;
    public long stars;
    public boolean starsPartial;
    public long commits = -1; // -1 means the commit search was not available

    /** Milliseconds since epoch for createdAt (ISO-8601 UTC), or -1 if it cannot be parsed. */
    public long createdMillis() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.parse(createdAt).getTime();
        } catch (ParseException | NullPointerException e) {
            return -1;
        }
    }
}
