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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.speed.sofasogood.R;
import com.speed.sofasogood.activities.LeaderboardActivity;
import com.speed.sofasogood.models.LeaderboardSubmitRequest;
import com.speed.sofasogood.network.LeaderboardApi;
import com.speed.sofasogood.network.RetrofitClient;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.LocaleHelper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LevelResultActivity extends AppCompatActivity {

    public static final String EXTRA_FINISH_TIME_MS = "finishTimeMs";
    public static final String EXTRA_SCORE = "score";

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
        int level = getIntent().getIntExtra("level", 1);
        long finishTimeMs = getIntent().getLongExtra(EXTRA_FINISH_TIME_MS, 0L);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);

        TextView resultFinishTime = findViewById(R.id.resultFinishTime);
        TextView resultScore = findViewById(R.id.resultScore);
        resultFinishTime.setText(getString(R.string.label_finish_time, LevelTimeScore.formatElapsed(finishTimeMs)));
        resultScore.setText(getString(R.string.label_finish_score, score));

        // Stars
        int stars = LevelTimeScore.starsFromScore(score);
        ImageView star1 = findViewById(R.id.star1);
        ImageView star2 = findViewById(R.id.star2);
        ImageView star3 = findViewById(R.id.star3);
        star1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        star2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        star3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);

        submitScoreToLeaderboard(level, score, finishTimeMs);

        View btnLeaderboard = findViewById(R.id.btnLeaderboard);
        setupButtonAnimation(btnLeaderboard);
        btnLeaderboard.setOnClickListener(v -> {
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

    private void submitScoreToLeaderboard(int level, int score, long timeMs) {
        LeaderboardApi api = RetrofitClient.getClient().create(LeaderboardApi.class);
        LeaderboardSubmitRequest body = new LeaderboardSubmitRequest(level, score, timeMs, "");
        api.submitScore(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Best-effort; webhook may not implement POST yet.
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Ignore network errors for gameplay flow.
            }
        });
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
