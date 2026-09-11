package com.example.inzightapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class CategoryStatistic {
    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("percent")
    private double percent;

    public String getCategoryName() { return categoryName; }
    public BigDecimal getAmount() { return amount; }
    public double getPercent() { return percent; }
}
