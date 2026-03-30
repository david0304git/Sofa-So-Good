package com.speed.sofasogood.game.levels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.services.BgmService;
import com.speed.sofasogood.R;
import com.speed.sofasogood.game.GameView;
import com.speed.sofasogood.game.LevelResultActivity;
import com.speed.sofasogood.game.LevelTimeScore;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

public abstract class BaseLevelActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private int pushSoundId;
    private int dialogClickId;
    private boolean soundReady = false;
    private int dialogIndex = 0;
    private boolean dialogFinished = false;
    private MediaPlayer levelBgm;
    private MediaPlayer voicePlayer;
    private float soundVolume = 1.0f;
    private float mediaVolume = 1.0f;

    private TextView levelTimer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable timerTick;
    private long gameStartElapsed;
    private long pausedTotalMs;
    private int pauseDepth;
    private long pauseSegmentStart;

    protected abstract int[][] getLevelData();
    protected abstract int[] getDialogResIds();
    protected abstract int[] getExpressions();
    protected abstract String getNextLevelClass();
    protected abstract int getLevelNumber();

    protected int getBackgroundResId() {
        return R.drawable.level1_background;
    }

    /** Override to provide voice-over resource IDs for each dialog line. Return null if no voice. */
    protected int[] getVoiceResIds() {
        return null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level1);
        findViewById(R.id.level1Root).setBackgroundResource(getBackgroundResId());
        ImmersiveHelper.enable(getWindow());

        Intent pauseBgm = new Intent(this, BgmService.class);
        pauseBgm.setAction("PAUSE");
        startService(pauseBgm);

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
        levelTimer = findViewById(R.id.levelTimer);
        gameView.setSoundPool(soundPool, pushSoundId);
        gameView.setSoundVolume(soundVolume);

        int[] dialogResIds = getDialogResIds();
        String[] dialogs = new String[dialogResIds.length];
        for (int i = 0; i < dialogResIds.length; i++) dialogs[i] = getString(dialogResIds[i]);
        int[] expressions = getExpressions();
        int[] voiceResIds = getVoiceResIds();

        dialogBox.setText(dialogs[dialogIndex]);
        dialogCharacter.setImageResource(expressions[dialogIndex]);
        playVoice(voiceResIds, dialogIndex);

        findViewById(R.id.level1Root).setOnClickListener(v -> {
            if (dialogFinished) return;
            if (soundReady) soundPool.play(dialogClickId, soundVolume, soundVolume, 1, 0, 1f);
            dialogIndex++;
            if (dialogIndex < dialogs.length) {
                dialogBox.setText(dialogs[dialogIndex]);
                dialogCharacter.setImageResource(expressions[dialogIndex]);
                playVoice(voiceResIds, dialogIndex);
            } else {
                dialogFinished = true;
                dialogBox.setVisibility(View.GONE);
                dialogCharacter.setVisibility(View.GONE);
                gameView.setVisibility(View.VISIBLE);
                gameView.loadLevel(getLevelData());
                startLevelTimer();
                if (levelBgm != null && !levelBgm.isPlaying()) levelBgm.start();
            }
        });

        gameView.setOnLevelCompleteListener(() -> {
            long finishMs = elapsedPlayMs();
            int score = LevelTimeScore.scoreFromElapsedMs(finishMs);
            resetLevelTimerUi();

            Intent result = new Intent(this, LevelResultActivity.class);

            String next = getNextLevelClass();
            if (next != null) {
                result.putExtra("nextLevel", next);
            }

            result.putExtra("level", getLevelNumber());
            result.putExtra(LevelResultActivity.EXTRA_FINISH_TIME_MS, finishMs);
            result.putExtra(LevelResultActivity.EXTRA_SCORE, score);

            startActivity(result);
            finish();
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            if (soundReady) soundPool.play(clickSoundId, soundVolume, soundVolume, 1, 0, 1f);
            if (levelBgm != null && levelBgm.isPlaying()) levelBgm.pause();
            pushPause();
            pauseOverlay.setVisibility(View.VISIBLE);
        });

        View btnResume = findViewById(R.id.btnResume);
        setupButtonAnimation(btnResume);
        btnResume.setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
            popPause();
            if (levelBgm != null && dialogFinished && !levelBgm.isPlaying()) levelBgm.start();
        });

        View btnRestart = findViewById(R.id.btnRestart);
        setupButtonAnimation(btnRestart);
        btnRestart.setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
            while (pauseDepth > 0) popPause();
            resetLevelTimerUi();
            if (levelBgm != null && levelBgm.isPlaying()) levelBgm.pause();
            levelBgm.seekTo(0);
            gameView.setVisibility(View.GONE);
            dialogIndex = 0;
            dialogFinished = false;
            dialogBox.setVisibility(View.VISIBLE);
            dialogCharacter.setVisibility(View.VISIBLE);
            dialogBox.setText(dialogs[0]);
            dialogCharacter.setImageResource(expressions[0]);
            playVoice(voiceResIds, 0);
        });

        View btnExit = findViewById(R.id.btnExit);
        setupButtonAnimation(btnExit);
        btnExit.setOnClickListener(v -> finish());
    }

    private void pushPause() {
        if (gameStartElapsed == 0) return;
        if (pauseDepth == 0) pauseSegmentStart = SystemClock.elapsedRealtime();
        pauseDepth++;
    }

    private void popPause() {
        if (pauseDepth == 0) return;
        pauseDepth--;
        if (pauseDepth == 0 && pauseSegmentStart != 0) {
            pausedTotalMs += SystemClock.elapsedRealtime() - pauseSegmentStart;
            pauseSegmentStart = 0;
        }
    }

    private long elapsedPlayMs() {
        if (gameStartElapsed == 0) return 0;
        long now = SystemClock.elapsedRealtime();
        long midPause = (pauseDepth > 0 && pauseSegmentStart != 0)
                ? (now - pauseSegmentStart)
                : 0;
        return now - gameStartElapsed - pausedTotalMs - midPause;
    }

    private void startLevelTimer() {
        stopLevelTimerTicks();
        gameStartElapsed = SystemClock.elapsedRealtime();
        pausedTotalMs = 0;
        pauseDepth = 0;
        pauseSegmentStart = 0;
        levelTimer.setVisibility(View.VISIBLE);
        levelTimer.setText(LevelTimeScore.formatElapsed(0));
        timerTick = new Runnable() {
            @Override
            public void run() {
                if (gameStartElapsed == 0 || timerTick == null) return;
                levelTimer.setText(LevelTimeScore.formatElapsed(elapsedPlayMs()));
                mainHandler.postDelayed(timerTick, 50);
            }
        };
        mainHandler.post(timerTick);
    }

    private void stopLevelTimerTicks() {
        if (timerTick != null) {
            mainHandler.removeCallbacks(timerTick);
            timerTick = null;
        }
    }

    private void resetLevelTimerUi() {
        stopLevelTimerTicks();
        gameStartElapsed = 0;
        pausedTotalMs = 0;
        pauseDepth = 0;
        pauseSegmentStart = 0;
        levelTimer.setVisibility(View.GONE);
        levelTimer.setText("0:00.00");
    }

    @Override
    protected void onPause() {
        if (dialogFinished) pushPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogFinished) popPause();
    }

    private void playVoice(int[] voiceResIds, int index) {
        stopVoice();
        if (voiceResIds == null || index >= voiceResIds.length) return;
        voicePlayer = MediaPlayer.create(this, voiceResIds[index]);
        if (voicePlayer != null) {
            voicePlayer.setVolume(soundVolume, soundVolume);
            voicePlayer.setOnCompletionListener(mp -> stopVoice());
            voicePlayer.start();
        }
    }

    private void stopVoice() {
        if (voicePlayer != null) {
            voicePlayer.stop();
            voicePlayer.release();
            voicePlayer = null;
        }
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
        stopLevelTimerTicks();
        stopVoice();
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