package com.example.inzightapp.api;

import com.example.inzightapp.model.request.RegisterRequest;
import com.example.inzightapp.model.request.TransactionRequest;
import com.example.inzightapp.model.response.StatisticResponse;
import com.example.inzightapp.model.response.TransactionResponse;
import com.example.inzightapp.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {



    @GET("api/users/{id}")
    Call<UserResponse> getUser(@Path("id") Long id);

    @GET("api/transactions/statistics")
    Call<StatisticResponse> getStatistics(@Query("type") String type);

}
