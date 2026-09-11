package com.example.inzightapp.fragments;

// PaymentPlanFragment.java

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.payment.PaymentConfirmBottomSheet;
import com.example.inzightapp.adapter.payment.PaymentPlanAdapter;
import com.example.inzightapp.adapter.payment.PaymentSuccessDialog;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.payment.PaymentApiService;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.model.request.PaymentPlan;
import com.example.inzightapp.model.response.PaymentResponse;
import com.example.inzightapp.model.response.UserResponse;
import com.example.inzightapp.databinding.FragmentPaymentPlanBinding;
import com.example.inzightapp.fragments.HomeFragment;
import com.example.inzightapp.utils.TutorialHelper;

import android.os.Handler;
import android.os.Looper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class PaymentPlanFragment extends Fragment implements PaymentPlanAdapter.OnPlanSelectedListener, PaymentConfirmBottomSheet.OnPaymentConfirmListener {

    private FragmentPaymentPlanBinding binding;
    private PaymentPlanAdapter adapter;
    private PaymentPlan selectedPlan;
    private PaymentApiService paymentApiService;
    private UserApiService userApiService;
    private boolean isPaymentInProgress = false;
    private String previousRank = "FREE";
    private boolean hasShownSuccessDialog = false;
    private Long currentOrderCode = null; // Lưu orderCode của payment hiện tại
    private static final int RETRY_DELAY_MS = 500;
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public PaymentPlanFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo API services
        paymentApiService = ApiClient.getClient(requireContext()).create(PaymentApiService.class);
        userApiService = ApiClient.getClient(requireContext()).create(UserApiService.class);

        setupToolbar();
        setupRecyclerView();
        setupContinueButton();
        setupSystemBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kiểm tra và verify payment khi quay lại app
        if (isPaymentInProgress) {
            // Delay nhỏ để đảm bảo backend đã xử lý callback từ PayOS
            handler.postDelayed(() -> {
                verifyAndUpgradePremium();
            }, RETRY_DELAY_MS);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setTitle("Choose Payment Plan");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        binding.toolbar.setNavigationOnClickListener(v -> handleBack());
    }

    private void setupRecyclerView() {
        List<PaymentPlan> plans = new ArrayList<>();
        plans.add(new PaymentPlan(
                "free",
                "Free",
                "0đ",
                "Free with basic features",
                false
        ));
        plans.add(new PaymentPlan(
                "premium_1m",
                "Premium 1 Month",
                "100.000đ",
                "Pay monthly, cancel anytime",
                true
        ));
        plans.add(new PaymentPlan(
                "premium_6m",
                "Premium 6 Months",
                "555.000đ",
                "Save more compared to 1 month",
                false
        ));
        plans.add(new PaymentPlan(
                "premium_12m",
                "Premium Yearly",
                "999.000đ",
                "Best value for yearly plan",
                false
        ));

        adapter = new PaymentPlanAdapter(plans, this);
        binding.recyclerPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPlans.setAdapter(adapter);
    }

    private void setupContinueButton() {
        binding.btnContinue.setEnabled(false);
        binding.btnContinue.setBackgroundResource(R.drawable.bg_payment_button_disabled);

        binding.btnContinue.setOnClickListener(v -> {
            if (selectedPlan == null) {
                Toast.makeText(requireContext(), "Please select a payment plan", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // If FREE plan is selected, navigate to home
            if (selectedPlan.getId().equals("free")) {
                navigateToHome();
                return;
            }
            
            showConfirmBottomSheet();
        });
    }

    private void updateContinueButtonState() {
        boolean enabled = selectedPlan != null;
        binding.btnContinue.setEnabled(enabled);
        binding.btnContinue.setBackgroundResource(
                enabled ? R.drawable.bg_payment_button_enabled : R.drawable.bg_payment_button_disabled
        );
    }

    private void showConfirmBottomSheet() {
        PaymentConfirmBottomSheet sheet = PaymentConfirmBottomSheet.newInstance(
                selectedPlan.getName(),
                selectedPlan.getPriceDisplay(),
                selectedPlan.getBillingInfo()
        );
        sheet.setPaymentConfirmListener(this);
        sheet.show(getParentFragmentManager(), "PaymentConfirmBottomSheet");
    }

    /**
     * Map plan ID từ frontend sang backend format
     */
    private String mapPlanIdToBackend(String frontendPlanId) {
        switch (frontendPlanId) {
            case "premium_1m":
                return "VIP_1_MONTH";
            case "premium_6m":
                return "VIP_6_MONTH";
            case "premium_12m":
                return "VIP_12_MONTH";
            default:
                throw new IllegalArgumentException("Invalid plan: " + frontendPlanId);
        }
    }

    /**
     * Navigate to HomeFragment
     */
    private void navigateToHome() {
        if (getActivity() == null) return;
        
        // Navigate to HomeFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();
        
        // Update bottom navigation to home
        if (getActivity() instanceof com.example.inzightapp.MainActivity) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    /**
     * Gọi API để tạo payment link
     */
    private void createPaymentLink() {
        if (selectedPlan == null || selectedPlan.getId().equals("free")) {
            Toast.makeText(requireContext(), "Please select a valid payment plan", Toast.LENGTH_SHORT).show();
            return;
        }

        String backendPlanId = mapPlanIdToBackend(selectedPlan.getId());
        
        // Hiển thị loading
        binding.btnContinue.setEnabled(false);
        binding.btnContinue.setText("Processing...");

        paymentApiService.createPayment(backendPlanId).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (!isAdded() || getContext() == null) return;
                
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Continue");

                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse paymentResponse = response.body();
                    String paymentUrl = paymentResponse.getCheckoutUrl();
                    Long orderCode = paymentResponse.getOrderCode();
                    
                    // Lưu orderCode để verify sau
                    currentOrderCode = orderCode;
                    
                    // Lưu orderCode vào SharedPreferences để backup
                    android.content.SharedPreferences prefs = getContext()
                            .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putLong("current_order_code", orderCode).apply();
                    
                    // Mở payment URL trong browser
                    openPaymentUrl(paymentUrl);
                } else {
                    Toast.makeText(getContext(), "Unable to create payment link. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Continue");
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Mở payment URL trong browser
     */
    private void openPaymentUrl(String url) {
        if (!isAdded() || getContext() == null) return;
        
        // Lưu rank hiện tại trước khi thanh toán
        loadCurrentRankBeforePayment();
        
        // Lấy orderCode từ SharedPreferences nếu chưa có
        if (currentOrderCode == null) {
            android.content.SharedPreferences prefs = getContext()
                    .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
            currentOrderCode = prefs.getLong("current_order_code", 0);
        }
        
        isPaymentInProgress = true;
        hasShownSuccessDialog = false;
        retryCount = 0;
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Load rank hiện tại trước khi thanh toán để so sánh sau này
     */
    private void loadCurrentRankBeforePayment() {
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    previousRank = user.getRank() != null ? user.getRank() : "FREE";
                    
                    // Lưu vào SharedPreferences để backup
                    android.content.SharedPreferences prefs = getContext()
                            .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putString("previous_rank", previousRank).apply();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                // Nếu không load được, lấy từ SharedPreferences
                android.content.SharedPreferences prefs = getContext()
                        .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                previousRank = prefs.getString("previous_rank", "FREE");
            }
        });
    }

    /**
     * Verify payment - Gọi endpoint verify-payment để kiểm tra payment đã thành công chưa
     */
    private void verifyAndUpgradePremium() {
        if (!isAdded() || getContext() == null) return;
        
        // Lấy orderCode từ SharedPreferences nếu chưa có
        if (currentOrderCode == null) {
            android.content.SharedPreferences prefs = getContext()
                    .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
            currentOrderCode = prefs.getLong("current_order_code", 0);
        }
        
        android.util.Log.d("PaymentPlan", "verifyAndUpgradePremium - orderCode: " + currentOrderCode);
        
        if (currentOrderCode == null || currentOrderCode == 0) {
            // Nếu không có orderCode, fallback về cách cũ (check rank)
            android.util.Log.w("PaymentPlan", "No orderCode found, falling back to refreshUserInfo");
            refreshUserInfo();
            return;
        }
        
        // Lấy previousRank từ SharedPreferences nếu chưa có
        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        if ("FREE".equals(previousRank)) {
            previousRank = prefs.getString("previous_rank", "FREE");
        }
        
        android.util.Log.d("PaymentPlan", "verifyAndUpgradePremium - previousRank: " + previousRank);
        
        // Gọi endpoint verify-payment
        paymentApiService.verifyPayment(currentOrderCode).enqueue(new Callback<PaymentApiService.VerifyPaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentApiService.VerifyPaymentResponse> call, 
                                 Response<PaymentApiService.VerifyPaymentResponse> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    PaymentApiService.VerifyPaymentResponse verifyResponse = response.body();
                    android.util.Log.d("PaymentPlan", "verifyPayment response - success: " + verifyResponse.isSuccess() + ", message: " + verifyResponse.getMessage());
                    
                    if (verifyResponse.isSuccess()) {
                        // Payment đã được verify thành công, refresh user info để lấy rank mới
                        // Delay nhỏ để đợi backend cập nhật rank
                        handler.postDelayed(() -> {
                            if (isAdded()) {
                                refreshUserInfo();
                            }
                        }, 1000); // Delay 1 giây để đợi backend cập nhật rank
                    } else {
                        // Payment chưa được verify, retry sau
                        if (retryCount < MAX_RETRY_COUNT) {
                            retryCount++;
                            android.util.Log.d("PaymentPlan", "Payment not verified yet, retry " + retryCount + "/" + MAX_RETRY_COUNT);
                            handler.postDelayed(() -> {
                                if (isAdded()) {
                                    verifyAndUpgradePremium();
                                }
                            }, RETRY_DELAY_MS * (retryCount + 1));
                        } else {
                            // Đã hết retry, fallback về cách cũ
                            android.util.Log.w("PaymentPlan", "Max retry reached, falling back to refreshUserInfo");
                            refreshUserInfo();
                        }
                    }
                } else {
                    // Response không thành công, retry
                    android.util.Log.e("PaymentPlan", "verifyPayment failed - response not successful: " + response.code());
                    handleRetry();
                }
            }

            @Override
            public void onFailure(Call<PaymentApiService.VerifyPaymentResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                android.util.Log.e("PaymentPlan", "verifyPayment onFailure", t);
                // Lỗi kết nối, fallback về cách cũ (check rank)
                refreshUserInfo();
            }
        });
    }

    /**
     * Refresh thông tin user để cập nhật rank
     */
    private void refreshUserInfo() {
        android.util.Log.d("PaymentPlan", "refreshUserInfo - retryCount: " + retryCount);
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String newRank = user.getRank() != null ? user.getRank() : "FREE";
                    
                    android.util.Log.d("PaymentPlan", "refreshUserInfo - previousRank: " + previousRank + ", newRank: " + newRank);
                    
                    // Lưu rank vào SharedPreferences
                    android.content.SharedPreferences prefs = getContext()
                            .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putString("rank", newRank).apply();
                    
                    // Kiểm tra nếu rank đã thay đổi từ FREE sang PREMIUM
                    boolean isUpgraded = "FREE".equalsIgnoreCase(previousRank) 
                            && "PREMIUM".equalsIgnoreCase(newRank);
                    
                    android.util.Log.d("PaymentPlan", "refreshUserInfo - isUpgraded: " + isUpgraded + ", hasShownSuccessDialog: " + hasShownSuccessDialog);
                    
                    if (isUpgraded && !hasShownSuccessDialog) {
                        // Rank đã được upgrade thành công, không hiển thị popup nữa
                        // Chỉ đánh dấu để HomeFragment hiển thị tutorial arrow
                        android.util.Log.d("PaymentPlan", "Rank upgraded to PREMIUM! Tutorial arrow will be shown in HomeFragment");
                        hasShownSuccessDialog = true;
                        isPaymentInProgress = false;
                        
                        // Reset premium tutorial flag so user will see premium tutorial arrow
                        if (TutorialHelper.shouldShowPremiumTutorial(getContext())) {
                            // Tutorial arrow will be shown when user navigates to HomeFragment
                            android.util.Log.d("PaymentPlan", "Premium tutorial arrow will be shown on next Home visit");
                        }
                    } else if (!"PREMIUM".equalsIgnoreCase(newRank) && retryCount < MAX_RETRY_COUNT) {
                        // Nếu chưa upgrade và chưa hết số lần retry, thử lại sau delay
                        retryCount++;
                        android.util.Log.d("PaymentPlan", "Rank still not PREMIUM, retrying refreshUserInfo - retryCount: " + retryCount);
                        handler.postDelayed(() -> {
                            if (isAdded()) {
                                refreshUserInfo();
                            }
                        }, RETRY_DELAY_MS * (retryCount + 1));
                    } else {
                        // Đã hết retry hoặc không cần upgrade
                        if (!"PREMIUM".equalsIgnoreCase(newRank)) {
                            android.util.Log.w("PaymentPlan", "Rank still not PREMIUM after all retries. Current rank: " + newRank);
                        }
                        isPaymentInProgress = false;
                    }
                } else {
                    // Retry nếu response không thành công
                    android.util.Log.e("PaymentPlan", "getCurrentUser failed - response not successful: " + (response != null ? response.code() : "null"));
                    handleRetry();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                android.util.Log.e("PaymentPlan", "getCurrentUser onFailure", t);
                // Retry nếu có lỗi
                handleRetry();
            }
        });
    }

    /**
     * Xử lý retry logic
     */
    private void handleRetry() {
        if (!isAdded() || getContext() == null) return;
        
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            handler.postDelayed(() -> {
                if (isAdded()) {
                    refreshUserInfo();
                }
            }, RETRY_DELAY_MS * (retryCount + 1));
        } else {
            isPaymentInProgress = false;
        }
    }

    /**
     * Hiển thị dialog thông báo nâng cấp thành công
     * Đã tắt - thay bằng tutorial arrow trong HomeFragment
     */
    // private void showPaymentSuccessDialog() {
    //     PaymentSuccessDialog dialog = PaymentSuccessDialog.newInstance();
    //     dialog.show(getParentFragmentManager(), "PaymentSuccessDialog");
    // }

    @Override
    public void onPaymentConfirmed() {
        createPaymentLink();
    }

    private void setupSystemBackPressed() {
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBack();
                    }
                });
    }

    private void handleBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().finish();
        }
    }

    @Override
    public void onPlanSelected(@NonNull PaymentPlan plan) {
        selectedPlan = plan;
        updateContinueButtonState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}