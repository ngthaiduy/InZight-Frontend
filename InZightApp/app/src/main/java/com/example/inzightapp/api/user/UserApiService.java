package com.example.inzightapp.api.user;

import android.graphics.pdf.PdfDocument;

import com.example.inzightapp.model.request.AdminCreateUserRequest;
import com.example.inzightapp.model.request.AdminUpdateUserRequest;
import com.example.inzightapp.model.response.PageResponse;
import com.example.inzightapp.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApiService {
    @GET("/api/users/{id}")
    Call<UserResponse> getUserById(@Path("id") Long id);

    @GET("/api/users/me")
    Call<UserResponse> getCurrentUser();

    // --- ADMIN APIs ---
    @GET("/api/users")
    Call<PageResponse<UserResponse>> getAllUsers(
            @Query("page") int page,
            @Query("size") int size
    );
    @POST("/api/users")
    Call<UserResponse> createUser(@Body AdminCreateUserRequest request);

    @PUT("/api/users/{id}")
    Call<UserResponse> updateUser(@Path("id") Long id, @Body AdminUpdateUserRequest request);

    @DELETE("/api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);
}
