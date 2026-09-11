package com.example.inzightapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class StatisticResponse {
    @SerializedName("totalIncome")
    private BigDecimal totalIncome;

    @SerializedName("totalExpense")
    private BigDecimal totalExpense;

    @SerializedName("compareLastMonth")
    private BigDecimal compareLastMonth;

    @SerializedName("categories")
    private List<CategoryStatistic> categories;

    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getCompareLastMonth() { return compareLastMonth; }
    public List<CategoryStatistic> getCategories() { return categories; }
}
