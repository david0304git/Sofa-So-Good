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
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LevelSelectActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private ViewFlipper viewFlipper;
    private View btnPrev, btnNext;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level_select);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.levelSelect), (v, insets) -> {
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

        viewFlipper = findViewById(R.id.viewFlipper);
        btnPrev = findViewById(R.id.btnPrevPage);
        btnNext = findViewById(R.id.btnNextPage);

        // 翻頁
        btnNext.setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
            viewFlipper.showNext();
            updateArrows();
        });
        btnPrev.setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
            viewFlipper.showPrevious();
            updateArrows();
        });

        // 關卡按鈕動畫
        int[] levelBtnIds = {
                R.id.btnLevel1, R.id.btnLevel2, R.id.btnLevel3, R.id.btnLevel4,
                R.id.btnLevel5, R.id.btnLevel6, R.id.btnLevel7, R.id.btnLevel8
        };
        for (int id : levelBtnIds) {
            setupButtonAnimation(findViewById(id));
        }

        // 返回
        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 第一關
        findViewById(R.id.btnLevel1).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level1Activity.class)));
        findViewById(R.id.btnLevel2).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level2Activity.class)));
        findViewById(R.id.btnLevel3).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level3Activity.class)));
        findViewById(R.id.btnLevel4).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level4Activity.class)));
        findViewById(R.id.btnLevel5).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level5Activity.class)));
        findViewById(R.id.btnLevel6).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level6Activity.class)));
        findViewById(R.id.btnLevel7).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level7Activity.class)));
        findViewById(R.id.btnLevel8).setOnClickListener(v ->
                startActivity(new Intent(this, com.speed.sofasogood.game.levels.Level8Activity.class)));
    }

    private void updateArrows() {
        int current = viewFlipper.getDisplayedChild();
        int total = viewFlipper.getChildCount();
        btnPrev.setVisibility(current == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(current == total - 1 ? View.INVISIBLE : View.VISIBLE);
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
