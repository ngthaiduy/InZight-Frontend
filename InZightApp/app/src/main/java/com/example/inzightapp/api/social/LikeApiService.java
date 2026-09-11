package com.example.inzightapp.api.social;

import com.example.inzightapp.model.response.LikeResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LikeApiService {
    @POST("/likes/{postId}/like")
    Call<String> toggleLike(@Path("postId") Long postId);

    @GET("/likes/{postId}/likes")
    Call<List<LikeResponse>> getLikes(@Path("postId") Long postId);

    @GET("/likes/{postId}/likes/count")
    Call<Long> getLikeCount(@Path("postId") Long postId);
}
