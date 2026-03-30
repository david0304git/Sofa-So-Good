package com.speed.sofasogood.network;

import com.speed.sofasogood.models.LeaderboardResponse;
import com.speed.sofasogood.models.LeaderboardSubmitRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LeaderboardApi {
    @GET("leaderboard/top")
    Call<LeaderboardResponse> getTopScores(@Query("level") int level);

    @POST("leaderboard/submit")
    Call<ResponseBody> submitScore(@Body LeaderboardSubmitRequest body);
}
