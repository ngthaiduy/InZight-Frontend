// com/example/inzightapp/api/finance/TransactionApiService.java
package com.example.inzightapp.api.finance;

import com.example.inzightapp.model.request.TransactionRequest;
import com.example.inzightapp.model.response.TransactionResponse;
import com.example.inzightapp.model.response.CategoryResponse;
import com.example.inzightapp.model.response.WalletResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransactionApiService {
    @GET("/api/categories")
    Call<List<CategoryResponse>> getCategories();

    @GET("/api/categories")
    Call<List<CategoryResponse>> getCategoriesByType(@Query("type") String type);


    @GET("/api/wallets")
    Call<List<WalletResponse>> getWallets();

    @POST("/api/transactions")
    Call<TransactionResponse> createTransaction(@Body TransactionRequest request);

    @GET("/api/transactions")
    Call<List<TransactionResponse>> getTransactions();

    @PUT("/api/transactions/{id}")
    Call<TransactionResponse> updateTransaction(@Path("id") Long id, @Body TransactionRequest request);

    @DELETE("/api/transactions/{id}")
    Call<Void> deleteTransaction(@Path("id") Long id);
}
