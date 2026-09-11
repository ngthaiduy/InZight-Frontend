package com.example.inzightapp.api.social;

import com.example.inzightapp.model.request.CommentRequest;
import com.example.inzightapp.model.response.CommentResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CommentApiService {
    @GET("/comments/post")
    Call<List<CommentResponse>> getCommentsByPost(@Query("postId") Long postId);

    @POST("/comments")
    Call<CommentResponse> addComment(@Body CommentRequest request);

    @PUT("/comments/{id}")
    Call<CommentResponse> updateComment(@Path("id") Long id, @Body CommentRequest request);

    @DELETE("/comments/{id}")
    Call<Void> deleteComment(@Path("id") Long id);
}
