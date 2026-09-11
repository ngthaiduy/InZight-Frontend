package com.example.inzightapp.model.request;

public class GoalInputRequest {
    public String name;
    public double targetAmount;
    public double monthlySaving;
    public double interestRate;
    public int priority;
    public String targetDate;

    public GoalInputRequest() {
        this.interestRate = 0.05;
        this.priority = 2;
    }

    public GoalInputRequest(String name, double targetAmount, double monthlySaving) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.monthlySaving = monthlySaving;
        this.interestRate = 0.05;
        this.priority = 2;
    }
    
    public GoalInputRequest(String name, double targetAmount, double monthlySaving, 
                           double interestRate, int priority, String targetDate) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.monthlySaving = monthlySaving;
        this.interestRate = interestRate;
        this.priority = priority;
        this.targetDate = targetDate;
    }
}

