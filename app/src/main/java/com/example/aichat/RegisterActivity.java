package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.text.Html;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_register);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        EditText confirmPassword = findViewById(R.id.confirm_password);
        Button registerBtn = findViewById(R.id.register_btn);
        TextView toggle = findViewById(R.id.toggle_login);

        registerBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirm = confirmPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                username.setError(getString(R.string.empty_field));
                return;
            }

            if (!pass.equals(confirm)) {
                confirmPassword.setError(getString(R.string.password_mismatch));
                return;
            }

            if (UserManager.userExists(this, user)) {
                username.setError(getString(R.string.user_exists));
                return;
            }

            UserManager.register(this, user, pass);
            UserManager.login(this, user, pass);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        toggle.setText(Html.fromHtml(getString(R.string.already_have_account)));
        toggle.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
