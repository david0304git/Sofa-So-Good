package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.speed.sofasogood.R;
import com.speed.sofasogood.auth.AuthManager;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private View btnResetPassword;
    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ImmersiveHelper.enable(getWindow());

        authManager = new AuthManager();

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> sendReset());
        setupButtonAnimation(btnResetPassword);

        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_release));
                    break;
            }
            return false;
        });
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