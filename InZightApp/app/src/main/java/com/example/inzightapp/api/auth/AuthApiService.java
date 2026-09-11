package com.example.inzightapp.api.auth;

import com.example.inzightapp.model.request.LoginRequest;
import com.example.inzightapp.model.request.RegisterRequest;
import com.example.inzightapp.model.request.VerifyOtpRequest;
import com.example.inzightapp.model.response.AuthResponse;
import com.example.inzightapp.model.response.InitRegisterResponse;
import com.example.inzightapp.model.response.LoginResponse;
import com.example.inzightapp.model.response.MessageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    // LOGIN
    @POST("api/auth/init-register")
    Call<InitRegisterResponse> initRegister(@Body RegisterRequest request);

    @POST("api/auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

}
