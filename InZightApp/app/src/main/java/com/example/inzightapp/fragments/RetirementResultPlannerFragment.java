package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class RetirementResultPlannerFragment extends Fragment {

    private ImageView btnBack;
    private MaterialButton btnSavePlan;
    private TextView tvTotalFunds, tvLifestylePercent, tvMonthlySavings, tvYearsToRetire;
    private TextView tvTotalFundsNeeded, tvLifestyleVsCoreNeeds, tvAnnualBurnRate, tvLifestyleStressLevel;
    private TextView tvCurrentAge, tvRetireAge;
    private NumberFormat numberFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retirement_result_planner, container, false);

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        initViews(view);
        setupListeners();
        displayData();

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
        btnSavePlan = view.findViewById(R.id.btnSavePlan);
        tvTotalFunds = view.findViewById(R.id.tvTotalFunds);
        tvLifestylePercent = view.findViewById(R.id.tvLifestylePercent);
        tvMonthlySavings = view.findViewById(R.id.tvMonthlySavings);
        tvYearsToRetire = view.findViewById(R.id.tvYearsToRetire);
        
        // Additional TextViews if they exist in layout
        tvTotalFundsNeeded = view.findViewById(R.id.tvTotalFundsNeeded);
        tvLifestyleVsCoreNeeds = view.findViewById(R.id.tvLifestyleVsCoreNeeds);
        tvAnnualBurnRate = view.findViewById(R.id.tvAnnualBurnRate);
        tvLifestyleStressLevel = view.findViewById(R.id.tvLifestyleStressLevel);
        tvCurrentAge = view.findViewById(R.id.tvCurrentAge);
        tvRetireAge = view.findViewById(R.id.tvRetireAge);
    }
    
    private void displayData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double futureValue = args.getDouble("futureValue", 0);
        int sustainableYears = args.getInt("sustainableYears", 0);
        int retireAge = args.getInt("retireAge", 65);
        int ageNow = args.getInt("ageNow", 30);
        double monthlyExpense = args.getDouble("monthlyExpense", 0);
        double totalNeeded = args.getDouble("totalNeeded", futureValue);
        double monthlySavingsNeeded = args.getDouble("monthlySavingsNeeded", 0);
        double currentSavings = args.getDouble("currentSavings", 0);
        
        // Calculate years to retirement
        int yearsToRetire = retireAge - ageNow;
        if (yearsToRetire < 0) yearsToRetire = 0;
        
        // Calculate required monthly savings (use from API if available, otherwise calculate)
        double monthlySavings = monthlySavingsNeeded > 0 ? monthlySavingsNeeded : 
                               (yearsToRetire > 0 ? totalNeeded / (yearsToRetire * 12) : 0);
        
        // Calculate lifestyle percentage (expense vs income estimate)
        double estimatedIncome = monthlyExpense / 0.7; // Assuming 70% expense ratio
        double lifestylePercent = estimatedIncome > 0 ? (monthlyExpense / estimatedIncome) * 100 : 0;
        
        // Calculate annual burn rate
        double annualBurnRate = monthlyExpense * 12;
        
        // Calculate lifestyle vs core needs percentage
        double coreNeeds = monthlyExpense * 0.6; // Assume 60% is core needs
        double lifestyleVsCoreNeeds = monthlyExpense > 0 ? ((monthlyExpense - coreNeeds) / monthlyExpense) * 100 : 0;
        
        // Determine stress level
        String stressLevel = "THẤP";
        if (lifestylePercent > 90) {
            stressLevel = "RẤT CAO";
        } else if (lifestylePercent > 75) {
            stressLevel = "CAO";
        } else if (lifestylePercent > 60) {
            stressLevel = "TRUNG BÌNH";
        }
        
        // Display data
        if (tvTotalFunds != null) {
            tvTotalFunds.setText(formatAmount(futureValue));
        }
        if (tvTotalFundsNeeded != null) {
            tvTotalFundsNeeded.setText(formatAmount(totalNeeded));
        }
        if (tvLifestylePercent != null) {
            tvLifestylePercent.setText(String.format("%.1f%%", lifestylePercent));
        }
        if (tvLifestyleVsCoreNeeds != null) {
            tvLifestyleVsCoreNeeds.setText(String.format("%.1f%%", lifestyleVsCoreNeeds));
        }
        if (tvAnnualBurnRate != null) {
            tvAnnualBurnRate.setText(formatAmount(annualBurnRate) + "/năm");
        }
        if (tvLifestyleStressLevel != null) {
            tvLifestyleStressLevel.setText(stressLevel);
        }
        if (tvMonthlySavings != null) {
            tvMonthlySavings.setText(formatAmount(monthlySavings));
        }
        if (tvYearsToRetire != null) {
            tvYearsToRetire.setText(yearsToRetire + " năm");
        }
        if (tvCurrentAge != null) {
            tvCurrentAge.setText(String.valueOf(ageNow));
        }
        if (tvRetireAge != null) {
            tvRetireAge.setText(String.valueOf(retireAge));
        }
    }
    
    private String formatAmount(double amount) {
        // Use Vietnamese currency formatter with dot separator
        return VietnameseCurrencyFormatter.format(amount);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        btnSavePlan.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Plan Saved!", Toast.LENGTH_SHORT).show();
        });
    }
}
