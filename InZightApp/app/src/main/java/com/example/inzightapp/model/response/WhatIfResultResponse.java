package com.example.inzightapp.model.response;

import java.util.List;

public class WhatIfResultResponse {
    public double newIncome;
    public double adjustedExpense;
    public double netSavings;
    public double improvement;
    public List<YearlyProjection> yearlyProjections;

    public WhatIfResultResponse() {
    }

    public WhatIfResultResponse(double newIncome, double adjustedExpense) {
        this.newIncome = newIncome;
        this.adjustedExpense = adjustedExpense;
    }
    
    public static class YearlyProjection {
        public int year;
        public double income;
        public double expense;
        public double savings;
        public double cumulativeSavings;
        
        public YearlyProjection() {
        }
    }
}

