package com.example.inzightapp.model.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("username")
    private String username;

    public String getToken() { return token; }
    public String getUsername() { return username; }
}
