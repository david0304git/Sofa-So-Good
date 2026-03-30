package com.speed.sofasogood.game.levels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.BgmService;
import com.speed.sofasogood.R;
import com.speed.sofasogood.game.GameView;
import com.speed.sofasogood.game.LevelResultActivity;

public abstract class BaseLevelActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private int pushSoundId;
    private int dialogClickId;
    private boolean soundReady = false;
    private int dialogIndex = 0;
    private boolean dialogFinished = false;
    private MediaPlayer levelBgm;
    private float soundVolume = 1.0f;
    private float mediaVolume = 1.0f;

    protected abstract int[][] getLevelData();
    protected abstract int[] getDialogResIds();
    protected abstract int[] getExpressions();
    protected abstract String getNextLevelClass();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(com.speed.sofasogood.LocaleHelper.applyLocale(newBase));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level1);

        // Pause the background music service
        Intent pauseBgm = new Intent(this, BgmService.class);
        pauseBgm.setAction("PAUSE");
        startService(pauseBgm);

        // Small delay to ensure PAUSE command is processed
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);
        mediaVolume = prefs.getFloat("media_volume", 1.0f);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);
        pushSoundId = soundPool.load(this, R.raw.asset_push, 1);
        dialogClickId = soundPool.load(this, R.raw.level_click, 1);

        levelBgm = MediaPlayer.create(this, R.raw.level_soundtrack_loop);
        levelBgm.setLooping(true);
        levelBgm.setVolume(mediaVolume, mediaVolume);

        TextView dialogBox = findViewById(R.id.dialogBox);
        ImageView dialogCharacter = findViewById(R.id.dialogCharacter);
        View pauseOverlay = findViewById(R.id.pauseOverlay);
        GameView gameView = findViewById(R.id.gameView);
        gameView.setSoundPool(soundPool, pushSoundId);
        gameView.setSoundVolume(soundVolume);

        int[] dialogResIds = getDialogResIds();
        String[] dialogs = new String[dialogResIds.length];
        for (int i = 0; i < dialogResIds.length; i++) dialogs[i] = getString(dialogResIds[i]);
        int[] expressions = getExpressions();

        dialogBox.setText(dialogs[dialogIndex]);
        dialogCharacter.setImageResource(expressions[dialogIndex]);

        findViewById(R.id.level1Root).setOnClickListener(v -> {
            if (dialogFinished) return;
            if (soundReady) soundPool.play(dialogClickId, soundVolume, soundVolume, 1, 0, 1f);
            dialogIndex++;
            if (dialogIndex < dialogs.length) {
                dialogBox.setText(dialogs[dialogIndex]);
                dialogCharacter.setImageResource(expressions[dialogIndex]);
            } else {
                dialogFinished = true;
                dialogBox.setVisibility(View.GONE);
                dialogCharacter.setVisibility(View.GONE);
                gameView.setVisibility(View.VISIBLE);
                gameView.loadLevel(getLevelData());
                if (levelBgm != null && !levelBgm.isPlaying()) levelBgm.start();
            }
        });

        gameView.setOnLevelCompleteListener(() -> {
            Intent result = new Intent(this, LevelResultActivity.class);
            String next = getNextLevelClass();
            if (next != null) result.putExtra("nextLevel", next);
            startActivity(result);
            finish();
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
            if (levelBgm != null && levelBgm.isPlaying()) levelBgm.pause();
            pauseOverlay.setVisibility(View.VISIBLE);
        });

        View btnResume = findViewById(R.id.btnResume);
        setupButtonAnimation(btnResume);
        btnResume.setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
            if (levelBgm != null && dialogFinished && !levelBgm.isPlaying()) levelBgm.start();
        });

        View btnRestart = findViewById(R.id.btnRestart);
        setupButtonAnimation(btnRestart);
        btnRestart.setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
            if (levelBgm != null && levelBgm.isPlaying()) levelBgm.pause();
            levelBgm.seekTo(0);
            gameView.setVisibility(View.GONE);
            dialogIndex = 0;
            dialogFinished = false;
            dialogBox.setVisibility(View.VISIBLE);
            dialogCharacter.setVisibility(View.VISIBLE);
            dialogBox.setText(dialogs[0]);
            dialogCharacter.setImageResource(expressions[0]);
        });

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
        if (levelBgm != null) {
            levelBgm.stop();
            levelBgm.release();
            levelBgm = null;
        }
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
