package com.speed.sofasogood.game.levels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.speed.sofasogood.BgmService;
import com.speed.sofasogood.R;

public class Level1Activity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private int dialogIndex = 0;
    private boolean dialogFinished = false;

    private final String[] dialogs = {
            "Finally, I've moved in… this is my new home.",
            "But the living room is such a mess, boxes and furniture everywhere.",
            "I should set up the sofa and table first, so it feels more like home.",
            "Hmm, those boxes in the corner need sorting too.",
            "Alright, let's start arranging—time to make the living room shine!"
    };

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level1);

        // 暫停 BGM
        Intent bgm = new Intent(this, BgmService.class);
        bgm.setAction("PAUSE");
        startService(bgm);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        TextView dialogBox = findViewById(R.id.dialogBox);
        ImageView dialogCharacter = findViewById(R.id.dialogCharacter);
        View pauseOverlay = findViewById(R.id.pauseOverlay);

        // 顯示第一句台詞
        dialogBox.setText(dialogs[dialogIndex]);

        // 點擊畫面切換台詞
        findViewById(R.id.level1Root).setOnClickListener(v -> {
            if (dialogFinished) return;
            dialogIndex++;
            if (dialogIndex < dialogs.length) {
                dialogBox.setText(dialogs[dialogIndex]);
            } else {
                // 對話結束，隱藏角色和對話框
                dialogFinished = true;
                dialogBox.setVisibility(View.GONE);
                dialogCharacter.setVisibility(View.GONE);
            }
        });

        // 暫停按鈕
        findViewById(R.id.btnPause).setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
            pauseOverlay.setVisibility(View.VISIBLE);
        });

        // 繼續
        View btnResume = findViewById(R.id.btnResume);
        setupButtonAnimation(btnResume);
        btnResume.setOnClickListener(v -> pauseOverlay.setVisibility(View.GONE));

        // 退出
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
                    soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
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
        Intent bgm = new Intent(this, BgmService.class);
        bgm.setAction("RESUME");
        startService(bgm);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }
}
