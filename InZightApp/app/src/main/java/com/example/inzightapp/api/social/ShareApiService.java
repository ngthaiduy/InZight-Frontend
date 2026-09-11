package com.example.inzightapp.api.social;

import com.example.inzightapp.model.response.ShareResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ShareApiService {
    // toggle share
    @POST("/shares/{postId}")
    Call<String> sharePost(@Path("postId") Long postId);

    // get count share
    @GET("/shares/{postId}/count")
    Call<Long> getShareCount(@Path("postId") Long postId);

    // get list share
    @GET("/shares/{postId}/shares")
    Call<List<ShareResponse>> getShares(@Path("postId") Long postId);
}
