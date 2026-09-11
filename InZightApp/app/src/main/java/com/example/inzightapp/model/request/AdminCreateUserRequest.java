package com.example.inzightapp.model.request;

public class AdminCreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role; // "ADMIN" hoặc "USER"

    public AdminCreateUserRequest(String username, String email, String password, String fullName, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
}