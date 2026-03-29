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
import com.speed.sofasogood.game.GameView;
import com.speed.sofasogood.game.LevelResultActivity;
import com.speed.sofasogood.game.model.LevelData;

public class Level5Activity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private int dialogIndex = 0;
    private boolean dialogFinished = false;

    private final String[] dialogs = {
            "Bedroom looks cosy! Now the balcony.",
            "Plant pots and chairs are crammed in here.",
            "This narrow space is going to be tricky.",
            "Fresh air and a tidy balcony — perfect combo.",
            "Let's push things into place!"
    };

    private final int[] expressions = {
            R.drawable.character_happy,
            R.drawable.character_thinking,
            R.drawable.character_thinking,
            R.drawable.character_thinking,
            R.drawable.character_idea
    };

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level5);

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
        GameView gameView = findViewById(R.id.gameView);

        if (getIntent().getBooleanExtra("skipDialog", false)) {
            dialogFinished = true;
            dialogBox.setVisibility(View.GONE);
            dialogCharacter.setVisibility(View.GONE);
            gameView.setVisibility(View.VISIBLE);
            gameView.loadLevel(LevelData.LEVEL_5);
        } else {
            dialogBox.setText(dialogs[dialogIndex]);
        }

        findViewById(R.id.level5Root).setOnClickListener(v -> {
            if (dialogFinished) return;
            dialogIndex++;
            if (dialogIndex < dialogs.length) {
                dialogBox.setText(dialogs[dialogIndex]);
                dialogCharacter.setImageResource(expressions[dialogIndex]);
            } else {
                dialogFinished = true;
                dialogBox.setVisibility(View.GONE);
                dialogCharacter.setVisibility(View.GONE);
                gameView.setVisibility(View.VISIBLE);
                gameView.loadLevel(LevelData.LEVEL_5);
            }
        });

        gameView.setOnLevelCompleteListener(() -> {
            Intent result = new Intent(this, LevelResultActivity.class);
            result.putExtra("nextLevel", "com.speed.sofasogood.game.levels.Level6Activity");
            startActivity(result);
            finish();
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
            pauseOverlay.setVisibility(View.VISIBLE);
        });

        View btnResume = findViewById(R.id.btnResume);
        setupButtonAnimation(btnResume);
        btnResume.setOnClickListener(v -> pauseOverlay.setVisibility(View.GONE));

        View btnRestart = findViewById(R.id.btnRestart);
        setupButtonAnimation(btnRestart);
        btnRestart.setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
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
