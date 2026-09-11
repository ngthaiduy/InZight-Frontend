package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.FinancePlannerApiService;
import com.example.inzightapp.model.request.RetirementInputRequest;
import com.example.inzightapp.model.response.RetirementResultResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetirementInputFragment extends Fragment {

    private ImageView btnBack;
    
    // Section 1 Inputs
    private EditText etCurrentAge, etCurrentIncome, etCurrentSavings, etMonthlyExpense, etMonthlySavings, etAnnualReturn, etInflationRate;
    private Slider sliderRetireAge;
    private TextView tvRetireAge;
    private MaterialButton btnCalculatePension;

    // Section 2 Inputs
    private EditText etAnnualCost;
    private TextView tvYears, btnDecreaseYears, btnIncreaseYears;
    private Slider sliderLifestyle;
    private MaterialButton btnCalculatePlanner;
    
    // Preview Card
    private MaterialCardView cardPreview;
    private TextView tvPreviewFutureValue, tvPreviewMonthlySavings;
    
    // Preset Buttons
    
    // API Service
    private FinancePlannerApiService financePlannerApiService;
    private ProgressBar progressBar;
    
    // Real-time calculation handler
    private Handler previewHandler = new Handler();
    private Runnable previewRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retirement_input, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        // Section 1
        etCurrentAge = view.findViewById(R.id.etCurrentAge);
        etCurrentIncome = view.findViewById(R.id.etCurrentIncome);
        etCurrentSavings = view.findViewById(R.id.etCurrentSavings);
        etMonthlyExpense = view.findViewById(R.id.etMonthlyExpense);
        etMonthlySavings = view.findViewById(R.id.etMonthlySavings);
        etAnnualReturn = view.findViewById(R.id.etAnnualReturn);
        etInflationRate = view.findViewById(R.id.etInflationRate);
        sliderRetireAge = view.findViewById(R.id.sliderRetireAge);
        tvRetireAge = view.findViewById(R.id.tvRetireAge);
        btnCalculatePension = view.findViewById(R.id.btnCalculatePension);

        // Section 2
        etAnnualCost = view.findViewById(R.id.etAnnualCost);
        tvYears = view.findViewById(R.id.tvYears);
        btnDecreaseYears = view.findViewById(R.id.btnDecreaseYears);
        btnIncreaseYears = view.findViewById(R.id.btnIncreaseYears);
        sliderLifestyle = view.findViewById(R.id.sliderLifestyle);
        btnCalculatePlanner = view.findViewById(R.id.btnCalculatePlanner);
        
        // Preview Card
        cardPreview = view.findViewById(R.id.cardPreview);
        tvPreviewFutureValue = view.findViewById(R.id.tvPreviewFutureValue);
        tvPreviewMonthlySavings = view.findViewById(R.id.tvPreviewMonthlySavings);
        
        // API Service
        financePlannerApiService = ApiClient.getClient(getContext()).create(FinancePlannerApiService.class);
        
        // Progress bar (if exists in layout)
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup currency formatting for amount inputs
        setupCurrencyFormatting();
    }
    
    private void setupCurrencyFormatting() {
        etCurrentIncome.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etCurrentIncome));
        etCurrentSavings.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etCurrentSavings));
        etMonthlyExpense.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlyExpense));
        etMonthlySavings.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlySavings));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Slider Listeners
        sliderRetireAge.addOnChangeListener((slider, value, fromUser) -> {
            tvRetireAge.setText(String.valueOf((int) value));
            if (fromUser) {
                triggerPreviewCalculation();
            }
        });
        
        // Real-time preview listeners
        setupRealTimePreview();
        
        // Years Counter Listeners
        btnDecreaseYears.setOnClickListener(v -> {
            int current = Integer.parseInt(tvYears.getText().toString());
            if (current > 1) {
                tvYears.setText(String.valueOf(current - 1));
            }
        });

        btnIncreaseYears.setOnClickListener(v -> {
            int current = Integer.parseInt(tvYears.getText().toString());
            tvYears.setText(String.valueOf(current + 1));
        });

        // Calculate Buttons
        btnCalculatePension.setOnClickListener(v -> {
            calculateRetirement(true); // true for pension calculation
        });

        btnCalculatePlanner.setOnClickListener(v -> {
            calculateRetirement(false); // false for planner calculation
        });
    }
    
    private void calculateRetirement(boolean isPension) {
        // Validate inputs
        String currentAgeStr = etCurrentAge.getText().toString().trim();
        String currentIncomeStr = etCurrentIncome.getText().toString().trim();
        String currentSavingsStr = etCurrentSavings.getText().toString().trim();
        String monthlyExpenseStr = etMonthlyExpense.getText().toString().trim();
        String monthlySavingsStr = etMonthlySavings.getText().toString().trim();
        String annualReturnStr = etAnnualReturn.getText().toString().trim();
        String inflationRateStr = etInflationRate.getText().toString().trim();
        String retireAgeStr = tvRetireAge.getText().toString().trim();
        
        if (currentAgeStr.isEmpty() || currentIncomeStr.isEmpty() || 
            currentSavingsStr.isEmpty() || monthlyExpenseStr.isEmpty() ||
            annualReturnStr.isEmpty() || inflationRateStr.isEmpty() || retireAgeStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int ageNow = Integer.parseInt(currentAgeStr);
            int retireAge = Integer.parseInt(retireAgeStr);
            
            // Parse amounts (remove formatting)
            double currentIncome = VietnameseCurrencyFormatter.parse(currentIncomeStr);
            double currentSavings = VietnameseCurrencyFormatter.parse(currentSavingsStr);
            double monthlyExpense = VietnameseCurrencyFormatter.parse(monthlyExpenseStr);
            double annualReturn = Double.parseDouble(annualReturnStr) / 100.0; // Convert percentage to decimal
            
            // Monthly savings is optional
            double monthlySavings = monthlySavingsStr.isEmpty() ? 0 : VietnameseCurrencyFormatter.parse(monthlySavingsStr);
            
            // Determine investment type based on return rate
            String investmentType = "tietkiem"; // Default
            if (annualReturn >= 0.13) {
                investmentType = "chungkhoan";
            } else if (annualReturn >= 0.085) {
                investmentType = "etf";
            } else if (annualReturn >= 0.08) {
                investmentType = "traiphieu";
            }
            
            double inflationRate = Double.parseDouble(inflationRateStr) / 100.0;
            
            RetirementInputRequest request = new RetirementInputRequest(
                currentSavings,
                monthlyExpense,
                monthlySavings,
                inflationRate,
                investmentType,
                ageNow,
                retireAge
            );
            
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            
            financePlannerApiService.calculateRetirement(request).enqueue(new Callback<RetirementResultResponse>() {
                @Override
                public void onResponse(Call<RetirementResultResponse> call, Response<RetirementResultResponse> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        RetirementResultResponse result = response.body();
                        
                        // Pass data to result fragment
                        Bundle bundle = new Bundle();
                        bundle.putDouble("futureValue", result.futureValue);
                        bundle.putInt("sustainableYears", result.sustainableYears);
                        bundle.putInt("ageNow", ageNow);
                        bundle.putInt("retireAge", retireAge);
                        bundle.putDouble("monthlyExpense", monthlyExpense);
                        bundle.putDouble("currentSavings", currentSavings);
                        if (result.monthlySavingsNeeded > 0) {
                            bundle.putDouble("monthlySavingsNeeded", result.monthlySavingsNeeded);
                        }
                        if (result.retirementGap > 0) {
                            bundle.putDouble("retirementGap", result.retirementGap);
                        }
                        if (result.totalNeeded > 0) {
                            bundle.putDouble("totalNeeded", result.totalNeeded);
                        }
                        
                        Fragment resultFragment;
                        if (isPension) {
                            resultFragment = new RetirementResultPensionFragment();
                        } else {
                            resultFragment = new RetirementResultPlannerFragment();
                        }
                        resultFragment.setArguments(bundle);
                        
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, resultFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi tính toán: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<RetirementResultResponse> call, Throwable t) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupRealTimePreview() {
        TextWatcher previewWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                triggerPreviewCalculation();
            }
        };
        
        etCurrentAge.addTextChangedListener(previewWatcher);
        etCurrentIncome.addTextChangedListener(previewWatcher);
        etCurrentSavings.addTextChangedListener(previewWatcher);
        etMonthlyExpense.addTextChangedListener(previewWatcher);
        etMonthlySavings.addTextChangedListener(previewWatcher);
        etAnnualReturn.addTextChangedListener(previewWatcher);
        etInflationRate.addTextChangedListener(previewWatcher);
    }
    
    private void triggerPreviewCalculation() {
        // Cancel previous calculation
        if (previewRunnable != null) {
            previewHandler.removeCallbacks(previewRunnable);
        }
        
        // Delay calculation to avoid too frequent calls
        previewRunnable = () -> {
            calculatePreview();
        };
        previewHandler.postDelayed(previewRunnable, 500);
    }
    
    private void calculatePreview() {
        try {
            String ageStr = etCurrentAge.getText().toString().trim();
            String incomeStr = etCurrentIncome.getText().toString().trim();
            String savingsStr = etCurrentSavings.getText().toString().trim();
            String expenseStr = etMonthlyExpense.getText().toString().trim();
            String monthlySavingsStr = etMonthlySavings.getText().toString().trim();
            String returnStr = etAnnualReturn.getText().toString().trim();
            String inflationStr = etInflationRate.getText().toString().trim();
            String retireAgeStr = tvRetireAge.getText().toString().trim();
            
            if (ageStr.isEmpty() || incomeStr.isEmpty() || savingsStr.isEmpty() || 
                expenseStr.isEmpty() || returnStr.isEmpty() || 
                inflationStr.isEmpty() || retireAgeStr.isEmpty()) {
                cardPreview.setVisibility(View.GONE);
                return;
            }
            
            int ageNow = Integer.parseInt(ageStr);
            int retireAge = Integer.parseInt(retireAgeStr);
            double currentIncome = Double.parseDouble(incomeStr);
            double currentSavings = Double.parseDouble(savingsStr);
            double monthlyExpense = Double.parseDouble(expenseStr);
            double monthlySavings = monthlySavingsStr.isEmpty() ? 0 : Double.parseDouble(monthlySavingsStr);
            double annualReturn = Double.parseDouble(returnStr) / 100.0;
            double inflationRate = Double.parseDouble(inflationStr) / 100.0;
            
            if (retireAge <= ageNow) {
                cardPreview.setVisibility(View.GONE);
                return;
            }
            
            // Preview calculation matching backend logic
            int years = retireAge - ageNow;
            double monthlyReturn = annualReturn / 12;
            double months = years * 12;
            
            // Future value from current savings
            double futureValueFromSavings = currentSavings * Math.pow(1 + annualReturn, years);
            
            // Future value from monthly contributions (simple, no compound interest)
            double futureValueFromContributions = 0;
            if (monthlySavings > 0) {
                // Simple calculation: monthly amount × 12 months × years
                futureValueFromContributions = monthlySavings * 12 * years;
            }
            
            double totalFutureValue = futureValueFromSavings + futureValueFromContributions;
            
            // Calculate expense at retirement (adjusted for inflation)
            double yearlyExpenseAtRetirement = monthlyExpense * 12 * Math.pow(1 + inflationRate, years);
            
            // Calculate total needed for 20 years retirement
            double totalNeeded = 0;
            for (int i = 0; i < 20; i++) {
                double expenseInYear = yearlyExpenseAtRetirement * Math.pow(1 + inflationRate, i);
                totalNeeded += expenseInYear / Math.pow(1 + annualReturn, i);
            }
            
            // Calculate gap and monthly savings needed
            double gap = totalNeeded - totalFutureValue;
            double monthlySavingsNeeded = 0;
            if (gap > 0 && years > 0) {
                double monthlyRate = annualReturn / 12;
                double totalMonths = years * 12;
                monthlySavingsNeeded = gap * monthlyRate / 
                    (Math.pow(1 + monthlyRate, totalMonths) - 1);
            }
            
            // Update preview
            tvPreviewFutureValue.setText(VietnameseCurrencyFormatter.format(totalFutureValue));
            tvPreviewMonthlySavings.setText(VietnameseCurrencyFormatter.format(monthlySavingsNeeded));
            cardPreview.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            cardPreview.setVisibility(View.GONE);
        }
    }
    
}
