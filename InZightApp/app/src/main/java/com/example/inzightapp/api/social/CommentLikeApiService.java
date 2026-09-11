package com.example.inzightapp.api.social;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentLikeApiService {

    @POST("/api/comment-likes/{commentId}/like")
    Call<String> toggleLike(@Path("commentId") Long commentId);

    @GET("/api/comment-likes/{commentId}/count")
    Call<Long> getLikeCount(@Path("commentId") Long commentId);
}
