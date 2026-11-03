package com.example.aichat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        EditText phoneInput = findViewById(R.id.phone);
        Button sendBtn = findViewById(R.id.send_code_btn);
        Button bioBtn = findViewById(R.id.btn_biometric);

        animateIn(findViewById(R.id.card));

        sendBtn.setOnClickListener(v -> {
            String phone = OtpManager.normalizePhone(phoneInput.getText().toString());
            if (!OtpManager.isValidPhone(phone)) {
                phoneInput.setError(getString(R.string.invalid_phone));
                return;
            }
            OtpManager.generate(phone);
            startActivity(new Intent(this, VerifyActivity.class));
        });

        bioBtn.setOnClickListener(v -> authenticateWithBiometric());
        bioBtn.setVisibility(biometricUser(this) != null && canUseBiometric(this) ? View.VISIBLE : View.GONE);
    }

    /** Subtle fade + slide-up entrance animation. */
    static void animateIn(View v) {
        v.setAlpha(0f);
        v.setTranslationY(40f);
        v.animate().alpha(1f).translationY(0f).setDuration(450).setStartDelay(80).start();
    }


    static boolean canUseBiometric(Context ctx) {
        return BiometricManager.from(ctx).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private static SharedPreferences biometricPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_BIOMETRIC, Context.MODE_PRIVATE);
    }

    static String biometricUser(Context ctx) {
        return biometricPrefs(ctx).getString(KEY_BIO_USER, null);
    }

    /** After a successful OTP login, ask once whether to enable fingerprint login, then call next. */
    static void offerBiometric(AppCompatActivity activity, String user, Runnable next) {
        if (!canUseBiometric(activity) || user.equals(biometricUser(activity))) {
            next.run();
            return;
        }
        new AlertDialog.Builder(activity)
                .setMessage(R.string.enable_biometric_q)
                .setPositiveButton(R.string.yes, (d, w) ->
                        biometricPrefs(activity).edit().putString(KEY_BIO_USER, user).apply())
                .setNegativeButton(R.string.no, null)
                .setOnDismissListener(d -> next.run())
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void authenticateWithBiometric() {
        final String user = biometricUser(this);
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
