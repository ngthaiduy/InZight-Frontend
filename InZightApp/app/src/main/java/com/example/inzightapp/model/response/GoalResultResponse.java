package com.example.inzightapp.model.response;

import java.util.List;

public class GoalResultResponse {
    public String name;
    public int monthsNeeded;
    public int yearsNeeded;
    public double totalWithInterest;
    public double interestEarned;
    public String targetDate;
    public List<Milestone> milestones;
    public int priority;

    public GoalResultResponse() {
    }

    public GoalResultResponse(String name, int monthsNeeded) {
        this.name = name;
        this.monthsNeeded = monthsNeeded;
    }
    
    public static class Milestone {
        public int month;
        public double amount;
        public double progress;
        public String description;
        
        public Milestone() {
        }
    }
}

