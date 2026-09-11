package com.example.inzightapp.api;


import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;


    public static final String BASE_URL = "http://10.0.2.2:8080/";
    
    // Timeout settings (in seconds) - giảm thời gian chờ để fail nhanh hơn nếu có vấn đề
    private static final int CONNECT_TIMEOUT = 10; // 10 giây để thiết lập kết nối
    private static final int READ_TIMEOUT = 15;    // 15 giây để đọc response
    private static final int WRITE_TIMEOUT = 15;    // 15 giây để ghi request
    
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Configure timeouts để tránh chờ quá lâu
            httpClient.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    // Retry on connection failure (chỉ retry 1 lần)
                    .retryOnConnectionFailure(true);

            // Add interceptor to add JWT token
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                // Không add token cho login/register
                String url = original.url().toString();
                if (url.contains("/api/auth/login")
                        || url.contains("/api/auth/init-register")
                        || url.contains("/api/auth/verify-otp")) {
                    return chain.proceed(original);
                }


                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("jwt_token", null);

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Content-Type", "application/json");

                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });


            String baseUrl;

            if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("emulator")) {
                // Nếu đang chạy trên Android Emulator
                baseUrl = "http://10.0.2.2:8080/";
            } else {
                // ngrok
                baseUrl = BASE_URL;

            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient.build())
                    .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}
