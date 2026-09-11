package com.example.inzightapp.model.request;

// PaymentPlan.java

import androidx.annotation.NonNull;

public class PaymentPlan {
    private final String id;
    private final String name;
    private final String priceDisplay;
    private final String billingInfo;
    private final boolean recommended;

    public PaymentPlan(@NonNull String id,
                       @NonNull String name,
                       @NonNull String priceDisplay,
                       @NonNull String billingInfo,
                       boolean recommended) {
        this.id = id;
        this.name = name;
        this.priceDisplay = priceDisplay;
        this.billingInfo = billingInfo;
        this.recommended = recommended;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPriceDisplay() {
        return priceDisplay;
    }

    @NonNull
    public String getBillingInfo() {
        return billingInfo;
    }

    public boolean isRecommended() {
        return recommended;
    }
}