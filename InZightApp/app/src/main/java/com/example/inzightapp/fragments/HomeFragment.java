package com.example.inzightapp.fragments;

import android.app.Dialog;
import android.graphics.Color;
import java.math.BigDecimal;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.Toast;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.example.inzightapp.utils.CategoryUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Category.CategoryStatAdapter;
import com.example.inzightapp.adapter.Home.FeatureIntroAdapter;
import com.example.inzightapp.adapter.Home.WrapContentLinearLayoutManager;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.ApiService;
import com.example.inzightapp.api.finance.CategoryApiService;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.api.finance.WalletApiService;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.model.response.UserResponse;
import com.example.inzightapp.model.response.CategoryResponse;
import com.example.inzightapp.model.response.CategoryStatistic;
import com.example.inzightapp.model.response.StatisticResponse;
import com.example.inzightapp.model.response.TransactionResponse;
import com.example.inzightapp.model.response.WalletResponse;
import com.example.inzightapp.view.FinbotChatActivity;
import com.example.inzightapp.utils.TutorialHelper;
import com.example.inzightapp.utils.TutorialOverlay;
import com.example.inzightapp.utils.TutorialArrowView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.card.MaterialCardView;

import android.content.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private PieChart pieChart;
    private ProgressBar progressBarChart;
    private RecyclerView recyclerView;
    private CategoryStatAdapter adapter;
    private ApiService apiService;

    private WalletApiService walletApiService;

    private CategoryApiService categoryApiService;

    private TransactionApiService transactionApiService;
    
    private UserApiService userApiService;

    // Các view hiển thị tổng

    private LinearLayout boxExpense, boxIncome;
    private TextView tvExpenseAmount, tvIncomeAmount;
    private TextView btnExpense, btnIncome;
    private LinearLayout btnNavMultiGoal;

    private FloatingActionButton btnAddTransaction;
    private String currentType = "EXPENSE"; // mặc định
    
    // Tutorial arrow view
    private TutorialArrowView tutorialArrowView;
    
    // ViewPager2 cho feature intro
    private ViewPager2 viewPagerFeatures;
    private LinearLayout indicatorContainer;
    private FeatureIntroAdapter featureAdapter;
    private ScrollView homeScrollView;

    // Auto-scroll cho ViewPager2
    private static final long AUTO_SCROLL_INTERVAL = 4000L; // 4 giây
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || getContext() == null) return;
            if (viewPagerFeatures != null && featureAdapter != null && featureAdapter.getItemCount() > 0) {
                int current = viewPagerFeatures.getCurrentItem();
                int next = (current + 1) % featureAdapter.getItemCount();
                viewPagerFeatures.setCurrentItem(next, true);

                autoScrollHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        homeScrollView = view.findViewById(R.id.homeScrollView);

        LinearLayout btnNavMultiGoal = view.findViewById(R.id.btnNavMultiGoal);

        btnNavMultiGoal.setOnClickListener(v -> {
            showIntroMultiGoalDialog();
        });

        // Pie chart
        pieChart = view.findViewById(R.id.pieChart);
        progressBarChart = view.findViewById(R.id.progressBarChart);

        // Bot Image (GIF)
        ImageView imgBot = view.findViewById(R.id.imgBot);
        Glide.with(this)
                .load(R.drawable.imagebot) // Local PNG
                .into(imgBot);

        // Total boxes
        boxExpense = view.findViewById(R.id.boxExpense);
        boxIncome = view.findViewById(R.id.boxIncome);
        tvExpenseAmount = view.findViewById(R.id.tvIncomeAmount);
        tvIncomeAmount = view.findViewById(R.id.tvExpenseAmount);

        // Tabs
        btnExpense = view.findViewById(R.id.tvExpenseLabel);
        btnIncome = view.findViewById(R.id.tvIncomeLabel);

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));

        //  Floating button
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        // Lưu original listener để có thể restore sau khi tutorial arrow được hide
        View.OnClickListener originalAddTransactionListener = v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        };
        btnAddTransaction.setTag(R.id.original_click_listener, originalAddTransactionListener);
        btnAddTransaction.setOnClickListener(originalAddTransactionListener);

        // Wire Retire Navigation
        View btnNavRetire = view.findViewById(R.id.btnNavRetire);
        if (btnNavRetire != null) {
            btnNavRetire.setOnClickListener(v -> {
                checkRankAndNavigate(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new RetirementInputFragment())
                            .addToBackStack(null)
                            .commit();
                });
            });
        }

        // Multi-goal Navigation
        btnNavMultiGoal = view.findViewById(R.id.btnNavMultiGoal);
        btnNavMultiGoal.setOnClickListener(v -> {
            // Hide tutorial arrow if showing
            hideTutorialArrow();
            
            checkRankAndNavigate(() -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new MultiGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        });

        // Optimizer Navigation
        View btnNavOptimizer = view.findViewById(R.id.btnNavOptimizer);
        if (btnNavOptimizer != null) {
            btnNavOptimizer.setOnClickListener(v -> {
                checkRankAndNavigate(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new OptimizerInputFragment())
                            .addToBackStack(null)
                            .commit();
                });
            });
        }

        // Scenario Navigation
        View btnNavScenario = view.findViewById(R.id.btnNavScenario);
        if (btnNavScenario != null) {
            btnNavScenario.setOnClickListener(v -> {
                checkRankAndNavigate(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new ScenarioInputFragment())
                            .addToBackStack(null)
                            .commit();
                });
            });
        }


        // API client
        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        userApiService = ApiClient.getClient(getContext()).create(UserApiService.class);


        // Mặc định hiển thị Expense
        selectTab("EXPENSE");

        // Chuyển tab
        btnExpense.setOnClickListener(v -> selectTab("EXPENSE"));
        btnIncome.setOnClickListener(v -> selectTab("INCOME"));

        // Button "TRY NOW" trong home (chuyển sang FinbotChatActivity)
        MaterialButton btnTryNow = view.findViewById(R.id.btnTryNow);
        if (btnTryNow != null) {
            btnTryNow.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), FinbotChatActivity.class);
                startActivity(intent);
            });
        }

        // Setup ViewPager2 cho feature intro
        setupFeatureViewPager(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Load user info ngay khi vào app lần đầu (đặc biệt cho user mới đăng ký)
        // Đảm bảo userId và rank được load ngay, không cần chờ onResume
        refreshUserInfoOnFirstLoad();
        
        // Popup tính năng mới (thay thế banner) - hiển thị sau khi view đã sẵn sàng
        // Delay một chút để đảm bảo fragment đã load xong
        view.postDelayed(() -> {
            if (isAdded() && getContext() != null && isResumed()) {
                // Check and show tutorial first (for new users)
                checkAndShowTutorial(view);
                // Then show new feature popup (for existing users)
                showNewFeaturePopup();
            }
        }, 800); // Delay 800ms to ensure views are fully loaded
    }
    
    /**
     * Load user info ngay khi vào app lần đầu (đặc biệt cho user mới đăng ký)
     * Đảm bảo userId và rank được load ngay, không cần chờ onResume
     */
    private void refreshUserInfoOnFirstLoad() {
        if (getContext() == null || userApiService == null) return;
        
        // Kiểm tra xem đã có userId chưa, nếu chưa có thì load ngay
        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1L);
        
        // Nếu chưa có userId (user mới đăng ký), load ngay
        if (userId == -1L) {
            userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (getContext() == null) return;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        UserResponse user = response.body();
                        String rank = user.getRank() != null ? user.getRank() : "FREE";
                        
                        android.content.SharedPreferences prefs = getContext()
                                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                        prefs.edit()
                                .putLong("userId", user.getId() != null ? user.getId() : -1L)
                                .putString("rank", rank)
                                .apply();
                        
                        android.util.Log.d("HomeFragment", "User info loaded on first load - userId: " + user.getId() + ", rank: " + rank);
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    android.util.Log.e("HomeFragment", "Failed to load user info on first load", t);
                    // Không cần xử lý, sẽ retry trong onResume
                }
            });
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up tutorial arrow when view is destroyed
        hideTutorialArrow();
    }

    private void setupFeatureViewPager(View view) {
        viewPagerFeatures = view.findViewById(R.id.viewPagerFeatures);
        indicatorContainer = view.findViewById(R.id.indicatorContainer);
        
        featureAdapter = new FeatureIntroAdapter();
        
        // Xóa click listener cho ViewPager2 - không còn navigate đến PaymentPlanFragment nữa
        // featureAdapter.setOnItemClickListener(position -> openPaymentPlanFragment());
        
        viewPagerFeatures.setAdapter(featureAdapter);
        
        // Tạo indicator dots
        createIndicators(featureAdapter.getItemCount());
        
        // Cập nhật indicator khi vuốt
        viewPagerFeatures.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
        
        // Bắt đầu auto scroll sau khi setup
        startAutoScroll();
    }

    /**
     * Popup tính năng mới: chỉ hiện cho user FREE, hiển thị dạng popup nổi
     * Popup chỉ hiển thị sau khi login vào app (không hiển thị lại khi quay lại HomeFragment)
     */
    private void showNewFeaturePopup() {
        if (!isAdded() || getContext() == null) return;

        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        
        // Kiểm tra flag - chỉ hiển thị nếu vừa login
        boolean shouldShow = prefs.getBoolean("should_show_new_feature_popup", false);
        if (!shouldShow) {
            return; // Không phải lần đầu sau login, không hiển thị
        }

        // Kiểm tra rank từ SharedPreferences
        String rank = prefs.getString("rank", "FREE");

        // Chỉ hiển thị cho user FREE
        if ("PREMIUM".equalsIgnoreCase(rank)) {
            // Clear flag nếu là PREMIUM
            prefs.edit().putBoolean("should_show_new_feature_popup", false).apply();
            return;
        }

        // Tạo và hiển thị dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_feature);

        // Cấu hình dialog để hiển thị như popup nổi
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }

        // Button "TRY NOW" - check rank và navigate tương ứng
        MaterialButton btnTryNow = dialog.findViewById(R.id.btnTryNow);
        if (btnTryNow != null) {
            btnTryNow.setOnClickListener(v -> {
                dialog.dismiss();
                // Clear flag sau khi hiển thị popup
                prefs.edit().putBoolean("should_show_new_feature_popup", false).apply();
                
                // Check rank và navigate: FREE → PaymentPlanFragment, PREMIUM → có thể sử dụng bình thường
                checkRankAndNavigate(() -> {
                    // Nếu PREMIUM, có thể navigate đến một trong các chức năng hoặc để ở home
                    // Ở đây ta để user tự chọn từ home (không làm gì cả)
                    // Hoặc có thể navigate đến một fragment overview nếu cần
                });
            });
        }

        // Cho phép đóng dialog khi click bên ngoài
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Clear flag khi dialog bị đóng (dù bằng cách nào)
        dialog.setOnDismissListener(dialog1 -> {
            prefs.edit().putBoolean("should_show_new_feature_popup", false).apply();
        });

        // Hiển thị dialog
        dialog.show();
    }

    private void createIndicators(int count) {
        if (indicatorContainer == null) return;
        
        indicatorContainer.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            
            if (i == 0) {
                dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_active));
            } else {
                dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_inactive));
            }
            
            indicatorContainer.addView(dot);
        }
    }

    private void startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private void updateIndicators(int position) {
        if (!isAdded() || getContext() == null || indicatorContainer == null) return;
        
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View dot = indicatorContainer.getChildAt(i);
            if (i == position) {
                dot.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_indicator_active));
            } else {
                dot.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_indicator_inactive));
            }
        }
    }

    private void loadWallets(Spinner spinner, Long[] selectedWalletId) {
        walletApiService.getWallets().enqueue(new Callback<List<WalletResponse>>() {
            @Override
            public void onResponse(Call<List<WalletResponse>> call, Response<List<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WalletResponse> wallets = response.body();
                    List<String> walletNames = new ArrayList<>();
                    for (WalletResponse w : wallets) walletNames.add(w.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, walletNames);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedWalletId[0] = wallets.get(position).getId();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_loading_wallets), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories(Spinner spinner, String type, Long[] selectedCategoryId) {
        categoryApiService.getCategories(type).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> categories = response.body();
                    List<String> names = new ArrayList<>();
                    for (CategoryResponse c : categories) names.add(c.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, names);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCategoryId[0] = categories.get(position).getId();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void selectTab(String type) {
        currentType = type;

        // Đổi trạng thái chọn cho 2 box
        boxExpense.setSelected("EXPENSE".equalsIgnoreCase(type));
        boxIncome.setSelected("INCOME".equalsIgnoreCase(type));

        // Đổi màu chữ tương ứng
        // Đổi màu chữ tương ứng
        if ("EXPENSE".equalsIgnoreCase(type)) {
            ((TextView) boxExpense.findViewById(R.id.tvExpenseLabel)).setTextColor(Color.WHITE);
            ((TextView) boxExpense.findViewById(R.id.tvExpenseAmount)).setTextColor(Color.WHITE);

            ((TextView) boxIncome.findViewById(R.id.tvIncomeLabel)).setTextColor(Color.parseColor("#1A2C54")); // home_text_strong
            ((TextView) boxIncome.findViewById(R.id.tvIncomeAmount)).setTextColor(Color.parseColor("#1A2C54"));
        } else {
            ((TextView) boxIncome.findViewById(R.id.tvIncomeLabel)).setTextColor(Color.WHITE);
            ((TextView) boxIncome.findViewById(R.id.tvIncomeAmount)).setTextColor(Color.WHITE);

            ((TextView) boxExpense.findViewById(R.id.tvExpenseLabel)).setTextColor(Color.parseColor("#1A2C54")); // home_text_strong
            ((TextView) boxExpense.findViewById(R.id.tvExpenseAmount)).setTextColor(Color.parseColor("#1A2C54"));
        }

        // Gọi API để đổi biểu đồ + list
        loadStatistics(type);
    }


    private void loadStatistics(String type) {
        progressBarChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.INVISIBLE);

        apiService.getStatistics(type).enqueue(new Callback<StatisticResponse>() {
            @Override
            public void onResponse(Call<StatisticResponse> call, Response<StatisticResponse> response) {
                if (!isAdded() || getContext() == null) return;
                progressBarChart.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);
                
                // Ẩn loading dialog trong MainActivity khi đã load xong dữ liệu
                if (getActivity() instanceof com.example.inzightapp.MainActivity) {
                    ((com.example.inzightapp.MainActivity) getActivity()).hideLoadingDialog();
                }

                if (response.isSuccessful() && response.body() != null) {

                    StatisticResponse data = response.body();

                    setupPieChart(data.getCategories());

                    adapter = new CategoryStatAdapter(data.getCategories());
                    recyclerView.setAdapter(adapter);

                    Number totalExpense = data.getTotalExpense() != null ? data.getTotalExpense() : 0.0;
                    Number totalIncome = data.getTotalIncome() != null ? data.getTotalIncome() : 0.0;

                    boxExpense.setSelected("EXPENSE".equalsIgnoreCase(type));
                    if(boxExpense.isSelected()){
                        tvIncomeAmount.setText(String.format("%,.0f", totalIncome.doubleValue()));
                        tvExpenseAmount.setText("");
                    }

                    boxIncome.setSelected("INCOME".equalsIgnoreCase(type));
                    if(boxIncome.isSelected()){
                        tvExpenseAmount.setText(String.format("%,.0f", totalExpense.doubleValue()));
                        tvIncomeAmount.setText("");
                    }

                } else {
                    progressBarChart.setVisibility(View.GONE);
                    pieChart.setVisibility(View.VISIBLE);
                    pieChart.clear();
                    tvExpenseAmount.setText("0");
                    tvIncomeAmount.setText("0");
                }
            }

            @Override
            public void onFailure(Call<StatisticResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                progressBarChart.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);
                
                // Ẩn loading dialog trong MainActivity ngay cả khi có lỗi
                if (getActivity() instanceof com.example.inzightapp.MainActivity) {
                    ((com.example.inzightapp.MainActivity) getActivity()).hideLoadingDialog();
                }
                
                Toast.makeText(getContext(), getString(R.string.api_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.example.inzightapp.MainActivity) {
            ((com.example.inzightapp.MainActivity) getActivity()).setBottomNavVisibility(true);
        }
        loadStatistics(currentType);  // hàm dùng để refresh biểu đồ & danh sách
        // Tiếp tục auto-scroll khi quay lại màn hình
        startAutoScroll();
        
        // Refresh rank khi quay lại HomeFragment (để đảm bảo rank được cập nhật sau khi payment)
        refreshUserRank();
        
        // Re-check tutorial when fragment becomes visible again
        if (getView() != null && isAdded() && getContext() != null) {
            View view = getView();
            view.postDelayed(() -> {
                if (isAdded() && getContext() != null && isResumed() && isVisible()) {
                    checkAndShowTutorial(view);
                }
            }, 300);
        }
    }
    
    /**
     * Refresh rank từ API và lưu vào SharedPreferences
     */
    private void refreshUserRank() {
        if (getContext() == null || userApiService == null) return;
        
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String rank = user.getRank() != null ? user.getRank() : "FREE";
                    
                    // Lưu rank vào SharedPreferences
                    android.content.SharedPreferences prefs = getContext()
                            .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putString("rank", rank).apply();
                    
                    android.util.Log.d("HomeFragment", "Rank refreshed in onResume: " + rank);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Silent fail - không cần xử lý
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng auto-scroll để tránh leak
        stopAutoScroll();
        // Hide tutorial arrow when fragment is paused (user navigated away)
        hideTutorialArrow();
    }
    private void setupPieChart(List<CategoryStatistic> categories) {
        if (!isAdded() || getContext() == null || pieChart == null) return;
        // 1. Sắp xếp dữ liệu giảm dần để màu sắc đi từ Đậm -> Nhạt theo chiều kim đồng hồ
        Collections.sort(categories, (c1, c2) -> {
            BigDecimal a1 = c1.getAmount() != null ? c1.getAmount() : BigDecimal.ZERO;
            BigDecimal a2 = c2.getAmount() != null ? c2.getAmount() : BigDecimal.ZERO;
            return a2.compareTo(a1); // Descending
        });

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Float> amounts = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (CategoryStatistic c : categories) {
            if (c.getAmount() != null && c.getAmount().floatValue() > 0f) {
                amounts.add(c.getAmount().floatValue());
                names.add(c.getCategoryName());
            }
        }

        if (amounts.isEmpty()) {
            pieChart.clear();
            return;
        }


        // Tính tổng & phần trăm
        float total = 0;
        for (Float amount : amounts) total += amount;

        List<Integer> percentRounded = new ArrayList<>();
        int currentTotal = 0;
        for (int i = 0; i < amounts.size(); i++) {
            if (i == amounts.size() - 1) {
                percentRounded.add(100 - currentTotal);
            } else {
                float percent = (amounts.get(i) / total) * 100;
                int rounded = Math.round(percent);
                percentRounded.add(rounded);
                currentTotal += rounded;
            }
        }


        int n = amounts.size();
        for (int i = 0; i < n; i++) {
            // Label là % để hiển thị bên trong
            PieEntry e = new PieEntry(amounts.get(i), percentRounded.get(i) + "%");
            
            // Icon hiển thị bên ngoài (theo ValuePosition)
            int iconRes = CategoryUtils.getIconForCategory(names.get(i));
            Drawable icon = ContextCompat.getDrawable(getContext(), iconRes);
            if (icon != null) {
                icon.setBounds(0, 0, 40, 40); 
                e.setIcon(icon);
            }

            entries.add(e);
        }

        // Màu biểu đồ - Gradient từ Đậm -> Nhạt
        List<Integer> chartColors = new ArrayList<>();

        if ("EXPENSE".equalsIgnoreCase(currentType)) {
            // Blue Gradient for Expense
            chartColors.add(Color.parseColor("#1976D2")); // Blue 700
            chartColors.add(Color.parseColor("#2196F3")); // Blue 500
            chartColors.add(Color.parseColor("#42A5F5")); // Blue 400
            chartColors.add(Color.parseColor("#90CAF9")); // Blue 200
            chartColors.add(Color.parseColor("#E3F2FD")); // Blue 50
        } else {
            // Green Gradient for Income
            chartColors.add(Color.parseColor("#2E7D32")); // Green 800
            chartColors.add(Color.parseColor("#388E3C")); // Green 700
            chartColors.add(Color.parseColor("#4CAF50")); // Green 500
            chartColors.add(Color.parseColor("#81C784")); // Green 300
            chartColors.add(Color.parseColor("#C8E6C9")); // Green 100
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(chartColors);
        dataSet.setSliceSpace(3f);
        
        // Disable default icon drawing because we draw them in CustomPieChartRenderer
        dataSet.setDrawIcons(false); 
        dataSet.setIconsOffset(new com.github.mikephil.charting.utils.MPPointF(0, 0));

        // Cấu hình vị trí: Label (%) bên trong, Value (Icon) bên ngoài
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        
        // Cấu hình đường kẻ (chỉ thẳng ra, không gấp khúc)
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.6f);
        dataSet.setValueLinePart2Length(0f); // Xóa đoạn ngang
        dataSet.setValueLineColor(Color.parseColor("#1A2C54"));
        dataSet.setValueLineWidth(1f);
        
        // Ẩn text value bên ngoài (chỉ để lại icon)
        dataSet.setValueTextSize(0f);
        dataSet.setValueTextColor(Color.TRANSPARENT);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ""; // Không hiện số bên ngoài
            }
        });

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setTransparentCircleAlpha(90);
        pieChart.setHoleColor(Color.parseColor("#F9FBFF"));
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        
        // Quan trọng: Bắt đầu từ 12h (270 độ)
        pieChart.setRotationAngle(270f);
        pieChart.setRotationEnabled(false);
        
        // Set Custom Renderer
        pieChart.setRenderer(new com.example.inzightapp.view.CustomPieChartRenderer(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler()));
        
        // Cấu hình text % bên trong (Label)
        pieChart.setDrawEntryLabels(true); 
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        
        pieChart.setExtraOffsets(30f, 10f, 30f, 10f);
        
        pieChart.animateY(900, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void showIntroMultiGoalDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_intro_multi_goal);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        MaterialButton btnStart = dialog.findViewById(R.id.btnStartNow);

        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            openPaymentPlanFragment();
        });

        dialog.show();
    }

    /**
     * Kiểm tra rank của user và navigate tương ứng
     * - Nếu rank = PREMIUM → thực hiện action (navigate đến fragment tương ứng)
     * - Nếu rank = FREE → navigate đến PaymentPlanFragment
     */
    private void checkRankAndNavigate(Runnable premiumAction) {
        if (getContext() == null) return;
        
        // Lấy rank từ SharedPreferences trước (nhanh hơn)
        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        String rank = prefs.getString("rank", "FREE");
        
        android.util.Log.d("HomeFragment", "checkRankAndNavigate - Rank from SharedPreferences: " + rank);
        
        // Nếu đã là PREMIUM, thực hiện action ngay
        if ("PREMIUM".equalsIgnoreCase(rank)) {
            android.util.Log.d("HomeFragment", "User is PREMIUM, executing premium action");
            premiumAction.run();
            return;
        }
        
        android.util.Log.d("HomeFragment", "Rank is not PREMIUM, calling API to verify");
        
        // Nếu chưa chắc chắn, gọi API để verify
        userApiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String userRank = user.getRank() != null ? user.getRank() : "FREE";
                    
                    android.util.Log.d("HomeFragment", "API Response - Rank: " + userRank);
                    
                    // Lưu rank mới vào SharedPreferences
                    android.content.SharedPreferences prefs = getContext()
                            .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putString("rank", userRank).apply();
                    
                    if ("PREMIUM".equalsIgnoreCase(userRank)) {
                        // User là PREMIUM → thực hiện action
                        android.util.Log.d("HomeFragment", "User is PREMIUM (from API), executing premium action");
                        premiumAction.run();
                    } else {
                        // User là FREE → navigate đến PaymentPlanFragment
                        android.util.Log.d("HomeFragment", "User is FREE (from API), navigating to PaymentPlanFragment");
                        openPaymentPlanFragment();
                    }
                } else {
                    android.util.Log.d("HomeFragment", "API Response failed, using cached rank: " + rank);
                    // Nếu API fail, dùng rank từ SharedPreferences
                    if ("PREMIUM".equalsIgnoreCase(rank)) {
                        premiumAction.run();
                    } else {
                        openPaymentPlanFragment();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (getContext() == null) return;
                
                android.util.Log.e("HomeFragment", "API Call failed: " + t.getMessage());
                // Nếu API fail, dùng rank từ SharedPreferences
                if ("PREMIUM".equalsIgnoreCase(rank)) {
                    premiumAction.run();
                } else {
                    openPaymentPlanFragment();
                }
            }
        });
    }

    private void openPaymentPlanFragment() {
        Fragment fragment = new PaymentPlanFragment();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_right
                )
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    /**
     * Check and show tutorial for first-time users or users with few transactions
     */
    private void checkAndShowTutorial(View view) {
        if (!isAdded() || getContext() == null) return;
        
        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        String rank = prefs.getString("rank", "FREE");
        
        // Check transaction count first
        checkTransactionCountAndShowTutorial(view, rank);
    }
    
    /**
     * Check transaction count and show tutorial if user has <= 2 transactions
     */
    private void checkTransactionCountAndShowTutorial(View view, String rank) {
        if (!isAdded() || getContext() == null) return;
        
        // Initialize transactionApiService if not already initialized
        if (transactionApiService == null) {
            transactionApiService = ApiClient.getClient(getContext()).create(TransactionApiService.class);
        }
        
        // Get transaction count
        transactionApiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, 
                                 Response<List<TransactionResponse>> response) {
                if (!isAdded() || getContext() == null) return;
                
                int transactionCount = 0;
                if (response.isSuccessful() && response.body() != null) {
                    transactionCount = response.body().size();
                }
                
                // Logic ưu tiên: Nếu PREMIUM -> ưu tiên show premium tutorial (multi-goal arrow)
                // Nếu không PREMIUM và <= 2 transactions -> show add transaction arrow
                if ("PREMIUM".equalsIgnoreCase(rank) && TutorialHelper.shouldShowPremiumTutorial(getContext())) {
                    // Ưu tiên: Nếu user đã thanh toán, show multi-goal arrow
                    startPremiumTutorial(view);
                } else if (TutorialHelper.shouldShowBasicTutorial(getContext(), transactionCount)) {
                    // Nếu chưa PREMIUM và có <= 2 transactions, show add transaction arrow
                    startBasicTutorial(view, transactionCount);
                }
            }
            
            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                // On failure, still check tutorial state (fallback)
                // Logic ưu tiên: PREMIUM -> multi-goal arrow
                if ("PREMIUM".equalsIgnoreCase(rank) && TutorialHelper.shouldShowPremiumTutorial(getContext())) {
                    startPremiumTutorial(view);
                } else if (TutorialHelper.shouldShowBasicTutorial(getContext())) {
                    startBasicTutorial(view, 0); // Unknown count, use 0
                }
            }
        });
    }
    
    /**
     * Start basic tutorial: Add Transaction -> Home Chart -> Transaction History
     * @param transactionCount Number of transactions user has (to determine if tutorial should persist)
     */
    private void startBasicTutorial(View view, int transactionCount) {
        if (!isAdded() || getContext() == null) return;
        
        final boolean shouldPersist = transactionCount <= 2; // Don't mark as completed if user has <= 2 transactions
        
        // Show arrow pointing to Add Transaction button
        showTutorialArrow(view, btnAddTransaction, () -> {
            // When user clicks Add Transaction, hide arrow and mark step as shown
            hideTutorialArrow();
            TutorialHelper.markTutorialStepShown(getContext(), TutorialHelper.STEP_ADD_TRANSACTION);
            
            // Only mark as completed if user has more than 2 transactions
            if (!shouldPersist) {
                TutorialHelper.markBasicTutorialCompleted(getContext());
            }
            
            // Navigate to AddTransactionFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
    
    /**
     * Show tutorial arrow pointing to a target view
     */
    private void showTutorialArrow(View parentView, View targetView, Runnable onTargetClicked) {
        if (!isAdded() || getContext() == null || targetView == null || !isResumed()) {
            return; // Only show if fragment is visible and resumed
        }
        
        // Remove existing arrow if any
        hideTutorialArrow();
        
        // Double check that we're still visible
        if (!isResumed() || !isVisible()) {
            return;
        }
        
        // Create arrow view
        tutorialArrowView = new TutorialArrowView(getContext());
        tutorialArrowView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // Add arrow view to MainActivity's container (not root view to avoid showing on other fragments)
        ViewGroup container = requireActivity().findViewById(R.id.container);
        if (container != null && isResumed() && isVisible()) {
            container.addView(tutorialArrowView);
            
            // Point arrow to target view
            parentView.postDelayed(() -> {
                if (tutorialArrowView != null && targetView != null && isResumed() && isVisible()) {
                    tutorialArrowView.pointTo(targetView);
                } else {
                    // Fragment is no longer visible, hide arrow
                    hideTutorialArrow();
                }
            }, 300);
            
            // Lưu original listener nếu chưa có
            View.OnClickListener originalListener = (View.OnClickListener) targetView.getTag(R.id.original_click_listener);
            if (originalListener == null) {
                // Nếu chưa có original listener, lấy listener hiện tại (nếu có)
                // Với btnAddTransaction, original listener đã được lưu trong onCreateView
                originalListener = (View.OnClickListener) targetView.getTag(R.id.original_click_listener);
            }
            
            // Tạo wrapper listener - gọi cả original listener và custom handler
            final View.OnClickListener finalOriginalListener = originalListener;
            View.OnClickListener wrapper = v -> {
                // Hide arrow when clicked
                hideTutorialArrow();
                
                // Call custom handler (onTargetClicked) - đã bao gồm navigation logic
                if (onTargetClicked != null) {
                    onTargetClicked.run();
                } else if (finalOriginalListener != null) {
                    // Nếu không có custom handler, gọi original listener
                    finalOriginalListener.onClick(v);
                }
            };
            
            targetView.setOnClickListener(wrapper);
        }
    }
    
    /**
     * Hide tutorial arrow
     */
    private void hideTutorialArrow() {
        if (tutorialArrowView != null) {
            tutorialArrowView.stopAnimation();
            ViewGroup parent = (ViewGroup) tutorialArrowView.getParent();
            if (parent != null) {
                parent.removeView(tutorialArrowView);
            }
            tutorialArrowView = null;
        }
        
        // Restore original click listener cho btnAddTransaction nếu có
        if (btnAddTransaction != null) {
            View.OnClickListener originalListener = (View.OnClickListener) btnAddTransaction.getTag(R.id.original_click_listener);
            if (originalListener != null) {
                btnAddTransaction.setOnClickListener(originalListener);
            }
        }
    }
    
    /**
     * Start premium tutorial: Chỉ hiển thị mũi tên trỏ vào Multi-goal (không dùng popup overlay)
     */
    private void startPremiumTutorial(View view) {
        if (!isAdded() || getContext() == null) return;
        
        View targetView = view.findViewById(R.id.btnNavMultiGoal);
        if (targetView == null) {
            android.util.Log.w("HomeFragment", "btnNavMultiGoal not found, cannot show premium tutorial");
            return;
        }
        
        // Show arrow pointing to Multi-goal button
        // Save original click listener first
        View.OnClickListener originalListener = btnNavMultiGoal.getTag(R.id.original_click_listener) != null ?
            (View.OnClickListener) btnNavMultiGoal.getTag(R.id.original_click_listener) : null;
        if (originalListener == null) {
            // Save current listener as original (trước khi wrap)
            View.OnClickListener currentListener = v -> {
                checkRankAndNavigate(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new MultiGoalFragment())
                            .addToBackStack(null)
                            .commit();
                });
            };
            btnNavMultiGoal.setTag(R.id.original_click_listener, currentListener);
        }
        
        showTutorialArrow(view, targetView, () -> {
            // When user clicks Multi-goal, hide arrow and mark tutorial as shown
            TutorialHelper.markTutorialStepShown(getContext(), TutorialHelper.STEP_MULTI_GOAL);
            // Mark premium tutorial as completed khi user click vào multi-goal
            TutorialHelper.markPremiumTutorialCompleted(getContext());
            
            // Navigate to MultiGoalFragment (original behavior)
            checkRankAndNavigate(() -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new MultiGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        });
    }

}
