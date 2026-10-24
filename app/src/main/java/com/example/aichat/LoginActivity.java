package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.text.Html;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_login);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_btn);
        TextView toggle = findViewById(R.id.toggle_register);

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                username.setError(getString(R.string.empty_field));
                return;
            }

            if (UserManager.login(this, user, pass)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                password.setText("");
                password.setError(getString(R.string.invalid_login));
            }
        });

        toggle.setText(Html.fromHtml(getString(R.string.no_account)));
        toggle.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }
}
