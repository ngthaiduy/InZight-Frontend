package com.example.inzightapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.FinancePlannerApiService;
import com.example.inzightapp.model.request.GoalInputRequest;
import com.example.inzightapp.model.response.GoalResultResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiGoalFragment extends Fragment {

    private TextInputEditText etGoalName, etTargetAmount, etMonthlySaving, etInterestRate, etDate, etTime;
    private CalendarView calendarView;
    private RecyclerView recyclerIcons;
    private MaterialButton btnCreateGoal;
    private ImageView btnBack;

    private IconAdapter iconAdapter;
    private int selectedIconResId = -1;
    
    private FinancePlannerApiService financePlannerApiService;
    private ProgressBar progressBar;
    
    // List to store multiple goals
    private List<GoalData> goalsList = new ArrayList<>();
    private MaterialButton btnAddAnotherGoal;
    private MaterialButton btnViewDashboard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_goal, container, false);

        initViews(view);
        setupIcons();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide Bottom Navigation
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
        // Show Bottom Navigation when leaving
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initViews(View view) {
        etGoalName = view.findViewById(R.id.etGoalName);
        etTargetAmount = view.findViewById(R.id.etTargetAmount);
        etMonthlySaving = view.findViewById(R.id.etMonthlySaving);
        etInterestRate = view.findViewById(R.id.etInterestRate);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        calendarView = view.findViewById(R.id.calendarView);
        recyclerIcons = view.findViewById(R.id.recyclerIcons);
        btnCreateGoal = view.findViewById(R.id.btnCreateGoal);
        btnBack = view.findViewById(R.id.btnBack);
        btnAddAnotherGoal = view.findViewById(R.id.btnAddAnotherGoal);
        btnViewDashboard = view.findViewById(R.id.btnViewDashboard);
        
        financePlannerApiService = ApiClient.getClient(getContext()).create(FinancePlannerApiService.class);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup currency formatting
        setupCurrencyFormatting();
        
        // Update button visibility based on goals list
        updateButtonVisibility();
    }
    
    private void setupCurrencyFormatting() {
        if (etTargetAmount != null) {
            etTargetAmount.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etTargetAmount));
        }
        if (etMonthlySaving != null) {
            etMonthlySaving.addTextChangedListener(VietnameseCurrencyFormatter.createTextWatcher(etMonthlySaving));
        }
    }
    
    private void updateButtonVisibility() {
        if (btnAddAnotherGoal != null && btnViewDashboard != null) {
            if (goalsList.isEmpty()) {
                btnCreateGoal.setVisibility(View.VISIBLE);
                btnAddAnotherGoal.setVisibility(View.GONE);
                btnViewDashboard.setVisibility(View.GONE);
            } else {
                btnCreateGoal.setVisibility(View.VISIBLE);
                btnAddAnotherGoal.setVisibility(View.VISIBLE);
                btnViewDashboard.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void clearForm() {
        if (etGoalName != null) etGoalName.setText("");
        if (etTargetAmount != null) etTargetAmount.setText("");
        if (etMonthlySaving != null) etMonthlySaving.setText("");
        if (etInterestRate != null) etInterestRate.setText("");
        if (etDate != null) etDate.setText("");
        if (etTime != null) etTime.setText("");
        selectedIconResId = -1;
        if (iconAdapter != null) {
            iconAdapter.clearSelection();
        }
    }

    private void setupIcons() {
        List<Integer> icons = new ArrayList<>();
        // Social icons (~8 items)
        icons.add(R.drawable.ic_social);
        icons.add(R.drawable.ic_message);
        icons.add(R.drawable.ic_heart_filled);
        icons.add(R.drawable.ic_camera);
        icons.add(R.drawable.ic_image);
        icons.add(R.drawable.ic_mic);
        icons.add(R.drawable.ic_videocam);
        icons.add(R.drawable.ic_person_add);

        iconAdapter = new IconAdapter(icons, (iconResId) -> {
            selectedIconResId = iconResId;
        });
        recyclerIcons.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4 columns
        recyclerIcons.setAdapter(iconAdapter);
    }

    private void setupListeners() {
        // Fix back button: popBackStack instead of onBackPressed
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        btnCreateGoal.setOnClickListener(v -> {
            String name = etGoalName.getText().toString().trim();
            double targetAmount = VietnameseCurrencyFormatter.parse(etTargetAmount.getText().toString());
            double monthlySaving = VietnameseCurrencyFormatter.parse(etMonthlySaving != null ? etMonthlySaving.getText().toString() : "");
            String interestRateStr = etInterestRate != null ? etInterestRate.getText().toString().trim() : "";

            if (name.isEmpty() || targetAmount <= 0 || monthlySaving <= 0) {
                Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedIconResId == -1) {
                Toast.makeText(getContext(), "Please select an icon", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                
                if (targetAmount <= 0 || monthlySaving <= 0) {
                    Toast.makeText(getContext(), "Target amount and monthly saving must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (monthlySaving >= targetAmount) {
                    Toast.makeText(getContext(), "Monthly saving must be less than target amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Get interest rate (default 5% if empty)
                double interestRate = 0.05;
                if (!interestRateStr.isEmpty()) {
                    interestRate = Double.parseDouble(interestRateStr) / 100.0; // Convert percentage to decimal
                    if (interestRate < 0 || interestRate > 1) {
                        interestRate = 0.05; // Reset to default if invalid
                    }
                }
                
                String targetDateStr = etDate.getText().toString().trim();
                int priority = 2; // Default medium priority
                
                GoalInputRequest request = new GoalInputRequest(
                    name, 
                    targetAmount, 
                    monthlySaving,
                    interestRate,
                    priority,
                    targetDateStr
                );
                
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

                double finalInterestRate = interestRate;
                financePlannerApiService.calculateGoal(request).enqueue(new Callback<GoalResultResponse>() {
                    @Override
                    public void onResponse(Call<GoalResultResponse> call, Response<GoalResultResponse> response) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            GoalResultResponse result = response.body();
                            
                            // Add goal to list
                            GoalData goalData = new GoalData(
                                name,
                                targetAmount,
                                monthlySaving,
                                finalInterestRate,
                                priority,
                                result.targetDate != null ? result.targetDate : targetDateStr,
                                selectedIconResId
                            );
                            
                            // Store result data for dashboard
                            goalData.monthsNeeded = result.monthsNeeded;
                            goalData.yearsNeeded = result.yearsNeeded;
                            goalData.totalWithInterest = result.totalWithInterest;
                            goalData.interestEarned = result.interestEarned;
                            
                            goalsList.add(goalData);
                            
                            // Clear form and update UI
                            clearForm();
                            updateButtonVisibility();
                            
                            Toast.makeText(getContext(), "Goal added! (" + goalsList.size() + " goals total)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error calculating: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<GoalResultResponse> call, Throwable t) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        // Date Picker for etDate
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, month1, dayOfMonth) -> etDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });

        // Time Picker for etTime
        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minute1) -> etTime.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });
        
        // Sync CalendarView with Date Input (Optional: Update etDate when calendar changes)
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
             etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        });
        
        // Add Another Goal button - just clear form
        if (btnAddAnotherGoal != null) {
            btnAddAnotherGoal.setOnClickListener(v -> {
                clearForm();
            });
        }
        
        // View Dashboard button - navigate to dashboard with all goals
        if (btnViewDashboard != null) {
            btnViewDashboard.setOnClickListener(v -> {
                if (goalsList.isEmpty()) {
                    Toast.makeText(getContext(), "Please create at least one goal first", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Navigate to dashboard with all goals
                navigateToDashboard();
            });
        }
    }
    
    private void navigateToDashboard() {
        if (goalsList.isEmpty()) {
            Toast.makeText(getContext(), "No goals to display", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert goalsList to ArrayList for Bundle
        ArrayList<GoalData> goalsArrayList = new ArrayList<>(goalsList);
        
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("goalsList", goalsArrayList);
        
        GoalDashboardFragment goalDashboard = new GoalDashboardFragment();
        goalDashboard.setArguments(bundle);
        
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, goalDashboard)
                .addToBackStack(null)
                .commit();
    }

    // Inner Adapter Class
    private static class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

        private final List<Integer> icons;
        private final OnIconSelectedListener listener;
        private int selectedPosition = -1;
        
        public void clearSelection() {
            int previous = selectedPosition;
            selectedPosition = -1;
            if (previous >= 0) {
                notifyItemChanged(previous);
            }
        }

        interface OnIconSelectedListener {
            void onIconSelected(int iconResId);
        }

        public IconAdapter(List<Integer> icons, OnIconSelectedListener listener) {
            this.icons = icons;
            this.listener = listener;
        }

        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using a simple FrameLayout with ImageView for the item
            // We can reuse an existing layout or create a view programmatically to save creating a file
            // Let's create programmatically for simplicity as it's just an icon
            android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    160)); // Fixed height
            
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setId(View.generateViewId());
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    120, 120); // Icon size
            params.gravity = android.view.Gravity.CENTER;
            imageView.setLayoutParams(params);
            imageView.setPadding(20, 20, 20, 20);
            
            frameLayout.addView(imageView);
            return new IconViewHolder(frameLayout, imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            int iconRes = icons.get(position);
            holder.imageView.setImageResource(iconRes);
            
            if (selectedPosition == position) {
                holder.imageView.setBackgroundResource(R.drawable.bg_category_item_selected); // Reuse existing background
                holder.imageView.setColorFilter(null); // Original colors
            } else {
                holder.imageView.setBackgroundResource(R.drawable.bg_category_item); // Reuse existing background
                 // Maybe tint it gray or leave as is? Let's leave as is but without selection border
            }

            holder.itemView.setOnClickListener(v -> {
                int previous = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                listener.onIconSelected(iconRes);
            });
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }

        static class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public IconViewHolder(@NonNull View itemView, ImageView imageView) {
                super(itemView);
                this.imageView = imageView;
            }
        }
    }
}
