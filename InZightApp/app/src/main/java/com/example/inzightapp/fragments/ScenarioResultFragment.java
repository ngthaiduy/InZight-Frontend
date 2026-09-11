package com.example.inzightapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.InvestmentRecommendationAdapter;
import com.example.inzightapp.adapter.JobRecommendationAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.chat.ChatApiService;
import com.example.inzightapp.model.request.ChatMessageRequest;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.example.inzightapp.model.response.ScenarioRecommendationResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScenarioResultFragment extends Fragment {

    private ImageView btnBack;
    private TextView tvOriginalIncome, tvOriginalExpense;
    private TextView tvAdjustedIncome, tvAdjustedExpense;
    private TextView tvScenarioDescription;
    
    private RecyclerView recyclerInvestments;
    private RecyclerView recyclerEasyJobs;
    private RecyclerView recyclerSkillJobs;
    private ProgressBar progressBarRecommendations;
    
    private InvestmentRecommendationAdapter investmentAdapter;
    private JobRecommendationAdapter easyJobAdapter;
    private JobRecommendationAdapter skillJobAdapter;
    
    private ChatApiService chatApiService;
    private Long currentUserId;
    private NumberFormat numberFormat;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scenario_result, container, false);

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        
        initViews(rootView);
        setupListeners();
        displayResults();

        return rootView;
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
        
        // Find views in GridLayout
        tvOriginalExpense = view.findViewById(R.id.tvOriginalExpense);
        tvAdjustedExpense = view.findViewById(R.id.tvAdjustedExpense);
        tvOriginalIncome = view.findViewById(R.id.tvOriginalIncome);
        tvAdjustedIncome = view.findViewById(R.id.tvAdjustedIncome);
        tvScenarioDescription = view.findViewById(R.id.tvScenarioDescription);
        
        // RecyclerViews
        recyclerInvestments = view.findViewById(R.id.recyclerInvestments);
        recyclerEasyJobs = view.findViewById(R.id.recyclerEasyJobs);
        recyclerSkillJobs = view.findViewById(R.id.recyclerSkillJobs);
        progressBarRecommendations = view.findViewById(R.id.progressBarRecommendations);
        
        // Setup RecyclerViews
        recyclerInvestments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerEasyJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSkillJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapters
        investmentAdapter = new InvestmentRecommendationAdapter(new ArrayList<>());
        easyJobAdapter = new JobRecommendationAdapter(new ArrayList<>());
        skillJobAdapter = new JobRecommendationAdapter(new ArrayList<>());
        
        recyclerInvestments.setAdapter(investmentAdapter);
        recyclerEasyJobs.setAdapter(easyJobAdapter);
        recyclerSkillJobs.setAdapter(skillJobAdapter);
        
        // Get userId from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", -1L);
        
        // Initialize API service
        chatApiService = ApiClient.getClient(requireContext()).create(ChatApiService.class);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }
    
    private void displayResults() {
        Bundle args = getArguments();
        if (args == null) return;
        
        double originalIncome = args.getDouble("originalIncome", 0);
        double originalExpense = args.getDouble("originalExpense", 0);
        double adjustedIncome = args.getDouble("adjustedIncome", 0);
        double adjustedExpense = args.getDouble("adjustedExpense", 0);
        String preset = args.getString("preset", "");
        String presetName = args.getString("presetName", "");
        double netChange = args.getDouble("netChange", 0);
        int durationYears = args.getInt("durationYears", 10);
        
        // Update description based on preset
        if (tvScenarioDescription != null) {
            String description = "Scenario: " + (!presetName.isEmpty() ? presetName : getPresetName(preset));
            if (durationYears > 0) {
                description += " for " + durationYears + " years";
            }
            tvScenarioDescription.setText(description);
        }
        
        // Display actual amounts in VND format (not percentages)
        if (tvOriginalIncome != null) {
            tvOriginalIncome.setText(VietnameseCurrencyFormatter.format(originalIncome));
        }
        if (tvAdjustedIncome != null) {
            tvAdjustedIncome.setText(VietnameseCurrencyFormatter.format(adjustedIncome));
        }
        if (tvOriginalExpense != null) {
            tvOriginalExpense.setText(VietnameseCurrencyFormatter.format(originalExpense));
        }
        if (tvAdjustedExpense != null) {
            tvAdjustedExpense.setText(VietnameseCurrencyFormatter.format(adjustedExpense));
        }
        
        // Generate and display dynamic recommendations
        generateAndDisplayRecommendations(originalIncome, originalExpense, netChange, preset);
    }
    
    private void generateAndDisplayRecommendations(double originalIncome, double originalExpense, 
                                                   double netChange, String preset) {
        // Show loading
        if (progressBarRecommendations != null) {
            progressBarRecommendations.setVisibility(View.VISIBLE);
        }
        
        // Try AI first, fallback to rule-based
        if (currentUserId != null && currentUserId != -1L && chatApiService != null) {
            generateAIRecommendations(originalIncome, originalExpense, netChange, preset);
        } else {
            // Fallback to rule-based
            generateRuleBasedRecommendations(originalIncome, originalExpense, netChange, preset);
        }
    }
    
    private void generateAIRecommendations(double originalIncome, double originalExpense, 
                                          double netChange, String preset) {
        String presetName = getPresetName(preset);
        String prompt = String.format(
            "You are a financial advisor. Based on:\n" +
            "- Monthly Income: %.0f VND\n" +
            "- Monthly Expense: %.0f VND\n" +
            "- Net Change: %.0f VND/month\n" +
            "- Scenario: %s\n\n" +
            "Provide personalized recommendations in JSON format:\n" +
            "{\n" +
            "  \"investments\": [\n" +
            "    {\"title\": \"...\", \"description\": \"...\", \"amount\": 4000000, \"icon\": \"ic_finance\"}\n" +
            "  ],\n" +
            "  \"easyJobs\": [\n" +
            "    {\"title\": \"...\", \"category\": \"easy\", \"earning\": \"+300K\", \"description\": \"...\"}\n" +
            "  ],\n" +
            "  \"skillJobs\": [\n" +
            "    {\"title\": \"...\", \"category\": \"skill-based\", \"earning\": \"50k/post\", \"description\": \"...\"}\n" +
            "  ],\n" +
            "  \"warning\": \"...\"\n" +
            "}\n" +
            "Only return the JSON, no other text.",
            originalIncome, originalExpense, netChange, presetName);
        
        ChatMessageRequest request = new ChatMessageRequest(currentUserId, null, prompt);
        chatApiService.chatWithAi(request).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                if (progressBarRecommendations != null) {
                    progressBarRecommendations.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getContent();
                    ScenarioRecommendationResponse recommendations = parseAIResponse(aiResponse);
                    
                    if (recommendations != null) {
                        displayRecommendations(recommendations);
                    } else {
                        // Fallback to rule-based if parsing fails
                        generateRuleBasedRecommendations(originalIncome, originalExpense, netChange, preset);
                    }
                } else {
                    // Fallback to rule-based
                    generateRuleBasedRecommendations(originalIncome, originalExpense, netChange, preset);
                }
            }
            
            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                if (progressBarRecommendations != null) {
                    progressBarRecommendations.setVisibility(View.GONE);
                }
                // Fallback to rule-based
                generateRuleBasedRecommendations(originalIncome, originalExpense, netChange, preset);
            }
        });
    }
    
    private void generateRuleBasedRecommendations(double originalIncome, double originalExpense, 
                                                   double netChange, String preset) {
        ScenarioRecommendationResponse recommendations = 
            com.example.inzightapp.utils.ScenarioRecommendationGenerator.generateRecommendations(
                originalIncome, originalExpense, netChange, preset);
        displayRecommendations(recommendations);
    }
    
    private ScenarioRecommendationResponse parseAIResponse(String aiResponse) {
        try {
            // Extract JSON from response (might have extra text)
            String jsonStr = aiResponse.trim();
            
            // Try to find JSON object
            int jsonStart = jsonStr.indexOf("{");
            int jsonEnd = jsonStr.lastIndexOf("}") + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonStr = jsonStr.substring(jsonStart, jsonEnd);
            }
            
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            
            ScenarioRecommendationResponse response = new ScenarioRecommendationResponse();
            
            // Parse investments
            if (json.has("investments")) {
                JsonArray investmentsArray = json.getAsJsonArray("investments");
                List<ScenarioRecommendationResponse.InvestmentRecommendation> investments = new ArrayList<>();
                for (JsonElement element : investmentsArray) {
                    JsonObject inv = element.getAsJsonObject();
                    investments.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                        inv.get("title").getAsString(),
                        inv.has("description") ? inv.get("description").getAsString() : "",
                        inv.has("amount") ? inv.get("amount").getAsDouble() : 0,
                        inv.has("icon") ? inv.get("icon").getAsString() : "ic_finance"
                    ));
                }
                response.investments = investments;
            }
            
            // Parse easy jobs
            if (json.has("easyJobs")) {
                JsonArray jobsArray = json.getAsJsonArray("easyJobs");
                List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
                for (JsonElement element : jobsArray) {
                    JsonObject job = element.getAsJsonObject();
                    jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                        job.get("title").getAsString(),
                        "easy",
                        job.has("earning") ? job.get("earning").getAsString() : "",
                        job.has("description") ? job.get("description").getAsString() : ""
                    ));
                }
                response.easyJobs = jobs;
            }
            
            // Parse skill jobs
            if (json.has("skillJobs")) {
                JsonArray jobsArray = json.getAsJsonArray("skillJobs");
                List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
                for (JsonElement element : jobsArray) {
                    JsonObject job = element.getAsJsonObject();
                    jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                        job.get("title").getAsString(),
                        "skill-based",
                        job.has("earning") ? job.get("earning").getAsString() : "",
                        job.has("description") ? job.get("description").getAsString() : ""
                    ));
                }
                response.skillJobs = jobs;
            }
            
            // Parse warning
            if (json.has("warning")) {
                response.warningMessage = json.get("warning").getAsString();
            }
            
            return response;
        } catch (Exception e) {
            android.util.Log.e("ScenarioResult", "Error parsing AI response", e);
            return null;
        }
    }
    
    private void displayRecommendations(ScenarioRecommendationResponse recommendations) {
        if (recommendations == null) return;
        
        // Display investments
        if (recommendations.investments != null && !recommendations.investments.isEmpty()) {
            investmentAdapter.updateItems(recommendations.investments);
        }
        
        // Display easy jobs
        if (recommendations.easyJobs != null && !recommendations.easyJobs.isEmpty()) {
            easyJobAdapter.updateItems(recommendations.easyJobs);
        }
        
        // Display skill jobs
        if (recommendations.skillJobs != null && !recommendations.skillJobs.isEmpty()) {
            skillJobAdapter.updateItems(recommendations.skillJobs);
        }
        
        // Display warning
        displayWarning(recommendations.warningMessage);
    }
    
    private void displayWarning(String warningMessage) {
        // Find warning TextView and update
        TextView tvWarning = rootView.findViewById(R.id.tvWarningMessage);
        if (tvWarning != null && warningMessage != null) {
            tvWarning.setText(warningMessage);
        }
    }
    
    private String getPresetName(String preset) {
        switch (preset) {
            case "inflation_low":
                return "Low Inflation";
            case "inflation_high":
                return "High Inflation";
            case "crisis_light":
                return "Light Crisis";
            case "crisis_medium":
                return "Medium Crisis";
            case "crisis_severe":
                return "Severe Crisis";
            case "pandemic":
                return "Pandemic";
            default:
                return "Default Scenario";
        }
    }
}
