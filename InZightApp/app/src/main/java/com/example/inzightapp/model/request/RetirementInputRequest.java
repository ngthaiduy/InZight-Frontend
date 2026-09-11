package com.example.inzightapp.model.request;

public class RetirementInputRequest {
    public double currentSavings;
    public double monthlyExpense;
    public double monthlySavings;
    public double inflationRate;
    public String investmentType;
    public int ageNow;
    public int retireAge;

    public RetirementInputRequest() {
        this.monthlySavings = 0;
        this.inflationRate = 0.03;
    }

    public RetirementInputRequest(double currentSavings, double monthlyExpense, String investmentType, int ageNow, int retireAge) {
        this.currentSavings = currentSavings;
        this.monthlyExpense = monthlyExpense;
        this.investmentType = investmentType;
        this.ageNow = ageNow;
        this.retireAge = retireAge;
        this.monthlySavings = 0;
        this.inflationRate = 0.03;
    }
    
    public RetirementInputRequest(double currentSavings, double monthlyExpense, double monthlySavings, 
                                 double inflationRate, String investmentType, int ageNow, int retireAge) {
        this.currentSavings = currentSavings;
        this.monthlyExpense = monthlyExpense;
        this.monthlySavings = monthlySavings;
        this.inflationRate = inflationRate;
        this.investmentType = investmentType;
        this.ageNow = ageNow;
        this.retireAge = retireAge;
    }
}

