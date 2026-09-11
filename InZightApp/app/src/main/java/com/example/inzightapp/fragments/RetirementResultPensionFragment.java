package com.example.inzightapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RetirementResultPensionFragment extends Fragment {

    private ImageView btnBack;
    private BarChart chartPension;
    private TextView tvFutureValue, tvSustainableYears, tvRetireAge, tvCurrentAge;
    private TextView tvMonthlyExpense, tvMonthlySavingsNeeded, tvPercentToSave, tvRetirementGap;
    private NumberFormat numberFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retirement_result_pension, container, false);

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        initViews(view);
        setupListeners();
        displayData();
        setupChart();

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
        chartPension = view.findViewById(R.id.chartPension);
        tvFutureValue = view.findViewById(R.id.tvFutureValue);
        tvSustainableYears = view.findViewById(R.id.tvSustainableYears);
        tvRetireAge = view.findViewById(R.id.tvRetireAge);
        tvCurrentAge = view.findViewById(R.id.tvCurrentAge);
        tvMonthlyExpense = view.findViewById(R.id.tvMonthlyExpense);
        tvMonthlySavingsNeeded = view.findViewById(R.id.tvMonthlySavingsNeeded);
        tvPercentToSave = view.findViewById(R.id.tvPercentToSave);
        tvRetirementGap = view.findViewById(R.id.tvRetirementGap);
    }
    
    private void displayData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double futureValue = args.getDouble("futureValue", 0);
        int sustainableYears = args.getInt("sustainableYears", 0);
        int retireAge = args.getInt("retireAge", 65);
        int ageNow = args.getInt("ageNow", 30);
        double monthlyExpense = args.getDouble("monthlyExpense", 0);
        double monthlySavingsNeeded = args.getDouble("monthlySavingsNeeded", 0);
        double retirementGap = args.getDouble("retirementGap", 0);
        double currentSavings = args.getDouble("currentSavings", 0);
        
        // Calculate percent of income to save (estimate income from expense)
        double estimatedIncome = monthlyExpense / 0.7; // Assuming 70% expense ratio
        double percentToSave = estimatedIncome > 0 ? (monthlySavingsNeeded / estimatedIncome) * 100 : 0;
        
        if (tvFutureValue != null) {
            tvFutureValue.setText(formatAmount(futureValue));
        }
        if (tvSustainableYears != null) {
            tvSustainableYears.setText(sustainableYears + " năm");
        }
        if (tvRetireAge != null) {
            tvRetireAge.setText(String.valueOf(retireAge));
        }
        if (tvCurrentAge != null) {
            tvCurrentAge.setText(String.valueOf(ageNow));
        }
        if (tvMonthlyExpense != null) {
            tvMonthlyExpense.setText(formatAmount(monthlyExpense));
        }
        if (tvMonthlySavingsNeeded != null) {
            tvMonthlySavingsNeeded.setText(formatAmount(monthlySavingsNeeded));
        }
        if (tvPercentToSave != null) {
            tvPercentToSave.setText(String.format("%.2f%%", percentToSave));
        }
        if (tvRetirementGap != null) {
            tvRetirementGap.setText(formatAmount(retirementGap));
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
    }

    private void setupChart() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double futureValue = args.getDouble("futureValue", 0);
        int sustainableYears = args.getInt("sustainableYears", 0);
        int retireAge = args.getInt("retireAge", 65);
        double monthlyExpense = args.getDouble("monthlyExpense", 0);
        
        // Calculate data for chart - show 5 years after retirement
        ArrayList<BarEntry> entriesGroup1 = new ArrayList<>(); // Pension/Savings
        ArrayList<BarEntry> entriesGroup2 = new ArrayList<>(); // Expenses
        
        // Calculate annual expense
        double annualExpense = monthlyExpense * 12;
        
        // Generate data for 5 years after retirement
        double remainingSavings = futureValue;
        String[] ageLabels = new String[5];
        
        for (int i = 0; i < 5; i++) {
            int age = retireAge + i;
            ageLabels[i] = String.valueOf(age);
            
            // Remaining savings (decreasing each year)
            double savingsAtAge = remainingSavings - (annualExpense * i);
            if (savingsAtAge < 0) savingsAtAge = 0;
            
            // Convert to millions for display
            entriesGroup1.add(new BarEntry(i + 1, (float)(savingsAtAge / 1_000_000)));
            entriesGroup2.add(new BarEntry(i + 1, (float)(annualExpense / 1_000_000)));
        }

        BarDataSet set1 = new BarDataSet(entriesGroup1, "Remaining Savings");
        set1.setColor(Color.parseColor("#4CAF50")); // Green
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(10f);
        set1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fM", value);
            }
        });
        
        BarDataSet set2 = new BarDataSet(entriesGroup2, "Annual Expenses");
        set2.setColor(Color.parseColor("#F44336")); // Red
        set2.setValueTextColor(Color.WHITE);
        set2.setValueTextSize(10f);
        set2.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fM", value);
            }
        });

        BarData data = new BarData(set1, set2);
        data.setBarWidth(0.35f); // width of the bars

        chartPension.setData(data);
        
        // Group bars
        float groupSpace = 0.15f;
        float barSpace = 0.05f;
        chartPension.groupBars(0.5f, groupSpace, barSpace);
        
        // X-Axis
        XAxis xAxis = chartPension.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(ageLabels));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(6f);
        xAxis.setTextColor(Color.parseColor("#757575"));
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);

        // Y-Axis
        chartPension.getAxisLeft().setTextColor(Color.parseColor("#757575"));
        chartPension.getAxisLeft().setTextSize(12f);
        chartPension.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0fM", value);
            }
        });
        chartPension.getAxisRight().setEnabled(false);
        
        // Legend
        chartPension.getLegend().setEnabled(true);
        chartPension.getLegend().setTextColor(Color.parseColor("#424242"));
        chartPension.getLegend().setTextSize(12f);
        chartPension.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        chartPension.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        chartPension.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        chartPension.getLegend().setDrawInside(false);

        chartPension.getDescription().setEnabled(false);
        chartPension.setBackgroundColor(Color.WHITE);
        chartPension.setExtraOffsets(10f, 20f, 10f, 30f);
        chartPension.animateY(1200);
        chartPension.invalidate();
    }
}
