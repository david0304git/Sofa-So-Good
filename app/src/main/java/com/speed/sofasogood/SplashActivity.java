package com.speed.sofasogood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splashLogo);

        // Fade in over 1.5s, hold 2s, then go to main
        logo.animate()
                .alpha(1f)
                .setDuration(1500)
                .withEndAction(() -> logo.postDelayed(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, 2000))
                .start();
    }
}
