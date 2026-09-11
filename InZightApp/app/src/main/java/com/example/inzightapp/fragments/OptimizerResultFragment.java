package com.example.inzightapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OptimizerResultFragment extends Fragment {

    private ImageView btnBack;
    private LineChart chartOptimizer;
    private TextView tvNewIncome, tvAdjustedExpense, tvOriginalIncome, tvOriginalExpense;
    private LinearLayout layoutFinancialFuture;
    private NumberFormat numberFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_optimizer_result, container, false);

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        initViews(view);
        setupListeners();
        displayData();
        setupChart();
        setupFinancialFutureGoals();

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
        chartOptimizer = view.findViewById(R.id.chartOptimizer);
        tvNewIncome = view.findViewById(R.id.tvNewIncome);
        tvAdjustedExpense = view.findViewById(R.id.tvAdjustedExpense);
        tvOriginalIncome = view.findViewById(R.id.tvOriginalIncome);
        tvOriginalExpense = view.findViewById(R.id.tvOriginalExpense);
        layoutFinancialFuture = view.findViewById(R.id.layoutFinancialFuture);
    }
    
    private void displayData() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double newIncome = args.getDouble("newIncome", 0);
        double adjustedExpense = args.getDouble("adjustedExpense", 0);
        double originalIncome = args.getDouble("originalIncome", 0);
        double originalExpense = args.getDouble("originalExpense", 0);
        
        if (tvNewIncome != null) {
            tvNewIncome.setText(formatAmount(newIncome));
        }
        if (tvAdjustedExpense != null) {
            tvAdjustedExpense.setText(formatAmount(adjustedExpense));
        }
        if (tvOriginalIncome != null) {
            tvOriginalIncome.setText(formatAmount(originalIncome));
        }
        if (tvOriginalExpense != null) {
            tvOriginalExpense.setText(formatAmount(originalExpense));
        }
    }
    
    private String formatAmount(double amount) {
        // Use Vietnamese currency formatter with dot separator
        return VietnameseCurrencyFormatter.format(amount);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void setupChart() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double originalIncome = args.getDouble("originalIncome", 0);
        double originalExpense = args.getDouble("originalExpense", 0);
        double newIncome = args.getDouble("newIncome", 0);
        double adjustedExpense = args.getDouble("adjustedExpense", 0);
        double inflation = args.getDouble("inflation", 0) / 100.0; // Convert percentage to decimal
        String returnRateStr = args.getString("returnRate", "SAFE");
        
        // Get return rate from user input (if available) or use default based on strategy
        double returnRate = args.getDouble("returnRateValue", 0) / 100.0;
        if (returnRate <= 0) {
            // Fallback to strategy-based default
            if ("HIGH".equals(returnRateStr)) {
                returnRate = 0.10; // 10% for high risk
            } else if ("MODERATE".equals(returnRateStr)) {
                returnRate = 0.07; // 7% for moderate
            } else {
                returnRate = 0.05; // 5% for safe
            }
        }
        
        // Expense growth rate (slower than inflation)
        double expenseGrowthRate = inflation * 0.8;
        
        // Calculate net savings (income - expense) for each year
        List<Entry> entries = new ArrayList<>(); // Current path
        List<Entry> entries2 = new ArrayList<>(); // Optimized path
        
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        
        // Project 15 years into the future
        for (int i = 0; i <= 15; i++) {
            int year = currentYear + i;
            
            // Current path: income and expense grow with inflation
            double currentYearIncome = originalIncome * Math.pow(1 + inflation, i);
            double currentYearExpense = originalExpense * Math.pow(1 + inflation, i);
            double currentNetSavings = (currentYearIncome - currentYearExpense) / 1_000_000; // Convert to millions
            
            // Optimized path: income grows with return rate, expense grows slower
            double optimizedYearIncome = newIncome * Math.pow(1 + returnRate, i);
            double optimizedYearExpense = adjustedExpense * Math.pow(1 + expenseGrowthRate, i);
            double optimizedNetSavings = (optimizedYearIncome - optimizedYearExpense) / 1_000_000;
            
            entries.add(new Entry(year, (float) currentNetSavings));
            entries2.add(new Entry(year, (float) optimizedNetSavings));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Hiện tại");
        dataSet.setColor(Color.parseColor("#FF5252")); // Red
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#FF5252"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFEBEE"));
        dataSet.setFillAlpha(100);

        LineDataSet dataSet2 = new LineDataSet(entries2, "Tối ưu");
        dataSet2.setColor(Color.parseColor("#448AFF")); // Blue
        dataSet2.setLineWidth(3f);
        dataSet2.setDrawCircles(true);
        dataSet2.setCircleColor(Color.parseColor("#448AFF"));
        dataSet2.setCircleRadius(4f);
        dataSet2.setDrawValues(false);
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setDrawFilled(false);

        LineData lineData = new LineData(dataSet, dataSet2);
        chartOptimizer.setData(lineData);

        // General Chart Styling
        chartOptimizer.setBackgroundColor(Color.WHITE);
        chartOptimizer.setDrawGridBackground(false);
        chartOptimizer.setDrawBorders(false);
        chartOptimizer.getDescription().setEnabled(false);
        chartOptimizer.getLegend().setEnabled(false);
        chartOptimizer.setExtraOffsets(10f, 20f, 10f, 10f); // Add padding

        // X-Axis Styling
        XAxis xAxis = chartOptimizer.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(3f);
        xAxis.setTextColor(Color.parseColor("#757575"));
        xAxis.setTextSize(11f);
        xAxis.setAxisLineColor(Color.parseColor("#E0E0E0"));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Left Y-Axis Styling
        YAxis leftAxis = chartOptimizer.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F5F5F5"));
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setTextColor(Color.parseColor("#757575"));
        leftAxis.setTextSize(11f);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setDrawZeroLine(true);
        leftAxis.setZeroLineColor(Color.parseColor("#E0E0E0"));
        leftAxis.setZeroLineWidth(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0fM", value);
            }
        });
        
        chartOptimizer.getAxisRight().setEnabled(false);
        
        // Legend
        chartOptimizer.getLegend().setEnabled(true);
        chartOptimizer.getLegend().setTextColor(Color.parseColor("#424242"));
        chartOptimizer.getLegend().setTextSize(12f);
        chartOptimizer.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        chartOptimizer.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        chartOptimizer.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        chartOptimizer.getLegend().setDrawInside(false);

        chartOptimizer.animateX(1500);
        chartOptimizer.invalidate();
    }
    
    /**
     * Calculate and display financial future goals based on input and results
     */
    private void setupFinancialFutureGoals() {
        Bundle args = getArguments();
        if (args == null || layoutFinancialFuture == null) return;
        
        double originalIncome = args.getDouble("originalIncome", 0);
        double originalExpense = args.getDouble("originalExpense", 0);
        double newIncome = args.getDouble("newIncome", 0);
        double adjustedExpense = args.getDouble("adjustedExpense", 0);
        double returnRateValue = args.getDouble("returnRateValue", 7.0);
        double inflation = args.getDouble("inflation", 0) / 100.0;
        int projectionYears = args.getInt("projectionYears", 15);
        
        if (originalIncome <= 0 || newIncome <= 0) return;
        
        layoutFinancialFuture.removeAllViews();
        
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        double returnRate = returnRateValue / 100.0;
        
        // Calculate net savings per month
        double currentNetSavings = originalIncome - originalExpense;
        double optimizedNetSavings = newIncome - adjustedExpense;
        
        // Goal 1: Major Purchase (House/Car) - when accumulated savings reach threshold
        double majorPurchaseThreshold = originalIncome * 20; // 20 months of income
        int yearsToMajorPurchase = calculateYearsToGoal(optimizedNetSavings, majorPurchaseThreshold, returnRate, inflation);
        if (yearsToMajorPurchase > 0 && yearsToMajorPurchase <= projectionYears) {
            addGoalItem(layoutFinancialFuture, 
                R.drawable.ic_home, 
                "Major Purchase Goal", 
                "In " + (currentYear + yearsToMajorPurchase),
                yearsToMajorPurchase <= 5);
        }
        
        // Goal 2: Cash Flow Stability - when optimized savings consistently exceed current
        if (optimizedNetSavings > currentNetSavings * 1.2) {
            addGoalItem(layoutFinancialFuture, 
                R.drawable.ic_savings, 
                "Cash Flow Stability", 
                "Achieved",
                true);
        } else {
            int yearsToStability = calculateYearsToStability(currentNetSavings, optimizedNetSavings, returnRate, inflation);
            if (yearsToStability > 0 && yearsToStability <= projectionYears) {
                addGoalItem(layoutFinancialFuture, 
                    R.drawable.ic_savings, 
                    "Cash Flow Stability", 
                    "In " + (currentYear + yearsToStability),
                    false);
            }
        }
        
        // Goal 3: Passive Income Growth - when passive income from investments covers expenses
        double passiveIncomeTarget = adjustedExpense * 0.5; // 50% of expenses from passive income
        int yearsToPassiveIncome = calculateYearsToPassiveIncome(optimizedNetSavings, passiveIncomeTarget, returnRate, inflation);
        if (yearsToPassiveIncome > 0 && yearsToPassiveIncome <= projectionYears) {
            addGoalItem(layoutFinancialFuture, 
                R.drawable.ic_finance, 
                "Passive Income Growth", 
                "In " + (currentYear + yearsToPassiveIncome),
                yearsToPassiveIncome <= 7);
        }
        
        // If no goals calculated, show a default message
        if (layoutFinancialFuture.getChildCount() == 0) {
            addGoalItem(layoutFinancialFuture, 
                R.drawable.ic_savings, 
                "Continue Optimizing", 
                "Keep saving consistently",
                false);
        }
    }
    
    /**
     * Calculate years needed to reach a savings goal
     */
    private int calculateYearsToGoal(double monthlySavings, double target, double returnRate, double inflation) {
        if (monthlySavings <= 0) return -1;
        
        double annualSavings = monthlySavings * 12;
        double realReturnRate = returnRate - inflation;
        
        if (realReturnRate <= 0) {
            // No real return, simple calculation
            return (int) Math.ceil(target / annualSavings);
        }
        
        // With compound interest: FV = PMT * (((1 + r)^n - 1) / r)
        // Solve for n: n = log((FV * r / PMT) + 1) / log(1 + r)
        double n = Math.log((target * realReturnRate / annualSavings) + 1) / Math.log(1 + realReturnRate);
        return (int) Math.ceil(n);
    }
    
    /**
     * Calculate years to achieve cash flow stability
     */
    private int calculateYearsToStability(double currentNet, double optimizedNet, double returnRate, double inflation) {
        if (optimizedNet <= currentNet) return -1;
        
        double growthRate = returnRate - inflation;
        if (growthRate <= 0) return 3; // Default 3 years
        
        // Calculate when optimized net exceeds current by 20%
        double target = currentNet * 1.2;
        double n = Math.log(target / optimizedNet) / Math.log(1 + growthRate);
        return (int) Math.ceil(n);
    }
    
    /**
     * Calculate years to achieve passive income goal
     */
    private int calculateYearsToPassiveIncome(double monthlySavings, double passiveIncomeTarget, double returnRate, double inflation) {
        if (monthlySavings <= 0 || returnRate <= 0) return -1;
        
        // Calculate required principal: P = I / r (where I is annual passive income needed)
        double annualPassiveIncomeNeeded = passiveIncomeTarget * 12;
        double requiredPrincipal = annualPassiveIncomeNeeded / returnRate;
        
        return calculateYearsToGoal(monthlySavings, requiredPrincipal, returnRate, inflation);
    }
    
    /**
     * Add a goal item to the layout
     */
    private void addGoalItem(LinearLayout parent, int iconRes, String title, String subtitle, boolean isAchievable) {
        if (getContext() == null) return;
        
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setBackgroundResource(R.drawable.bg_home_switch_item);
        itemLayout.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(isAchievable ? "#C8E6C9" : "#E3F2FD")));
        itemLayout.setPadding(
            (int) (16 * getResources().getDisplayMetrics().density),
            (int) (16 * getResources().getDisplayMetrics().density),
            (int) (16 * getResources().getDisplayMetrics().density),
            (int) (16 * getResources().getDisplayMetrics().density)
        );
        itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        itemLayout.setLayoutParams(params);
        
        // Icon
        FrameLayout iconFrame = new FrameLayout(getContext());
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
            (int) (48 * getResources().getDisplayMetrics().density),
            (int) (48 * getResources().getDisplayMetrics().density)
        );
        iconFrame.setLayoutParams(iconParams);
        iconFrame.setBackgroundResource(R.drawable.bg_circle_white);
        iconFrame.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#BBDEFB")));
        
        ImageView icon = new ImageView(getContext());
        FrameLayout.LayoutParams iconImgParams = new FrameLayout.LayoutParams(
            (int) (28 * getResources().getDisplayMetrics().density),
            (int) (28 * getResources().getDisplayMetrics().density)
        );
        iconImgParams.gravity = android.view.Gravity.CENTER;
        icon.setLayoutParams(iconImgParams);
        icon.setImageResource(iconRes);
        icon.setColorFilter(android.graphics.Color.WHITE);
        iconFrame.addView(icon);
        
        // Title
        TextView titleView = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        titleParams.setMarginStart((int) (16 * getResources().getDisplayMetrics().density));
        titleView.setLayoutParams(titleParams);
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(R.color.home_text_strong));
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Subtitle
        TextView subtitleView = new TextView(getContext());
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleView.setLayoutParams(subtitleParams);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(getResources().getColor(R.color.home_text_strong));
        subtitleView.setTextSize(14);
        
        itemLayout.addView(iconFrame);
        itemLayout.addView(titleView);
        itemLayout.addView(subtitleView);
        
        parent.addView(itemLayout);
    }
}
