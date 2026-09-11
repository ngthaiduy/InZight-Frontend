package com.example.inzightapp.model.response;

import java.util.List;

public class RetirementResultResponse {
    public double futureValue;
    public int sustainableYears;
    public double monthlySavingsNeeded;
    public double retirementGap;
    public double totalNeeded;
    public List<YearlyProjection> yearlyProjections;

    public RetirementResultResponse() {
    }

    public RetirementResultResponse(double futureValue, int sustainableYears) {
        this.futureValue = futureValue;
        this.sustainableYears = sustainableYears;
    }
    
    public static class YearlyProjection {
        public int year;
        public double savings;
        public double expense;
        public double net;
        
        public YearlyProjection() {
        }
    }
}

