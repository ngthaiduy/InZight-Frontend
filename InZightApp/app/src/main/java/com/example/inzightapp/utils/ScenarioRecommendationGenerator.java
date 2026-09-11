package com.example.inzightapp.utils;

import com.example.inzightapp.model.response.ScenarioRecommendationResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates personalized recommendations based on scenario results
 * This can be replaced with AI service later
 */
public class ScenarioRecommendationGenerator {
    
    /**
     * Generate recommendations based on financial situation
     */
    public static ScenarioRecommendationResponse generateRecommendations(
            double originalIncome, 
            double originalExpense, 
            double netChange, 
            String preset) {
        
        ScenarioRecommendationResponse response = new ScenarioRecommendationResponse();
        
        // Calculate net savings
        double originalNet = originalIncome - originalExpense;
        double adjustedNet = originalNet + netChange;
        
        // Generate investment recommendations
        response.investments = generateInvestmentRecommendations(originalNet, adjustedNet, netChange);
        
        // Generate job recommendations
        response.easyJobs = generateEasyJobs(originalIncome, netChange);
        response.skillJobs = generateSkillJobs(originalIncome, netChange);
        
        // Generate warning message
        response.warningMessage = generateWarningMessage(netChange, preset);
        
        return response;
    }
    
    private static List<ScenarioRecommendationResponse.InvestmentRecommendation> 
            generateInvestmentRecommendations(double originalNet, double adjustedNet, double netChange) {
        
        List<ScenarioRecommendationResponse.InvestmentRecommendation> recommendations = new ArrayList<>();
        
        // If net change is negative, focus on emergency fund and spending control
        if (netChange < 0) {
            double emergencyFundAmount = Math.abs(netChange) * 0.3; // 30% of gap
            if (emergencyFundAmount > 0) {
                recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                    "Allocate " + formatAmount(emergencyFundAmount) + "/month in emergency fund",
                    "Build emergency fund to handle unexpected expenses",
                    emergencyFundAmount,
                    "ic_add"
                ));
            }
            
            // Spending cap recommendation
            recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                "Set monthly discretionary spending cap",
                "Control spending to prevent pulling from investments prematurely",
                0,
                "ic_wallet"
            ));
        }
        
        // Investment recommendation based on available savings
        if (adjustedNet > 0) {
            double investmentAmount = adjustedNet * 0.3; // 30% of net savings
            if (investmentAmount >= 1_000_000) { // Only if >= 1M
                recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                    "Auto-invest " + formatAmount(investmentAmount) + "/month in growth ETFs",
                    "Grow your savings with diversified investments",
                    investmentAmount,
                    "ic_finance"
                ));
            }
        }
        
        // Ensure at least 2 recommendations
        if (recommendations.size() < 2) {
            recommendations.add(new ScenarioRecommendationResponse.InvestmentRecommendation(
                "Review and optimize monthly expenses",
                "Identify areas to reduce spending",
                0,
                "ic_wallet"
            ));
        }
        
        return recommendations;
    }
    
    private static List<ScenarioRecommendationResponse.JobRecommendation> 
            generateEasyJobs(double income, double netChange) {
        
        List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
        
        // Calculate how much extra income is needed
        double extraIncomeNeeded = Math.abs(netChange);
        
        // If net change is negative, suggest jobs to close the gap
        if (netChange < 0) {
            // Suggest jobs based on income level
            if (income < 10_000_000) {
                jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                    "Grab delivery", "easy", "+300K", "Flexible hours, no skill required"
                ));
                jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                    "Waitor /waitress", "easy", "+200K", "Part-time restaurant work"
                ));
            }
            
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Sell 2nd hand clothes", "easy", "+500K", "Sell unused items online"
            ));
            
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Private tutoring", "easy", "+400K", "Teach subjects you know well"
            ));
            
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Freelancer", "easy", "Depends", "Various freelance opportunities"
            ));
        } else {
            // If net change is positive, suggest jobs for extra income
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Grab delivery", "easy", "+300K", "Earn extra income in spare time"
            ));
            jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
                "Sell 2nd hand clothes", "easy", "+500K", "Monetize unused items"
            ));
        }
        
        return jobs;
    }
    
    private static List<ScenarioRecommendationResponse.JobRecommendation> 
            generateSkillJobs(double income, double netChange) {
        
        List<ScenarioRecommendationResponse.JobRecommendation> jobs = new ArrayList<>();
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Write blogs or SEO posts", "skill-based", "50k/post", "Content writing for websites"
        ));
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Create social posters", "skill-based", "200K/design", "Graphic design services"
        ));
        
        jobs.add(new ScenarioRecommendationResponse.JobRecommendation(
            "Build simple website", "skill-based", "500-1M", "Web development projects"
        ));
        
        return jobs;
    }
    
    private static String generateWarningMessage(double netChange, String preset) {
        if (netChange < 0) {
            return "Unless you set spending limits, unexpected expenses could force you to pull from investments prematurely.";
        } else {
            return "Consider increasing your investment allocation to maximize growth potential.";
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
}

