package com.speed.sofasogood.game.levels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.speed.sofasogood.game.LevelScoreConfig;
import com.speed.sofasogood.services.BgmService;
import com.speed.sofasogood.R;
import com.speed.sofasogood.game.GameView;
import android.view.KeyEvent;
import com.speed.sofasogood.game.LevelResultActivity;
import com.speed.sofasogood.game.LevelTimeScore;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

import java.util.HashMap;

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
    private boolean typing = false;
    private Runnable typeRunnable;

    private TextView levelTimer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable timerTick;
    private long gameStartElapsed;
    private long pausedTotalMs;
    private int pauseDepth;
    private long pauseSegmentStart;

    private GameView gameView;

    protected abstract int[][] getLevelData();
    protected abstract int[] getDialogResIds();
    protected abstract int[] getExpressions();
    protected abstract String getNextLevelClass();
    protected abstract int getLevelNumber();
    protected abstract LevelScoreConfig getScoreConfig();

    protected int getBackgroundResId() {
        return R.drawable.level1_background;
    }

    /** Override and return true to skip dialog and start the game directly. */
    protected boolean shouldSkipDialog() {
        return false;
    }
    // For debugging
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (dialogFinished && gameView != null && gameView.getVisibility() == View.VISIBLE) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                boolean handled = gameView.onKeyDown(event.getKeyCode(), event);
                if (handled) return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /** Override to provide a hint image resource ID. Return 0 for no hint. */
    protected int getHintResId() {
        return 0;
    }

    /** Override to provide per-dialog background changes. Return null to keep default. */
    protected int[] getDialogBackgrounds() {
        return null;
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
        setContentView(R.layout.level);
        findViewById(R.id.level1Root).setBackgroundResource(getBackgroundResId());
        ImmersiveHelper.enable(getWindow());

        Intent pauseBgm = new Intent(this, BgmService.class);
        pauseBgm.setAction("PAUSE");
        startService(pauseBgm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);
        mediaVolume = prefs.getFloat("media_volume", 1.0f);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
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
        int[] dialogBgs = getDialogBackgrounds();
        View rootView = findViewById(R.id.level1Root);

        View countdownOverlay = findViewById(R.id.countdownOverlay);
        com.speed.sofasogood.views.OutlinedTextView countdownText = findViewById(R.id.countdownText);

        // Hint
        View btnHint = findViewById(R.id.btnHint);
        View hintOverlay = findViewById(R.id.hintOverlay);
        ImageView hintImage = findViewById(R.id.hintImage);
        View btnCloseHint = findViewById(R.id.btnCloseHint);
        int hintResId = getHintResId();
        if (hintResId != 0) {
            hintImage.setImageResource(hintResId);
        }
        setupButtonAnimation(btnHint);
        btnHint.setOnClickListener(v -> {
            if (hintResId != 0) {
                hintOverlay.setVisibility(View.VISIBLE);
            }
        });
        setupButtonAnimation(btnCloseHint);
        btnCloseHint.setOnClickListener(v -> hintOverlay.setVisibility(View.GONE));

        // keep reference for forwarding key events
        this.gameView = gameView;

        // 跳過對話按鈕
        View btnSkip = findViewById(R.id.btnSkip);
        setupButtonAnimation(btnSkip);
        btnSkip.setOnClickListener(v -> {
            if (dialogFinished) return;
            cancelTyping();
            stopVoice();
            dialogFinished = true;
            dialogBox.setVisibility(View.GONE);
            dialogCharacter.setVisibility(View.GONE);
            v.clearAnimation();
            v.setVisibility(View.GONE);
            gameView.setVisibility(View.VISIBLE);
            gameView.loadLevel(getLevelData());
            showCountdownOverlay(countdownOverlay, countdownText, gameView, btnHint, hintResId);
        });

        // Skip dialog entirely if subclass requests it
        if (shouldSkipDialog()) {
            dialogFinished = true;
            dialogBox.setVisibility(View.GONE);
            dialogCharacter.setVisibility(View.GONE);
            btnSkip.setVisibility(View.GONE);
            gameView.setVisibility(View.VISIBLE);
            gameView.loadLevel(getLevelData());
            showCountdownOverlay(countdownOverlay, countdownText, gameView, btnHint, hintResId);
        } else {
            typeText(dialogBox, dialogs[dialogIndex]);
            dialogCharacter.setImageResource(expressions[dialogIndex]);
            playVoice(voiceResIds, dialogIndex);
        }

        findViewById(R.id.level1Root).setOnClickListener(v -> {
            if (dialogFinished) return;
            // If still typing, show full text immediately
            if (typing) {
                cancelTyping();
                dialogBox.setText(dialogs[dialogIndex]);
                return;
            }
            if (soundReady) soundPool.play(dialogClickId, soundVolume, soundVolume, 1, 0, 1f);
            dialogIndex++;
            if (dialogIndex < dialogs.length) {
                typeText(dialogBox, dialogs[dialogIndex]);
                dialogCharacter.setImageResource(expressions[dialogIndex]);
                playVoice(voiceResIds, dialogIndex);
                if (dialogBgs != null && dialogIndex < dialogBgs.length && dialogBgs[dialogIndex] != 0) {
                    rootView.setBackgroundResource(dialogBgs[dialogIndex]);
                }
            } else {
                dialogFinished = true;
                dialogBox.setVisibility(View.GONE);
                dialogCharacter.setVisibility(View.GONE);
                btnSkip.clearAnimation();
                btnSkip.setVisibility(View.GONE);
                gameView.setVisibility(View.VISIBLE);
                gameView.loadLevel(getLevelData());
                showCountdownOverlay(countdownOverlay, countdownText, gameView, btnHint, hintResId);
            }
        });

        FirebaseFirestore.getInstance()
                .collection("test")
                .add(new HashMap<>())
                .addOnSuccessListener(doc -> Log.d("TEST", "WRITE OK"))
                .addOnFailureListener(e -> Log.e("TEST", "WRITE FAIL", e));

        gameView.setOnLevelCompleteListener(() -> {
            long finishMs = elapsedPlayMs();
            int steps = gameView.getMoveCount();
            LevelScoreConfig config = getScoreConfig();
            int score = LevelTimeScore.calculateScore(finishMs, steps, config);
            int stars = LevelTimeScore.calculateStars(finishMs, steps, config);
            resetLevelTimerUi();

            Intent result = new Intent(this, LevelResultActivity.class);

            result.putExtra("stars", stars);

            String next = getNextLevelClass();
            // Also check intent extra for next level (used by bathroom mode etc.)
            if (next == null && getIntent() != null) {
                next = getIntent().getStringExtra("nextLevel");
            }
            if (next != null) {
                result.putExtra("nextLevel", next);
            }

            result.putExtra("level", getLevelNumber());
            result.putExtra(LevelResultActivity.EXTRA_FINISH_TIME_MS, finishMs);
            result.putExtra(LevelResultActivity.EXTRA_STEPS, steps);
            result.putExtra(LevelResultActivity.EXTRA_SCORE, score);

            startActivity(result);
            finish();
        });

        View btnPause = findViewById(R.id.btnPause);
        setupButtonAnimation(btnPause);
        btnPause.setOnClickListener(v -> {
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
            rootView.setBackgroundResource(getBackgroundResId());
            btnSkip.setVisibility(View.VISIBLE);
            btnHint.setVisibility(View.GONE);
            hintOverlay.setVisibility(View.GONE);
            countdownOverlay.setVisibility(View.GONE);
            mainHandler.removeCallbacksAndMessages(null);
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

    private void showCountdownOverlay(View overlay, com.speed.sofasogood.views.OutlinedTextView text, GameView gameView, View btnHint, int hintResId) {
        overlay.setVisibility(View.VISIBLE);
        text.setText("3");
        text.setTextSize(72);

        mainHandler.postDelayed(() -> text.setText("2"), 1000);
        mainHandler.postDelayed(() -> text.setText("1"), 2000);

        gameView.setOnDropCompleteListener(() -> {
            text.setText("Start!");
            text.setTextSize(52);
            mainHandler.postDelayed(() -> {
                overlay.setVisibility(View.GONE);
                text.setTextSize(72);
                startLevelTimer();
                if (hintResId != 0) {
                    btnHint.setVisibility(View.VISIBLE);
                }
                if (levelBgm != null && !levelBgm.isPlaying()) levelBgm.start();
            }, 800);
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GameView.MIC_REQUEST_CODE) {
            boolean granted = grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (gameView != null) gameView.onMicPermissionResult(granted);
        }
    }

    private static final long TYPE_DELAY = 40; // ms per character

    private void typeText(TextView tv, String text) {
        cancelTyping();
        typing = true;
        tv.setText("");
        typeRunnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < text.length()) {
                    tv.setText(text.substring(0, ++index));
                    mainHandler.postDelayed(this, TYPE_DELAY);
                } else {
                    typing = false;
                }
            }
        };
        mainHandler.post(typeRunnable);
    }

    private void cancelTyping() {
        if (typeRunnable != null) {
            mainHandler.removeCallbacks(typeRunnable);
            typeRunnable = null;
        }
        typing = false;
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
        button.setHapticFeedbackEnabled(false);
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
        mainHandler.removeCallbacksAndMessages(null);
        cancelTyping();
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