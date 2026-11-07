package com.example.aichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences apiPrefs;
    private TextView languageValue;
    private TextView githubValue;
    private SwitchCompat voiceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        apiPrefs = getSharedPreferences("settings", MODE_PRIVATE);
        TextView phone = findViewById(R.id.profile_phone);
        String user = UserManager.getCurrentUser(this);
        phone.setText(user != null ? user : "");

        languageValue = findViewById(R.id.language_value);
        githubValue = findViewById(R.id.github_value);
        voiceSwitch = findViewById(R.id.switch_voice);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.row_api).setOnClickListener(v -> showApiDialog());
        findViewById(R.id.row_language).setOnClickListener(v -> {
            String next = "en".equals(UserPrefs.language(this)) ? "fa" : "en";
            UserPrefs.setLanguage(this, next);
            refresh();
        });
        voiceSwitch.setOnCheckedChangeListener((b, checked) -> UserPrefs.setAssistantVoice(this, checked));
        findViewById(R.id.row_github).setOnClickListener(v ->
                startActivity(new Intent(this, GithubActivity.class)));
        findViewById(R.id.row_about).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        BottomNav.setup(this, findViewById(R.id.bottom_nav), R.id.nav_settings);
        LoginActivity.animateIn(findViewById(R.id.profile_card));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean en = "en".equals(UserPrefs.language(this));
        languageValue.setText(en ? getString(R.string.lang_en) : getString(R.string.lang_fa));
        voiceSwitch.setOnCheckedChangeListener(null);
        voiceSwitch.setChecked(UserPrefs.assistantVoice(this));
        voiceSwitch.setOnCheckedChangeListener((b, checked) -> UserPrefs.setAssistantVoice(this, checked));
        String gh = UserPrefs.githubUser(this);
        githubValue.setText(gh.isEmpty() ? getString(R.string.github_not_connected) : "@" + gh);
    }

    private void showApiDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final EditText etUrl = v.findViewById(R.id.et_url);
        final EditText etKey = v.findViewById(R.id.et_key);
        final EditText etModel = v.findViewById(R.id.et_model);
        etUrl.setText(apiPrefs.getString("url", MainActivity.DEFAULT_URL));
        etKey.setText(apiPrefs.getString("key", MainActivity.DEFAULT_KEY));
        etModel.setText(apiPrefs.getString("model", MainActivity.DEFAULT_MODEL));

        new AlertDialog.Builder(this)
                .setTitle(R.string.api_settings)
                .setView(v)
                .setPositiveButton(R.string.save, (d, w) -> apiPrefs.edit()
                        .putString("url", etUrl.getText().toString().trim())
                        .putString("key", etKey.getText().toString().trim())
                        .putString("model", etModel.getText().toString().trim())
                        .apply())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
