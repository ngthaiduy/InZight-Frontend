package com.example.inzightapp.model.response;

import java.util.List;

public class ScenarioRecommendationResponse {
    public List<InvestmentRecommendation> investments;
    public List<JobRecommendation> easyJobs;
    public List<JobRecommendation> skillJobs;
    public String warningMessage;
    
    public ScenarioRecommendationResponse() {
    }
    
    public static class InvestmentRecommendation {
        public String title;
        public String description;
        public double suggestedAmount; // in VND
        public String icon; // icon resource name
        
        public InvestmentRecommendation() {
        }
        
        public InvestmentRecommendation(String title, String description, double suggestedAmount, String icon) {
            this.title = title;
            this.description = description;
            this.suggestedAmount = suggestedAmount;
            this.icon = icon;
        }
    }
    
    public static class JobRecommendation {
        public String title;
        public String category; // "easy" or "skill-based"
        public String estimatedEarning; // "+300K" or "50k/post"
        public String description;
        
        public JobRecommendation() {
        }
        
        public JobRecommendation(String title, String category, String estimatedEarning, String description) {
            this.title = title;
            this.category = category;
            this.estimatedEarning = estimatedEarning;
            this.description = description;
        }
    }
}

