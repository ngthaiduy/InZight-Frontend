package com.example.inzightapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.FinancePlannerApiService;
import com.example.inzightapp.model.request.WhatIfInputRequest;
import com.example.inzightapp.model.response.WhatIfResultResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OptimizerInputFragment extends Fragment {

    private ImageView btnBack;
    private EditText etMonthlyIncome, etMonthlyExpenses, etExpectedYears, etReturnRate;
    private Slider sliderInflation;
    private TextView tvInflationValue;
    private MaterialButton btnCalculate;
    
    private FinancePlannerApiService financePlannerApiService;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_optimizer_input, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.example.inzightapp.MainActivity) {
            ((com.example.inzightapp.MainActivity) getActivity()).setBottomNavVisibility(false);
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        etMonthlyIncome = view.findViewById(R.id.etMonthlyIncome);
        etMonthlyExpenses = view.findViewById(R.id.etMonthlyExpenses);
        etExpectedYears = view.findViewById(R.id.etExpectedYears);
        etReturnRate = view.findViewById(R.id.etReturnRate);
        sliderInflation = view.findViewById(R.id.sliderInflation);
        tvInflationValue = view.findViewById(R.id.tvInflationValue);
        
        btnCalculate = view.findViewById(R.id.btnCalculate);
        
        financePlannerApiService = ApiClient.getClient(getContext()).create(FinancePlannerApiService.class);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup currency formatting
        setupCurrencyFormatting();
    }
    
    private void setupCurrencyFormatting() {
        if (etMonthlyIncome != null) {
            etMonthlyIncome.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlyIncome));
        }
        if (etMonthlyExpenses != null) {
            etMonthlyExpenses.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlyExpenses));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        sliderInflation.addOnChangeListener((slider, value, fromUser) -> {
            tvInflationValue.setText((int) value + "%");
        });

        btnCalculate.setOnClickListener(v -> {
            calculateOptimizer();
        });
    }
    
    private void calculateOptimizer() {
        try {
            // Get inputs
            String incomeStr = etMonthlyIncome.getText().toString().trim();
            String expenseStr = etMonthlyExpenses.getText().toString().trim();
            String yearsStr = etExpectedYears.getText().toString().trim();
            
            if (incomeStr.isEmpty() || expenseStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter monthly income and expenses", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Parse amounts (remove formatting)
            double income = VietnameseCurrencyFormatter.parse(incomeStr);
            double expense = VietnameseCurrencyFormatter.parse(expenseStr);
            
            if (income <= 0 || expense <= 0) {
                Toast.makeText(getContext(), "Income and expenses must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get inflation value
            double inflationValue = sliderInflation.getValue();
            double inflationRate = inflationValue / 100.0;
            
            // Get return rate from user input
            String returnRateStr = etReturnRate.getText().toString().trim();
            double returnRatePercent = 7.0; // Default 7%
            if (!returnRateStr.isEmpty()) {
                try {
                    returnRatePercent = Double.parseDouble(returnRateStr);
                    if (returnRatePercent < 0 || returnRatePercent > 50) {
                        Toast.makeText(getContext(), "Return rate should be between 0% and 50%", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid return rate format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            double returnRate = returnRatePercent / 100.0;
            
            // Calculate income change based on return rate (higher return = higher income growth potential)
            // Formula: income change = return rate * 0.5 (conservative estimate)
            double incomeChange = returnRate * 0.5;
            if (incomeChange < 0.01) incomeChange = 0.01; // Minimum 1%
            if (incomeChange > 0.15) incomeChange = 0.15; // Maximum 15%
            
            // Get projection years
            int projectionYears = 15; // Default
            if (!yearsStr.isEmpty()) {
                try {
                    projectionYears = Integer.parseInt(yearsStr);
                    if (projectionYears <= 0 || projectionYears > 30) {
                        projectionYears = 15; // Reset to default if invalid
                    }
                } catch (NumberFormatException e) {
                    projectionYears = 15;
                }
            }
            
            // Map return rate to strategy string for backend compatibility
            String returnRateStrategy = "MODERATE"; // Default
            if (returnRatePercent >= 9) {
                returnRateStrategy = "HIGH";
            } else if (returnRatePercent >= 5) {
                returnRateStrategy = "MODERATE";
            } else {
                returnRateStrategy = "SAFE";
            }
            
            WhatIfInputRequest request = new WhatIfInputRequest(
                income, 
                expense, 
                incomeChange,
                inflationRate,
                returnRateStrategy,
                projectionYears
            );
            
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            double finalIncomeChange = incomeChange;
            String finalReturnRateStrategy = returnRateStrategy;
            double finalReturnRatePercent = returnRatePercent;
            int finalProjectionYears = projectionYears;
            financePlannerApiService.calculateWhatIf(request).enqueue(new Callback<WhatIfResultResponse>() {
                @Override
                public void onResponse(Call<WhatIfResultResponse> call, Response<WhatIfResultResponse> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        WhatIfResultResponse result = response.body();
                        
                        Bundle bundle = new Bundle();
                        bundle.putDouble("newIncome", result.newIncome);
                        bundle.putDouble("adjustedExpense", result.adjustedExpense);
                        bundle.putDouble("originalIncome", income);
                        bundle.putDouble("originalExpense", expense);
                        bundle.putDouble("incomeChange", finalIncomeChange);
                        bundle.putString("returnRate", finalReturnRateStrategy);
                        bundle.putDouble("returnRateValue", finalReturnRatePercent); // Store actual return rate value
                        bundle.putDouble("inflation", inflationValue);
                        bundle.putInt("projectionYears", finalProjectionYears);
                        if (result.netSavings > 0) {
                            bundle.putDouble("netSavings", result.netSavings);
                        }
                        if (result.improvement != 0) {
                            bundle.putDouble("improvement", result.improvement);
                        }
                        
                        OptimizerResultFragment resultFragment = new OptimizerResultFragment();
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
                public void onFailure(Call<WhatIfResultResponse> call, Throwable t) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
