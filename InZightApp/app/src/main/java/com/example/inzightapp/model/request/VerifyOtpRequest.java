package com.example.inzightapp.model.request;

public class VerifyOtpRequest {
    private String registrationToken;
    private String otp;

    public VerifyOtpRequest(String registrationToken, String otp) {
        this.registrationToken = registrationToken;
        this.otp = otp;
    }

    public String getRegistrationToken() { return registrationToken; }
    public String getOtp() { return otp; }
}
