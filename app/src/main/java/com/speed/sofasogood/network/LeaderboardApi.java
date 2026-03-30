package com.speed.sofasogood.network;

import com.speed.sofasogood.models.LeaderboardResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface LeaderboardApi {
    @GET("leaderboard/top")
    Call<LeaderboardResponse> getTopScores(@Query("level") int level);
}
