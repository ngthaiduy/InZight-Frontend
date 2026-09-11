package com.example.inzightapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.MainActivity;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.auth.AuthApiService;
import com.example.inzightapp.model.request.LoginRequest;
import com.example.inzightapp.model.request.VerifyOtpRequest;
import com.example.inzightapp.model.response.AuthResponse;
import com.example.inzightapp.model.response.UserResponse;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.model.response.LoginResponse;
import com.example.inzightapp.utils.TutorialHelper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class VerifyOtpActivity extends AppCompatActivity {

    private EditText edtOtpCode;
    private Button btnConfirm;
    private AuthApiService authApiService;
    private UserApiService userApiService;

    private String registrationToken;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        edtOtpCode = findViewById(R.id.edtOtpCode);
        btnConfirm = findViewById(R.id.btnConfirm);

        authApiService = ApiClient.getClient(this).create(AuthApiService.class);
        userApiService = ApiClient.getClient(this).create(UserApiService.class);

        registrationToken = getIntent().getStringExtra("registrationToken");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        btnConfirm.setOnClickListener(v -> {
            String otp = edtOtpCode.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_otp), Toast.LENGTH_SHORT).show();
                return;
            }

            VerifyOtpRequest request = new VerifyOtpRequest(registrationToken, otp);

            authApiService.verifyOtp(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(VerifyOtpActivity.this, getString(R.string.otp_verification_failed), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // LƯU TOKEN
                    AuthResponse auth = response.body();

                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt_token", auth.getToken())
                            .putString("username", auth.getUsername())
                            .putBoolean("should_show_new_feature_popup", true) // Đánh dấu cần hiển thị popup sau khi register
                            .apply();

                    // Reset basic tutorial state để user mới đăng ký sẽ được hướng dẫn
                    TutorialHelper.resetBasicTutorial(VerifyOtpActivity.this);
                    
                    // Navigate ngay đến MainActivity, không chờ getCurrentUser()
                    // getCurrentUser() sẽ được gọi trong MainActivity/HomeFragment để không block UI
                    startActivity(new Intent(VerifyOtpActivity.this, MainActivity.class));
                    finishAffinity();
                    
                    // Gọi getCurrentUser() trong background (không block navigation)
                    userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> userRes) {
                            if (userRes.isSuccessful() && userRes.body() != null) {
                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                UserResponse user = userRes.body();
                                String rank = user.getRank() != null ? user.getRank() : "FREE";
                                prefs.edit()
                                        .putLong("userId", user.getId() != null ? user.getId() : -1L)
                                        .putString("rank", rank)
                                        .apply();
                                
                                Log.d("VerifyOtp", "User info loaded in background - userId: " + user.getId() + ", rank: " + rank);
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Log.e("VerifyOtp", "Failed to load user info in background", t);
                            // Không cần xử lý, user đã vào MainActivity rồi
                            // HomeFragment sẽ tự động refresh user info khi load
                        }
                    });
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(VerifyOtpActivity.this, getString(R.string.network_error, t.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
