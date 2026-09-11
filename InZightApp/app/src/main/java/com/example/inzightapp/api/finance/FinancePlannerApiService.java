package com.example.inzightapp.api.finance;

import com.example.inzightapp.model.request.GoalInputRequest;
import com.example.inzightapp.model.request.RetirementInputRequest;
import com.example.inzightapp.model.request.ScenarioInputRequest;
import com.example.inzightapp.model.request.WhatIfInputRequest;
import com.example.inzightapp.model.response.GoalResultResponse;
import com.example.inzightapp.model.response.RetirementResultResponse;
import com.example.inzightapp.model.response.ScenarioResultResponse;
import com.example.inzightapp.model.response.WhatIfResultResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FinancePlannerApiService {
    
    // Retirement
    @POST("api/planfinance/retirement")
    Call<RetirementResultResponse> calculateRetirement(@Body RetirementInputRequest request);
    
    @GET("api/planfinance/retirement")
    Call<List<RetirementResultResponse>> getRetirementHistory();
    
    // Scenario
    @POST("api/planfinance/scenario")
    Call<ScenarioResultResponse> calculateScenario(@Body ScenarioInputRequest request);
    
    @GET("api/planfinance/scenario")
    Call<List<ScenarioResultResponse>> getScenarioHistory();
    
    // WhatIf (Optimizer)
    @POST("api/planfinance/whatif")
    Call<WhatIfResultResponse> calculateWhatIf(@Body WhatIfInputRequest request);
    
    @GET("api/planfinance/whatif")
    Call<List<WhatIfResultResponse>> getWhatIfHistory();
    
    // Goals (Multi-goal)
    @POST("api/planfinance/goals")
    Call<GoalResultResponse> calculateGoal(@Body GoalInputRequest request);
    
    @GET("api/planfinance/goals")
    Call<List<GoalResultResponse>> getGoalHistory();
}

