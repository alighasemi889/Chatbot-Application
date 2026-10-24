package com.example.aichat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    private static final String PREF_BIOMETRIC = "biometric";
    private static final String KEY_BIO_USER = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_login);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_btn);
        Button bioBtn = findViewById(R.id.btn_biometric);
        TextView toggle = findViewById(R.id.toggle_register);

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                username.setError(getString(R.string.empty_field));
                return;
            }

            if (UserManager.login(this, user, pass)) {
                offerBiometric(user);
                goToMain();
            } else {
                password.setText("");
                password.setError(getString(R.string.invalid_login));
            }
        });

        bioBtn.setOnClickListener(v -> authenticateWithBiometric());
        bioBtn.setVisibility(biometricUser() != null && canUseBiometric() ? android.view.View.VISIBLE : android.view.View.GONE);

        toggle.setText(Html.fromHtml(getString(R.string.no_account)));
        toggle.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean canUseBiometric() {
        return BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private SharedPreferences biometricPrefs() {
        return getSharedPreferences(PREF_BIOMETRIC, Context.MODE_PRIVATE);
    }

    private String biometricUser() {
        return biometricPrefs().getString(KEY_BIO_USER, null);
    }

    /** After a successful password login, ask once whether to enable fingerprint login. */
    private void offerBiometric(String user) {
        if (!canUseBiometric() || user.equals(biometricUser())) return;
        new AlertDialog.Builder(this)
                .setMessage(R.string.enable_biometric_q)
                .setPositiveButton(R.string.yes, (d, w) ->
                        biometricPrefs().edit().putString(KEY_BIO_USER, user).apply())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void authenticateWithBiometric() {
        final String user = biometricUser();
        if (user == null) {
            Toast.makeText(this, R.string.biometric_no_user, Toast.LENGTH_SHORT).show();
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        UserManager.setCurrentUser(LoginActivity.this, user);
                        goToMain();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        Toast.makeText(LoginActivity.this, errString, Toast.LENGTH_SHORT).show();
                    }
                });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_sub))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        prompt.authenticate(info);
    }
}
