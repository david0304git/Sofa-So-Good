package com.speed.sofasogood.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speed.sofasogood.R;
import com.speed.sofasogood.adapters.LeaderboardAdapter;
import com.speed.sofasogood.models.LeaderboardResponse;
import com.speed.sofasogood.network.LeaderboardApi;
import com.speed.sofasogood.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvEmpty;

    private int level = 1; // default level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvLeaderboardTitle);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (getIntent() != null) {
            level = getIntent().getIntExtra("level", 1);
        }

        tvTitle.setText("Level " + level + " Leaderboard");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        loadLeaderboard(level);
    }

    private void loadLeaderboard(int level) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        LeaderboardApi api = RetrofitClient.getClient().create(LeaderboardApi.class);
        Call<LeaderboardResponse> call = api.getTopScores(level);

        call.enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardResponse> call,
                                   @NonNull Response<LeaderboardResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    LeaderboardResponse leaderboardResponse = response.body();

                    if (leaderboardResponse.getEntries() != null &&
                            !leaderboardResponse.getEntries().isEmpty()) {
                        adapter.setEntryList(leaderboardResponse.getEntries());
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No leaderboard data yet.");
                    }
                } else {
                    Toast.makeText(LeaderboardActivity.this,
                            "Failed to load leaderboard: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
