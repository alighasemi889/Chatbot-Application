package com.example.aichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        TextView phone = findViewById(R.id.profile_phone);
        String user = UserManager.getCurrentUser(this);
        phone.setText(user != null ? user : "");

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.row_api).setOnClickListener(v -> showApiDialog());
        findViewById(R.id.row_about).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        BottomNav.setup(this, nav, R.id.nav_settings);

        LoginActivity.animateIn(findViewById(R.id.profile_card));
    }

    private void showApiDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final EditText etUrl = v.findViewById(R.id.et_url);
        final EditText etKey = v.findViewById(R.id.et_key);
        final EditText etModel = v.findViewById(R.id.et_model);
        etUrl.setText(prefs.getString("url", MainActivity.DEFAULT_URL));
        etKey.setText(prefs.getString("key", MainActivity.DEFAULT_KEY));
        etModel.setText(prefs.getString("model", MainActivity.DEFAULT_MODEL));

        new AlertDialog.Builder(this)
                .setTitle(R.string.api_settings)
                .setView(v)
                .setPositiveButton(R.string.save, (d, w) -> prefs.edit()
                        .putString("url", etUrl.getText().toString().trim())
                        .putString("key", etKey.getText().toString().trim())
                        .putString("model", etModel.getText().toString().trim())
                        .apply())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
