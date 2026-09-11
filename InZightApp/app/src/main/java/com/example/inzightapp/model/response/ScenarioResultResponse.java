package com.example.inzightapp.model.response;

import java.util.List;

public class ScenarioResultResponse {
    public double adjustedIncome;
    public double adjustedExpense;
    public double netChange;
    public String preset;
    public String presetName;
    public List<YearlyProjection> yearlyProjections;
    public ScenarioRecommendationResponse recommendations;

    public ScenarioResultResponse() {
    }

    public ScenarioResultResponse(double adjustedIncome, double adjustedExpense) {
        this.adjustedIncome = adjustedIncome;
        this.adjustedExpense = adjustedExpense;
    }
    
    public static class YearlyProjection {
        public int year;
        public double income;
        public double expense;
        public double net;
        public double cumulativeImpact;
        
        public YearlyProjection() {
        }
    }
}

