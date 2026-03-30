package com.speed.sofasogood;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private String currentLang;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLang = LocaleHelper.getLanguage(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        // 標題圖片跟隨語言切換
        ImageView gameTitle = findViewById(R.id.gameTitle);
        switch (currentLang) {
            case "zh-TW": gameTitle.setImageResource(R.drawable.menu_title_cn); break;
            case "ja":    gameTitle.setImageResource(R.drawable.menu_title_jp); break;
            default:      gameTitle.setImageResource(R.drawable.menu_title);    break;
        }

        // 啟動背景音樂
        startService(new Intent(this, BgmService.class));

        setupButtonAnimation(findViewById(R.id.btnStart));
        setupButtonAnimation(findViewById(R.id.btnCredits));
        setupButtonAnimation(findViewById(R.id.btnSettings));
        setupButtonAnimation(findViewById(R.id.btnQuit));

        findViewById(R.id.btnStart).setOnClickListener(v ->
                startActivity(new Intent(this, LevelSelectActivity.class)));

        findViewById(R.id.btnCredits).setOnClickListener(v ->
                startActivity(new Intent(this, CreditsActivity.class)));

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btnQuit).setOnClickListener(v -> {
            stopService(new Intent(this, BgmService.class));
            finishAffinity();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String lang = LocaleHelper.getLanguage(this);
        if (!lang.equals(currentLang)) {
            currentLang = lang;
            recreate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
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
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}