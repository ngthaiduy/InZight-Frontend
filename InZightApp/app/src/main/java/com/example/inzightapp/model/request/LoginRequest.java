package com.example.inzightapp.model.request;

public class LoginRequest {
    private String contact;
    private String password;

    public LoginRequest(String contact, String password) {
        this.contact = contact;
        this.password = password;
    }
}

