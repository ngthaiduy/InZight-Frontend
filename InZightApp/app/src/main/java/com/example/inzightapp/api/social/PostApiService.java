package com.example.inzightapp.api.social;

import com.example.inzightapp.model.request.PostRequest;
import com.example.inzightapp.model.response.PostResponse;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PostApiService {
    @GET("/posts")
    Call<List<PostResponse>> getAllPosts();

    @GET("posts/me")
    Call<List<PostResponse>> getMyPosts();

    // Gửi multipart/form-data với file ảnh (image có thể null)
    @Multipart
    @POST("/posts")
    Call<PostResponse> createPost(
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("/posts/{id}")
    Call<PostResponse> updatePost(
            @Path("id") Long id,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image
    );

    @PUT("/posts/{id}")
    Call<PostResponse> updatePost(@Path("id") Long id, @Body PostRequest request);

    @DELETE("/posts/{id}")
    Call<Void> deletePost(@Path("id") Long id);
}
