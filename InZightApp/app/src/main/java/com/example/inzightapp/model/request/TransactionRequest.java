package com.example.inzightapp.model.request;

import java.math.BigDecimal;

public class TransactionRequest {
    private Long walletId;
    private Long categoryId;
    private BigDecimal amount;
    private String type; // "INCOME" | "EXPENSE"
    private String note;
    private String transactionDate;

    public TransactionRequest(Long walletId, Long categoryId, BigDecimal amount, String type, String note) {
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.note = note;
    }

    public TransactionRequest() {

    }

    public Long getWalletId() {
        return walletId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getTransactionDate() {
        return transactionDate;
    }


    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTransactionDate(String transactionDate){ this.transactionDate = transactionDate; }
}
