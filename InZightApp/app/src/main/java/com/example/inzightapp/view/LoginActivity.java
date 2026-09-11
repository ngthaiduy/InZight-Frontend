package com.example.inzightapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.MainActivity;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.user.UserApiService; // Import service user
import com.example.inzightapp.api.auth.AuthApiService;
import com.example.inzightapp.model.request.LoginRequest;
import com.example.inzightapp.model.response.LoginResponse;
import com.example.inzightapp.model.response.UserResponse;
import com.example.inzightapp.utils.LoadingDialog;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUser, edtPass;
    private Button btnLogin;
    private AuthApiService authApiService;
    private UserApiService userApiService; // 1. Khai báo thêm UserApiService
    private boolean isAutoLogin = false; // Flag để biết có phải auto-login không
    private LoadingDialog loadingDialog; // Loading dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Khởi tạo các Service trước
        authApiService = ApiClient.getClient(this).create(AuthApiService.class);
        userApiService = ApiClient.getClient(this).create(UserApiService.class);
        
        // Kiểm tra auto-login TRƯỚC KHI hiển thị UI
        // Nếu có credentials đã lưu, tự động đăng nhập và không hiển thị màn hình login
        if (checkAutoLogin()) {
            // Nếu có auto-login, không cần setContentView vì sẽ chuyển sang MainActivity ngay
            return;
        }
        
        // Nếu không có credentials, mới hiển thị màn hình login
        setContentView(R.layout.activity_login);
        initLoginViews();
        
        // Khởi tạo loading dialog
        loadingDialog = new LoadingDialog(this);
    }

    /**
     * Kiểm tra và thực hiện auto-login nếu có credentials đã lưu
     * @return true nếu đã thực hiện auto-login, false nếu không có credentials
     */
    private boolean checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("saved_username", null);
        String savedPassword = prefs.getString("saved_password", null);
        
        // Nếu có username và password đã lưu, tự động đăng nhập
        if (savedUsername != null && savedPassword != null 
                && !savedUsername.isEmpty() && !savedPassword.isEmpty()) {
            Log.d("LoginActivity", "Auto-login detected, performing automatic login");
            
            // Đánh dấu là auto-login
            isAutoLogin = true;
            
            // Tự động thực hiện đăng nhập (không hiển thị UI)
            performLogin(savedUsername, savedPassword);
            return true;
        }
        
        return false;
    }
    
    private void performLogin(String username, String password) {
        // Hiển thị loading dialog
        if (loadingDialog != null) {
            loadingDialog.show();
        }
        
        LoginRequest req = new LoginRequest(username, password);

        // BƯỚC 1: GỌI API LOGIN
        authApiService.login(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Lưu Token và thông tin đăng nhập vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("jwt_token", response.body().getToken());
                    editor.putString("username", response.body().getUsername());
                    
                    // Lưu username và password để auto-login lần sau
                    editor.putString("saved_username", username);
                    editor.putString("saved_password", password);

                    // Quan trọng: Phải dùng apply() hoặc commit() ngay lập tức
                    // để ApiClient có thể đọc được token cho request tiếp theo
                    editor.apply();

                    // BƯỚC 2: KIỂM TRA ROLE ĐỂ CHUYỂN TRANG
                    checkRoleAndNavigate();

                } else {
                    // Ẩn loading dialog khi có lỗi
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    
                    // Nếu auto-login thất bại, xóa credentials đã lưu và hiển thị màn hình login
                    if (isAutoLogin) {
                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        prefs.edit()
                            .remove("saved_username")
                            .remove("saved_password")
                            .apply();
                        
                        // Hiển thị màn hình login
                        setContentView(R.layout.activity_login);
                        initLoginViews();
                        isAutoLogin = false;
                    }
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Ẩn loading dialog khi có lỗi
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                
                // Nếu auto-login thất bại do network error, xóa credentials và hiển thị màn hình login
                if (isAutoLogin) {
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit()
                        .remove("saved_username")
                        .remove("saved_password")
                        .apply();
                    
                    // Hiển thị màn hình login
                    setContentView(R.layout.activity_login);
                    initLoginViews();
                    isAutoLogin = false;
                }
                Toast.makeText(LoginActivity.this, getString(R.string.network_error, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Khởi tạo các views cho màn hình login
     */
    private void initLoginViews() {
        edtUser = findViewById(R.id.edtUserName);
        edtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Pre-fill username nếu có
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("saved_username", null);
        if (savedUsername != null && !savedUsername.isEmpty() && edtUser != null) {
            edtUser.setText(savedUsername);
        }

        btnLogin.setOnClickListener(v -> {
            String u = edtUser.getText().toString().trim();
            String p = edtPass.getText().toString().trim();

            if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
                Toast.makeText(this, getString(R.string.enter_username_password), Toast.LENGTH_SHORT).show();
                return;
            }

            // Bắt đầu quy trình đăng nhập
            performLogin(u, p);
        });

        // Sign up navigation
        findViewById(R.id.tvSignup).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });
    }

    private void checkRoleAndNavigate() {
        // Lấy role từ SharedPreferences trước (nếu có từ lần login trước)
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String cachedRole = prefs.getString("role", null);
        
        // Navigate ngay dựa trên cached role (nếu có), không chờ API
        // getCurrentUser() sẽ được gọi trong background để cập nhật thông tin
        Intent intent;
        
        if ("ADMIN".equalsIgnoreCase(cachedRole)) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else {
            // Mặc định là USER, navigate đến MainActivity
            if (!isAutoLogin) {
                Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
            }
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        
        // Ẩn loading dialog trước khi navigate
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        
        // Xóa LoginActivity khỏi stack để user không bấm Back quay lại được màn hình login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        // Gọi getCurrentUser() trong background để cập nhật user info (không block navigation)
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String role = user.getRole(); // Lấy Role (ADMIN hoặc USER)
                    String rank = user.getRank() != null ? user.getRank() : "FREE"; // Lấy Rank

                    // Lưu userId + Role + Rank vào Prefs
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putLong("userId", user.getId() != null ? user.getId() : -1L)
                            .putString("role", role)
                            .putString("rank", rank)
                            .putBoolean("should_show_new_feature_popup", true) // Đánh dấu cần hiển thị popup sau khi login
                            .apply();
                    
                    Log.d("LoginActivity", "User info loaded in background - userId: " + user.getId() + ", role: " + role + ", rank: " + rank);
                    
                    // Nếu role thay đổi (ví dụ: từ USER thành ADMIN), cần navigate lại
                    // Nhưng trường hợp này hiếm, nên không cần xử lý ngay
                } else {
                    Log.e("LoginActivity", "Failed to load user info in background: " + (response != null ? response.code() : "null"));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("LoginActivity", "Failed to load user info in background", t);
                // Không cần xử lý, user đã vào MainActivity rồi
                // HomeFragment sẽ tự động refresh user info khi load
            }
        });
    }
}