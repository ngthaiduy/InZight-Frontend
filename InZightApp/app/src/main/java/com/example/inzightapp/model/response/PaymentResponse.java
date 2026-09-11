package com.example.inzightapp.model.response;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("orderCode")
    private Long orderCode;

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }
}

