package com.speed.sofasogood.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speed.sofasogood.R;
import com.speed.sofasogood.adapters.LeaderboardAdapter;
import com.speed.sofasogood.models.LeaderboardResponse;
import com.speed.sofasogood.network.LeaderboardApi;
import com.speed.sofasogood.network.RetrofitClient;
import com.speed.sofasogood.utils.ImmersiveHelper;
import com.speed.sofasogood.utils.UserInfoHelper;
import com.speed.sofasogood.utils.LocaleHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 8;

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvLevelLabel, tvEmpty;
    private View btnPrevLevel, btnNextLevel;

    private SoundPool soundPool;
    private int clickSoundId;
    private boolean soundReady = false;
    private float soundVolume = 1.0f;

    private int currentLevel = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        ImmersiveHelper.enable(getWindow());
        new UserInfoHelper().setup(this);

        // Sound
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .build();
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> soundReady = true);
        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        soundVolume = prefs.getFloat("sound_volume", 1.0f);

        // Views
        recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        progressBar = findViewById(R.id.progressBar);
        tvLevelLabel = findViewById(R.id.tvLevelLabel);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnPrevLevel = findViewById(R.id.btnPrevLevel);
        btnNextLevel = findViewById(R.id.btnNextLevel);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        // Initial level from intent
        if (getIntent() != null) {
            currentLevel = getIntent().getIntExtra("level", 1);
        }

        // Level navigation
        btnPrevLevel.setOnClickListener(v -> {
            if (currentLevel > MIN_LEVEL) {
                currentLevel--;
                refreshLevel();
            }
        });
        btnNextLevel.setOnClickListener(v -> {
            if (currentLevel < MAX_LEVEL) {
                currentLevel++;
                refreshLevel();
            }
        });

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        setupButtonAnimation(btnBack);
        btnBack.setOnClickListener(v -> finish());

        refreshLevel();
    }

    private void refreshLevel() {
        tvLevelLabel.setText("Level " + currentLevel);
        btnPrevLevel.setVisibility(currentLevel <= MIN_LEVEL ? View.INVISIBLE : View.VISIBLE);
        btnNextLevel.setVisibility(currentLevel >= MAX_LEVEL ? View.INVISIBLE : View.VISIBLE);
        loadLeaderboard(currentLevel);
    }

    private void loadLeaderboard(int level) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        adapter.clear();

        LeaderboardApi api = RetrofitClient.getClient().create(LeaderboardApi.class);
        api.getTopScores(level).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardResponse> call,
                                   @NonNull Response<LeaderboardResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load leaderboard");
                    Toast.makeText(LeaderboardActivity.this,
                            "HTTP " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                LeaderboardResponse leaderboardResponse = response.body();

                if (leaderboardResponse == null
                        || leaderboardResponse.getEntries() == null
                        || leaderboardResponse.getEntries().isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No leaderboard data for this level");
                    return;
                }

                adapter.setEntryList(leaderboardResponse.getEntries());
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Network error");
                Toast.makeText(LeaderboardActivity.this,
                        "Network error: " + t.getClass().getSimpleName() + ": " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
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
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
