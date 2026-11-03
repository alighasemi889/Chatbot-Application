package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            ui.postDelayed(this, 1000);
        }
    };

    private String phone;
    private TextView testCodeView;
    private TextView errorView;
    private TextView timerView;
    private EditText codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_verify);

        phone = OtpManager.getPhone();
        if (phone == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        TextView phoneView = findViewById(R.id.phone_text);
        testCodeView = findViewById(R.id.test_code);
        errorView = findViewById(R.id.error_text);
        timerView = findViewById(R.id.timer_text);
        codeInput = findViewById(R.id.code_input);

        phoneView.setText(getString(R.string.verify_subtitle, phone));
        LoginActivity.animateIn(findViewById(R.id.test_card));
        LoginActivity.animateIn(findViewById(R.id.card));
        showTestCode();

        codeInput.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onVerify();
                return true;
            }
            return false;
        });
        findViewById(R.id.verify_btn).setOnClickListener(v -> onVerify());
        findViewById(R.id.resend_btn).setOnClickListener(v -> {
            OtpManager.generate(phone);
            codeInput.setText("");
            errorView.setText("");
            showTestCode();
            Toast.makeText(this, R.string.code_regenerated, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.change_phone_btn).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ui.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.removeCallbacks(ticker);
    }

    private void showTestCode() {
        String c = OtpManager.currentCode();
        testCodeView.setText(c != null ? c : "------");
        updateTimer();
    }

    private void updateTimer() {
        long s = OtpManager.remainingSeconds();
        timerView.setText(s > 0 ? getString(R.string.timer_format, s) : getString(R.string.code_expired));
    }

    private void onVerify() {
        String input = OtpManager.normalizePhone(codeInput.getText().toString());
        if (OtpManager.verify(phone, input)) {
            UserManager.loginWithPhone(this, phone);
            LoginActivity.offerBiometric(this, phone, this::openMain);
        } else {
            errorView.setText(R.string.invalid_code);
            codeInput.setText("");
        }
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
