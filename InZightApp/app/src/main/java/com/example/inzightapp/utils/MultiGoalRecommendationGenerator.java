package com.example.inzightapp.utils;

import com.example.inzightapp.model.response.ScenarioRecommendationResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates personalized recommendations for multi-goal planning
 * This can be replaced with AI service later
 */
public class MultiGoalRecommendationGenerator {
    
    /**
     * Generate recommendations based on multiple goals
     */
    public static ScenarioRecommendationResponse generateRecommendations(
            List<GoalInfo> goals) {
        
        ScenarioRecommendationResponse response = new ScenarioRecommendationResponse();
        
        if (goals == null || goals.isEmpty()) {
            return response;
        }
        
        // Calculate total monthly savings needed
        double totalMonthlySavings = 0;
        double totalTargetAmount = 0;
        int totalMonths = 0;
        
        for (GoalInfo goal : goals) {
            totalMonthlySavings += goal.monthlySaving;
            totalTargetAmount += goal.targetAmount;
            if (goal.monthsNeeded > totalMonths) {
                totalMonths = goal.monthsNeeded;
            }
        }
        
        // Generate investment recommendations
        response.investments = generateInvestmentRecommendations(goals, totalMonthlySavings);
        
        // Generate job recommendations
        response.easyJobs = generateEasyJobs(totalMonthlySavings);
        response.skillJobs = generateSkillJobs();
        
        // Generate warning message
        response.warningMessage = generateWarningMessage(goals, totalMonthlySavings);
        
        return response;
    }
    
    private static List<ScenarioRecommendationResponse.InvestmentRecommendation> 
            generateInvestmentRecommendations(List<GoalInfo> goals, double totalMonthlySavings) {
        
        List<ScenarioRecommendationResponse.InvestmentRecommendation> recommendations = new ArrayList<>();
        
        // If total monthly savings is high, suggest prioritizing
        if (goals.size() > 2) {
            recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                "Prioritize goals by urgency",
                "Focus on goals with shorter deadlines first",
                0,
                "ic_wallet"
            ));
        }
        
        // Suggest emergency fund if no emergency goal exists
        boolean hasEmergencyFund = false;
        for (GoalInfo goal : goals) {
            if (goal.name.toLowerCase().contains("emergency") || 
                goal.name.toLowerCase().contains("fund")) {
                hasEmergencyFund = true;
                break;
            }
        }
        
        if (!hasEmergencyFund) {
            double emergencyAmount = totalMonthlySavings * 0.2; // 20% of monthly savings
            recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                "Allocate " + formatAmount(emergencyAmount) + "/month for emergency fund",
                "Build emergency fund before other goals",
                emergencyAmount,
                "ic_add"
            ));
        }
        
        // Investment recommendation
        if (totalMonthlySavings > 5_000_000) {
            double investmentAmount = totalMonthlySavings * 0.3;
            recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                "Auto-invest " + formatAmount(investmentAmount) + "/month in growth ETFs",
                "Grow savings faster with investments",
                investmentAmount,
                "ic_finance"
            ));
        }
        
        return recommendations;
    }
    
    private static List<ScenarioRecommendationResponse.JobRecommendation> 
            generateEasyJobs(double totalMonthlySavings) {
        
        List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
        
        // If monthly savings needed is high, suggest more jobs
        if (totalMonthlySavings > 10_000_000) {
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Grab delivery", "easy", "+300K", "Flexible part-time work"
            ));
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Sell 2nd hand clothes", "easy", "+500K", "Monetize unused items"
            ));
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Private tutoring", "easy", "+400K", "Share your knowledge"
            ));
        } else {
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Grab delivery", "easy", "+300K", "Earn extra income"
            ));
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Sell 2nd hand clothes", "easy", "+500K", "Quick way to earn"
            ));
        }
        
        return jobs;
    }
    
    private static List<ScenarioRecommendationResponse.JobRecommendation> 
            generateSkillJobs() {
        
        List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Write blogs or SEO posts", "skill-based", "50k/post", "Content writing"
        ));
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Create social posters", "skill-based", "200K/design", "Graphic design"
        ));
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Build simple website", "skill-based", "500-1M", "Web development"
        ));
        
        return jobs;
    }
    
    private static String generateWarningMessage(List<GoalInfo> goals, double totalMonthlySavings) {
        if (goals.size() > 3) {
            return "You have many goals. Consider prioritizing to avoid spreading savings too thin.";
        } else if (totalMonthlySavings > 15_000_000) {
            return "High monthly savings target. Review your expenses to ensure sustainability.";
        } else {
            return "Stay consistent with monthly savings to achieve your goals on time.";
        }
    }
    
    private static String formatAmount(double amount) {
        if (amount >= 1_000_000) {
            return String.format("%.0fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.0fK", amount / 1_000);
        } else {
            return String.format("%.0f", amount);
        }
    }
    
    // Helper class to pass goal information
    public static class GoalInfo {
        public String name;
        public double targetAmount;
        public double monthlySaving;
        public int monthsNeeded;
        
        public GoalInfo(String name, double targetAmount, double monthlySaving, int monthsNeeded) {
            this.name = name;
            this.targetAmount = targetAmount;
            this.monthlySaving = monthlySaving;
            this.monthsNeeded = monthsNeeded;
        }
    }
}

