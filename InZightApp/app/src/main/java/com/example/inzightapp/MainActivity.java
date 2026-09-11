package com.example.inzightapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.payment.PaymentApiService;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.fragments.PaymentPlanFragment;
import com.example.inzightapp.fragments.TransactionHistoryFragment;
import com.example.inzightapp.fragments.HomeFragment;
import com.example.inzightapp.fragments.MessageFragment;
import com.example.inzightapp.fragments.SettingFragment;
import com.example.inzightapp.fragments.SocialFragment;
import com.example.inzightapp.model.response.UserResponse;
import com.example.inzightapp.utils.BottomNavHelper;
import com.example.inzightapp.utils.LoadingDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo loading dialog
        loadingDialog = new LoadingDialog(this);
        
        // Hiển thị loading khi vào app lần đầu (nếu cần)
        // Loading sẽ được ẩn khi HomeFragment load xong dữ liệu
        if (savedInstanceState == null) {
            // Chỉ hiển thị loading nếu user mới login (có thể cần load dữ liệu)
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            boolean shouldShowNewFeaturePopup = prefs.getBoolean("should_show_new_feature_popup", false);
            if (shouldShowNewFeaturePopup) {
                // User mới login, có thể cần load dữ liệu
                loadingDialog.show();
            }
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
        }
//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.container, new PaymentPlanFragment())
//                    .commit();
//        }

        // Setup bottom navigation với indicator
        BottomNavHelper.setupBottomNavWithIndicator(bottomNavigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_social) {
                selected = new SocialFragment();
            } else if (id == R.id.nav_transaction) {
                selected = new TransactionHistoryFragment();
            } else if (id == R.id.nav_message) {
                selected = new MessageFragment();
            } else if (id == R.id.nav_setting) {
                selected = new SettingFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selected)
                        .commit();
            }
            // Cập nhật indicator sau khi chọn item
            bottomNavigation.post(() -> BottomNavHelper.updateIndicators(bottomNavigation));
            return true;
        });

        // Xử lý deep link từ PayOS để điều hướng về Home
        handleDeepLink(getIntent());
    }
    public void setBottomNavVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(isVisible ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
    
    /**
     * Ẩn loading dialog khi dữ liệu đã load xong
     */
    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data == null) return;

        String path = data.getPath();
        if (path == null) return;

        if (path.equals("/payment-success")) {
            // Refresh rank trước khi navigate về Home
            refreshUserRankAfterPayment(() -> {
                navigateHomeFromDeepLink(getString(R.string.payment_success));
            });
        } else if (path.equals("/payment-cancel")) {
            navigateHomeFromDeepLink(getString(R.string.payment_cancelled));
        }
    }

    /**
     * Refresh rank sau khi payment success để đảm bảo rank được cập nhật thành PREMIUM
     */
    private void refreshUserRankAfterPayment(Runnable onComplete) {
        UserApiService userApiService = ApiClient.getClient(this).create(UserApiService.class);
        
        // Lấy orderCode và verify payment trước khi refresh rank
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        Long orderCode = prefs.getLong("current_order_code", 0);
        
        Log.d("MainActivity", "refreshUserRankAfterPayment - orderCode: " + orderCode);
        
        // Nếu có orderCode, verify payment trước
        if (orderCode != null && orderCode != 0) {
            PaymentApiService paymentApiService = 
                ApiClient.getClient(this).create(PaymentApiService.class);
            
            // Delay nhỏ để đợi backend xử lý payment callback từ PayOS
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                paymentApiService.verifyPayment(orderCode).enqueue(new retrofit2.Callback<PaymentApiService.VerifyPaymentResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<PaymentApiService.VerifyPaymentResponse> call, 
                                         retrofit2.Response<PaymentApiService.VerifyPaymentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentApiService.VerifyPaymentResponse verifyResponse = response.body();
                            Log.d("MainActivity", "verifyPayment in MainActivity - success: " + verifyResponse.isSuccess());
                            
                            // Delay thêm để đợi backend cập nhật rank sau khi verify
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                refreshRankWithRetry(userApiService, onComplete, 0);
                            }, 1000);
                        } else {
                            Log.e("MainActivity", "verifyPayment failed in MainActivity");
                            refreshRankWithRetry(userApiService, onComplete, 0);
                        }
                    }
                    
                    @Override
                    public void onFailure(retrofit2.Call<PaymentApiService.VerifyPaymentResponse> call, Throwable t) {
                        Log.e("MainActivity", "verifyPayment onFailure in MainActivity", t);
                        refreshRankWithRetry(userApiService, onComplete, 0);
                    }
                });
            }, 500);
        } else {
            // Không có orderCode, chỉ refresh rank
            refreshRankWithRetry(userApiService, onComplete, 0);
        }
    }
    
    /**
     * Refresh rank với retry logic
     */
    private void refreshRankWithRetry(UserApiService userApiService, Runnable onComplete, int retryAttempt) {
        final int MAX_RETRIES = 5;
        final long[] RETRY_DELAYS = {1000, 2000, 3000, 5000, 5000}; // 1s, 2s, 3s, 5s, 5s
        
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String newRank = user.getRank() != null ? user.getRank() : "FREE";
                    
                    // Lưu rank vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    String oldRank = prefs.getString("rank", "FREE");
                    prefs.edit().putString("rank", newRank).apply();
                    
                    Log.d("MainActivity", "Rank refresh attempt " + (retryAttempt + 1) + ": " + oldRank + " -> " + newRank);
                    
                    // Nếu rank chưa phải PREMIUM và chưa hết retry, retry sau delay
                    if (!"PREMIUM".equalsIgnoreCase(newRank) && retryAttempt < MAX_RETRIES) {
                        long delay = RETRY_DELAYS[retryAttempt];
                        Log.d("MainActivity", "Rank still not PREMIUM, retrying after " + delay + "ms");
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            refreshRankWithRetry(userApiService, onComplete, retryAttempt + 1);
                        }, delay);
                    } else {
                        if (!"PREMIUM".equalsIgnoreCase(newRank)) {
                            Log.w("MainActivity", "Rank still not PREMIUM after all retries. Final rank: " + newRank);
                        } else {
                            Log.d("MainActivity", "Rank successfully updated to PREMIUM!");
                        }
                        if (onComplete != null) onComplete.run();
                    }
                } else {
                    Log.e("MainActivity", "getCurrentUser failed - response not successful: " + (response != null ? response.code() : "null"));
                    if (retryAttempt < MAX_RETRIES) {
                        long delay = RETRY_DELAYS[retryAttempt];
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            refreshRankWithRetry(userApiService, onComplete, retryAttempt + 1);
                        }, delay);
                    } else {
                        if (onComplete != null) onComplete.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("MainActivity", "getCurrentUser onFailure - attempt " + (retryAttempt + 1), t);
                if (retryAttempt < MAX_RETRIES) {
                    long delay = RETRY_DELAYS[retryAttempt];
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        refreshRankWithRetry(userApiService, onComplete, retryAttempt + 1);
                    }, delay);
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }
        });
    }

    private void navigateHomeFromDeepLink(String toastMessage) {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();

        if (toastMessage != null) {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
