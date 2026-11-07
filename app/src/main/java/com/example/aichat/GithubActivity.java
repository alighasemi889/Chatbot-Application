package com.example.aichat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Connects a public GitHub profile and shows its details from the official API. */
public class GithubActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler ui = new Handler(Looper.getMainLooper());

    private EditText input;
    private ProgressBar progress;
    private TextView errorView;
    private View card;
    private ImageView avatar;
    private TextView name, login, bio, statRepos, statStars, statCommits, statFollowers, statCreated, statAge;
    private MaterialButton openBtn;
    private String htmlUrl = "";
    private String lastRequestedUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_github);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        input = findViewById(R.id.github_input);
        progress = findViewById(R.id.progress);
        errorView = findViewById(R.id.error_text);
        card = findViewById(R.id.profile_card);
        avatar = findViewById(R.id.avatar);
        name = findViewById(R.id.name);
        login = findViewById(R.id.login);
        bio = findViewById(R.id.bio);
        statRepos = findViewById(R.id.stat_repos);
        statStars = findViewById(R.id.stat_stars);
        statCommits = findViewById(R.id.stat_commits);
        statFollowers = findViewById(R.id.stat_followers);
        statCreated = findViewById(R.id.stat_created);
        statAge = findViewById(R.id.stat_age);
        openBtn = findViewById(R.id.open_github);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.fetch_btn).setOnClickListener(v -> fetch(input.getText().toString()));
        openBtn.setOnClickListener(v -> {
            if (!htmlUrl.isEmpty()) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl)));
        });
        findViewById(R.id.disconnect_btn).setOnClickListener(v -> {
            UserPrefs.setGithubUser(this, "");
            card.setVisibility(View.GONE);
            input.setText("");
            errorView.setText("");
        });

        String saved = UserPrefs.githubUser(this);
        if (!saved.isEmpty()) {
            input.setText(saved);
            fetch(saved);
        }
    }

    private void fetch(String raw) {
        String user = GithubApi.parseUsername(raw);
        if (user == null) {
            showError(R.string.github_invalid);
            return;
        }
        lastRequestedUser = user;
        errorView.setText("");
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                GithubProfile p = GithubApi.fetch(user);
                ui.post(() -> {
                    if (!user.equals(lastRequestedUser)) return;
                    progress.setVisibility(View.GONE);
                    bind(p);
                    UserPrefs.setGithubUser(this, p.login);
                });
            } catch (GithubApi.GithubException e) {
                ui.post(() -> {
                    progress.setVisibility(View.GONE);
                    showError(e.messageRes());
                });
            }
        });
    }

    private void bind(GithubProfile p) {
        card.setVisibility(View.VISIBLE);
        name.setText(p.name.isEmpty() ? p.login : p.name);
        login.setText("@" + p.login);
        bio.setText(p.bio == null || p.bio.isEmpty() ? getString(R.string.github_no_bio) : p.bio);
        statRepos.setText(String.valueOf(p.publicRepos));
        statStars.setText(p.starsPartial ? p.stars + "+" : String.valueOf(p.stars));
        statCommits.setText(p.commits < 0 ? getString(R.string.github_commits_unknown) : String.valueOf(p.commits));
        statFollowers.setText(String.valueOf(p.followers));

        long created = p.createdMillis();
        if (created > 0) {
            statCreated.setText(new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date(created)));
            long days = Math.max(0, (System.currentTimeMillis() - created) / 86_400_000L);
            statAge.setText(getString(R.string.github_age, days / 365, (days % 365) / 30));
        } else {
            statCreated.setText("-");
            statAge.setText("-");
        }
        htmlUrl = p.htmlUrl;
        openBtn.setEnabled(!htmlUrl.isEmpty());
        loadAvatar(p.avatarUrl, p.login);
    }

    private void loadAvatar(String url, String user) {
        if (url == null || url.isEmpty()) return;
        executor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(15_000);
                try (InputStream in = conn.getInputStream()) {
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    if (bmp != null) ui.post(() -> {
                        if (user.equals(lastRequestedUser)) avatar.setImageBitmap(bmp);
                    });
                } finally {
                    conn.disconnect();
                }
            } catch (Exception ignored) {
                // Avatar is decorative; keep the placeholder if it fails.
            }
        });
    }

    private void showError(int messageRes) {
        card.setVisibility(View.GONE);
        errorView.setText(messageRes);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
