package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.inzightapp.model.request.ScenarioInputRequest;
import com.example.inzightapp.model.response.ScenarioResultResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScenarioInputFragment extends Fragment {

    private ImageView btnBack;
    private EditText etMonthlyIncome, etMonthlyExpense;
    private Slider sliderDuration;
    private TextView tvDurationValue;
    private MaterialButton btnGenerate;
    
    private FinancePlannerApiService financePlannerApiService;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scenario_input, container, false);

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
        etMonthlyExpense = view.findViewById(R.id.etMonthlyExpense);
        
        sliderDuration = view.findViewById(R.id.sliderDuration);
        tvDurationValue = view.findViewById(R.id.tvDurationValue);
        
        btnGenerate = view.findViewById(R.id.btnGenerate);
        
        financePlannerApiService = ApiClient.getClient(getContext()).create(FinancePlannerApiService.class);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup currency formatting
        setupCurrencyFormatting();
    }
    
    private void setupCurrencyFormatting() {
        if (etMonthlyIncome != null) {
            etMonthlyIncome.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlyIncome));
        }
        if (etMonthlyExpense != null) {
            etMonthlyExpense.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlyExpense));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        sliderDuration.addOnChangeListener((slider, value, fromUser) -> {
            tvDurationValue.setText(String.valueOf((int) value) + " năm");
        });

        btnGenerate.setOnClickListener(v -> {
            calculateScenario();
        });
    }
    
    private void calculateScenario() {
        try {
            // Get inputs
            String incomeStr = etMonthlyIncome.getText().toString().trim();
            String expenseStr = etMonthlyExpense.getText().toString().trim();
            
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
            
            // Use default preset (inflation_low)
            String preset = "inflation_low";
            
            // Get projection years from slider
            int projectionYears = (int) sliderDuration.getValue();
            if (projectionYears <= 0 || projectionYears > 30) {
                projectionYears = 10; // Default
            }
            
            ScenarioInputRequest request = new ScenarioInputRequest(income, expense, preset);
            request.projectionYears = projectionYears;
            
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            String finalPreset = preset;
            int finalProjectionYears = projectionYears;
            financePlannerApiService.calculateScenario(request).enqueue(new Callback<ScenarioResultResponse>() {
                @Override
                public void onResponse(Call<ScenarioResultResponse> call, Response<ScenarioResultResponse> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ScenarioResultResponse result = response.body();
                        
                        Bundle bundle = new Bundle();
                        bundle.putDouble("adjustedIncome", result.adjustedIncome);
                        bundle.putDouble("adjustedExpense", result.adjustedExpense);
                        bundle.putDouble("originalIncome", income);
                        bundle.putDouble("originalExpense", expense);
                        bundle.putString("preset", finalPreset);
                        bundle.putString("presetName", result.presetName != null ? result.presetName : "");
                        bundle.putDouble("netChange", result.netChange);
                        if (result.yearlyProjections != null && !result.yearlyProjections.isEmpty()) {
                            bundle.putInt("durationYears", finalProjectionYears);
                        }
                        
                        ScenarioResultFragment resultFragment = new ScenarioResultFragment();
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
                public void onFailure(Call<ScenarioResultResponse> call, Throwable t) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
