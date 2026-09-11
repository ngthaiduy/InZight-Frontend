package com.example.inzightapp.api.finance;

import com.example.inzightapp.model.request.WalletRequest;
import com.example.inzightapp.model.response.WalletResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WalletApiService {
    @GET("/api/wallets")
    Call<List<WalletResponse>> getWallets();

    @POST("/api/wallets")
    Call<WalletResponse> createWallet(@Body WalletRequest request);

    @DELETE("/api/wallets/{id}")
    Call<Void> deleteWallet(@Path("id") Long id);

}
