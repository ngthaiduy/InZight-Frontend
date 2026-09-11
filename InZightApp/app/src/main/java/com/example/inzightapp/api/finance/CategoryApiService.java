package com.example.inzightapp.api.finance;

import com.example.inzightapp.model.response.CategoryResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CategoryApiService {
    @GET("/api/categories")
    Call<List<CategoryResponse>> getCategories(@Query("type") String type);
}
