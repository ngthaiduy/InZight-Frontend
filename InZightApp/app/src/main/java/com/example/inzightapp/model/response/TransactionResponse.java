package com.example.inzightapp.model.response;

import java.io.Serializable;

public class TransactionResponse implements Serializable {
    private long id;
    private String categoryName;
    private String walletName;
    private long amount;
    private String type; // INCOME | EXPENSE
    private String note;
    private String transactionDate;

    public long getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getWalletName() {
        return walletName;
    }

    public long getAmount() {
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
}
