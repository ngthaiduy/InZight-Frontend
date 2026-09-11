package com.example.inzightapp.model.response;

import java.math.BigDecimal;

public class WalletResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
    private String currency;

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setCurrency(String currency) { this.currency = currency; }

    @Override
    public String toString() { return name; }
}
