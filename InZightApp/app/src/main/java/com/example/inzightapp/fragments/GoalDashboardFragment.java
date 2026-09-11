package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class GoalDashboardFragment extends Fragment {

    private RecyclerView recyclerGoals;
    private RecyclerView recyclerRecommendations;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_dashboard, container, false);

        initViews(view);
        setupGoals();
        setupRecommendations();
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
        recyclerGoals = view.findViewById(R.id.recyclerGoals);
        recyclerRecommendations = view.findViewById(R.id.recyclerRecommendations);
        btnBack = view.findViewById(R.id.btnBack);
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

    private void setupGoals() {
        List<GoalItem> goals = new ArrayList<>();
        
        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            // Check if we have multiple goals (new format)
            ArrayList<GoalData> goalsList = args.getParcelableArrayList("goalsList");
            
            if (goalsList != null && !goalsList.isEmpty()) {
                // Multiple goals - convert each GoalData to GoalItem
                for (GoalData goalData : goalsList) {
                    GoalItem goalItem = convertGoalDataToItem(goalData);
                    goals.add(goalItem);
                }
            } else {
                // Single goal (old format for backward compatibility)
                String goalName = args.getString("goalName", "New Goal");
                int monthsNeeded = args.getInt("monthsNeeded", 0);
                double targetAmount = args.getDouble("targetAmount", 0);
                double monthlySaving = args.getDouble("monthlySaving", 0);
                int iconResId = args.getInt("iconResId", R.drawable.ic_savings);
                
                GoalData goalData = new GoalData(goalName, targetAmount, monthlySaving, 0.05, 2, "", iconResId);
                goalData.monthsNeeded = monthsNeeded;
                goals.add(convertGoalDataToItem(goalData));
            }
        } else {
            // Fallback to default data if no arguments
            goals.add(new GoalItem("Emergency Funds", "2,5B VND", "1,5B (60%)", 60, "Dec 31, 2025", "18 months left", "On Track", R.drawable.ic_savings, "#1B5E20", "#C8E6C9", "#1565C0")); 
        }

        GoalAdapter adapter = new GoalAdapter(goals);
        recyclerGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerGoals.setAdapter(adapter);
    }
    
    private GoalItem convertGoalDataToItem(GoalData goalData) {
        // Format amounts
        String targetAmountStr = formatAmount(goalData.targetAmount);
        String currentAmountStr = "0 VND (0%)";
        int progress = 0;
        
        // Calculate deadline (months from now)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MONTH, goalData.monthsNeeded);
        String deadline = String.format("%d/%d/%d", 
            calendar.get(java.util.Calendar.DAY_OF_MONTH),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.YEAR));
        
        String timeLeft = goalData.monthsNeeded + " months left";
        
        // Determine status based on months needed
        String status = "On Track";
        String statusColor = "#1B5E20";
        String statusBgColor = "#C8E6C9";
        String progressColor = "#1565C0";
        
        if (goalData.monthsNeeded > 24) {
            status = "At Risk";
            statusColor = "#B71C1C";
            statusBgColor = "#FFCDD2";
            progressColor = "#F44336";
        } else if (goalData.monthsNeeded > 18) {
            status = "Behind";
            statusColor = "#F57F17";
            statusBgColor = "#FFF9C4";
            progressColor = "#FF9800";
        }
        
        return new GoalItem(goalData.name, targetAmountStr, currentAmountStr, progress, deadline, timeLeft, status, goalData.iconResId, statusColor, statusBgColor, progressColor);
    }
    
    private String formatAmount(double amount) {
        // Use Vietnamese currency formatter with dot separator
        return VietnameseCurrencyFormatter.format(amount);
    }

    private void setupRecommendations() {
        // Generate AI recommendations based on goals
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<GoalData> goalsList = args.getParcelableArrayList("goalsList");
            if (goalsList != null && !goalsList.isEmpty()) {
                // Convert to GoalInfo for recommendation generator
                List<com.example.inzightapp.utils.MultiGoalRecommendationGenerator.GoalInfo> goalInfos = new ArrayList<>();
                for (GoalData goalData : goalsList) {
                    goalInfos.add(new com.example.inzightapp.utils.MultiGoalRecommendationGenerator.GoalInfo(
                        goalData.name,
                        goalData.targetAmount,
                        goalData.monthlySaving,
                        goalData.monthsNeeded
                    ));
                }
                
                // Generate recommendations
                com.example.inzightapp.model.response.ScenarioRecommendationResponse recommendations = 
                    com.example.inzightapp.utils.MultiGoalRecommendationGenerator.generateRecommendations(goalInfos);
                
                // Convert to RecommendationItem list
                List<RecommendationItem> recs = new ArrayList<>();
                
                // Add investment recommendations
                if (recommendations.investments != null) {
                    for (com.example.inzightapp.model.response.ScenarioRecommendationResponse.InvestmentRecommendation inv : recommendations.investments) {
                        recs.add(new RecommendationItem(
                            inv.title,
                            inv.description,
                            inv.suggestedAmount > 0 ? VietnameseCurrencyFormatter.format(inv.suggestedAmount) + "/month" : "",
                            R.drawable.ic_bullet_check,
                            true // Show bot icon
                        ));
                    }
                }
                
                // Add warning message
                if (recommendations.warningMessage != null && !recommendations.warningMessage.isEmpty()) {
                    recs.add(new RecommendationItem(
                        "Warning",
                        recommendations.warningMessage,
                        "",
                        R.drawable.ic_warning_red,
                        true
                    ));
                }
                
                RecommendationAdapter adapter = new RecommendationAdapter(recs);
                recyclerRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerRecommendations.setAdapter(adapter);
                return;
            }
        }
        
        // Fallback to default recommendations
        List<RecommendationItem> recs = new ArrayList<>();
        recs.add(new RecommendationItem("Focus on Emergency Funds", "Due to rising gold prices, exchange rate increase 15% and high inflation rate", "Spend 2-3 months", R.drawable.ic_bullet_check, false));
        recs.add(new RecommendationItem("Put off Vacation Plan", "Upcoming lung pandemic, economy recession cause high price in tickets", "Delay for 2 months", R.drawable.ic_warning_red, true));
        recs.add(new RecommendationItem("Defer Buying New Car", "Use that savings to improve your mental and physical wellness first then back when things are done", "Delay for 4-6 months", R.drawable.ic_warning_red, false));

        RecommendationAdapter adapter = new RecommendationAdapter(recs);
        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecommendations.setAdapter(adapter);
    }

    // --- Models ---
    static class GoalItem {
        String title, target, current, deadline, timeLeft, status;
        int progress, iconRes;
        String statusColor, statusBgColor, progressColor;

        public GoalItem(String title, String target, String current, int progress, String deadline, String timeLeft, String status, int iconRes, String statusColor, String statusBgColor, String progressColor) {
            this.title = title;
            this.target = target;
            this.current = current;
            this.progress = progress;
            this.deadline = deadline;
            this.timeLeft = timeLeft;
            this.status = status;
            this.iconRes = iconRes;
            this.statusColor = statusColor;
            this.statusBgColor = statusBgColor;
            this.progressColor = progressColor;
        }
    }

    static class RecommendationItem {
        String title, desc, action;
        int iconRes;
        boolean showBot;

        public RecommendationItem(String title, String desc, String action, int iconRes, boolean showBot) {
            this.title = title;
            this.desc = desc;
            this.action = action;
            this.iconRes = iconRes;
            this.showBot = showBot;
        }
    }

    // --- Adapters ---
    static class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
        private final List<GoalItem> items;

        public GoalAdapter(List<GoalItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_card, parent, false);
            return new GoalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
            GoalItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvTarget.setText(item.target);
            holder.tvCurrent.setText(item.current);
            holder.tvDeadline.setText(item.deadline);
            holder.tvTimeLeft.setText(item.timeLeft);
            holder.tvStatus.setText(item.status);
            holder.progressBar.setProgress(item.progress);
            holder.imgIcon.setImageResource(item.iconRes);

            // Dynamic styling for status badge
            try {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor(item.statusColor));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
                android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) holder.tvStatus.getBackground();
                bg.setColor(android.graphics.Color.parseColor(item.statusBgColor));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Dynamic styling for progress bar
            try {
                android.graphics.drawable.LayerDrawable layerDrawable = (android.graphics.drawable.LayerDrawable) holder.progressBar.getProgressDrawable();
                android.graphics.drawable.Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
                
                // Determine color based on item property
                int progressColor = android.graphics.Color.parseColor(item.progressColor);
                
                // Need to mutate to avoid affecting other items sharing the drawable
                progressDrawable.mutate().setTint(progressColor);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class GoalViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTarget, tvCurrent, tvDeadline, tvTimeLeft, tvStatus;
            ProgressBar progressBar;
            ImageView imgIcon;

            public GoalViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvGoalTitle);
                tvTarget = itemView.findViewById(R.id.tvTargetAmount);
                tvCurrent = itemView.findViewById(R.id.tvCurrentSavings);
                tvDeadline = itemView.findViewById(R.id.tvDeadline);
                tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                progressBar = itemView.findViewById(R.id.progressBar);
                imgIcon = itemView.findViewById(R.id.imgGoalIcon);
            }
        }
    }

    static class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecViewHolder> {
        private final List<RecommendationItem> items;

        public RecommendationAdapter(List<RecommendationItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public RecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
            return new RecViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecViewHolder holder, int position) {
            RecommendationItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.desc);
            holder.tvAction.setText(item.action);
            holder.imgIcon.setImageResource(item.iconRes);
            holder.imgBot.setVisibility(item.showBot ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class RecViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvAction;
            ImageView imgIcon, imgBot;

            public RecViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvRecTitle);
                tvDesc = itemView.findViewById(R.id.tvRecDesc);
                tvAction = itemView.findViewById(R.id.tvRecAction);
                imgIcon = itemView.findViewById(R.id.imgRecIcon);
                imgBot = itemView.findViewById(R.id.imgBot);
            }
        }
    }
}
