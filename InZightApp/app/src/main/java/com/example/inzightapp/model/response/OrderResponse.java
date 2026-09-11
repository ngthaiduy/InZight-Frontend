package com.example.inzightapp.model.response;

public class OrderResponse {
    private Long id;
    private String orderDate;
    private String userEmail;
    private Double amount;
    private String status;

    public OrderResponse(Long id, String orderDate, String userEmail, Double amount, String status) {
        this.id = id;
        this.orderDate = orderDate;
        this.userEmail = userEmail;
        this.amount = amount;
        this.status = status;
    }
    // Getters...
    public Long getId() { return id; }
    public String getOrderDate() { return orderDate; }
    public String getUserEmail() { return userEmail; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
}