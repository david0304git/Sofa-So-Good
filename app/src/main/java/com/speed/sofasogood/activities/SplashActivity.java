package com.speed.sofasogood.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImmersiveHelper.enable(getWindow());

        ImageView logo = findViewById(R.id.splashLogo);

        MediaPlayer splashSound = MediaPlayer.create(this, R.raw.splashscreen);

        logo.animate()
                .alpha(1f)
                .setDuration(1500)
                .withStartAction(() -> splashSound.start())
                .withEndAction(() -> logo.postDelayed(() -> {
                    splashSound.release();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, 2000))
                .start();
    }
}
