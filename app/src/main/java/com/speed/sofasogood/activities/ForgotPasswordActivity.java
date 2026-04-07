package com.speed.sofasogood.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.speed.sofasogood.R;
import com.speed.sofasogood.auth.AuthManager;
import com.speed.sofasogood.utils.LocaleHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authManager = new AuthManager();

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> sendReset());
    }

    private void sendReset() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            return;
        }

        btnResetPassword.setEnabled(false);

        authManager.sendPasswordReset(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnResetPassword.setEnabled(true);
                    Toast.makeText(this, "Reset failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}