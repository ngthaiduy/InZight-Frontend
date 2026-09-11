package com.example.inzightapp.model.request;

public class ScenarioInputRequest {
    public double income;
    public double expense;
    public String preset;
    public int projectionYears;

    public ScenarioInputRequest() {
        this.projectionYears = 10;
    }

    public ScenarioInputRequest(double income, double expense, String preset) {
        this.income = income;
        this.expense = expense;
        this.preset = preset;
        this.projectionYears = 10;
    }
}

