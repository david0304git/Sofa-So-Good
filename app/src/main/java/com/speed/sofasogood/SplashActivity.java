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
        ImmersiveHelper.enable(getWindow());

        ImageView logo = findViewById(R.id.splashLogo);

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
