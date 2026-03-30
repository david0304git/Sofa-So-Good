package com.speed.sofasogood.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.activities.LeaderboardActivity;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

public class LevelResultActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private float soundVolume = 1.0f;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_result);
        ImmersiveHelper.enable(getWindow());

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        String nextLevelClass = getIntent().getStringExtra("nextLevel");

        View btnLeaderboard = findViewById(R.id.btnLeaderboard);
        setupButtonAnimation(btnLeaderboard);
        btnLeaderboard.setOnClickListener(v -> {
            int level = getIntent().getIntExtra("level", 1);
            Intent intent = new Intent(LevelResultActivity.this, LeaderboardActivity.class);
            intent.putExtra("level", level);
            startActivity(intent);
        });

        View btnNext = findViewById(R.id.btnNextLevel);
        setupButtonAnimation(btnNext);
        if (nextLevelClass != null) {
            btnNext.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(this, Class.forName(nextLevelClass)));
                } catch (ClassNotFoundException ignored) {}
                finish();
            });
        } else {
            btnNext.setVisibility(View.GONE);
        }

        View btnExit = findViewById(R.id.btnExit);
        setupButtonAnimation(btnExit);
        btnExit.setOnClickListener(v -> finish());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_release));
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }
}
