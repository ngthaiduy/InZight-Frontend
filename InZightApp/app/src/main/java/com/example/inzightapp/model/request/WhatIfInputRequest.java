package com.example.inzightapp.model.request;

public class WhatIfInputRequest {
    public double income;
    public double expense;
    public double incomeChange;
    public double inflationRate;
    public String returnRate;
    public int projectionYears;

    public WhatIfInputRequest() {
        this.inflationRate = 0.03;
        this.projectionYears = 15;
    }

    public WhatIfInputRequest(double income, double expense, double incomeChange) {
        this.income = income;
        this.expense = expense;
        this.incomeChange = incomeChange;
        this.inflationRate = 0.03;
        this.projectionYears = 15;
    }
    
    public WhatIfInputRequest(double income, double expense, double incomeChange, 
                              double inflationRate, String returnRate, int projectionYears) {
        this.income = income;
        this.expense = expense;
        this.incomeChange = incomeChange;
        this.inflationRate = inflationRate;
        this.returnRate = returnRate;
        this.projectionYears = projectionYears;
    }
}

