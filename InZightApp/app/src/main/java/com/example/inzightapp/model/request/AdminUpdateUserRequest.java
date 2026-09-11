package com.example.inzightapp.model.request;

public class AdminUpdateUserRequest {
    private String fullName;
    private String role;
    // Thêm field khác nếu cần update (phone, gender...)

    public AdminUpdateUserRequest(String fullName, String role) {
        this.fullName = fullName;
        this.role = role;
    }
}