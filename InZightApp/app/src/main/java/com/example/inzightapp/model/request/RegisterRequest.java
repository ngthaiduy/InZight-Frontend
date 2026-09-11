package com.example.inzightapp.model.request;

public class RegisterRequest {
    private String username;
    private String email;

    private String fullName;
    private String dateOfBirth; // yyyy-MM-dd
    private String gender;
    private String password;

    public RegisterRequest(String username, String email, String fullName, String dateOfBirth, String gender, String password) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.password = password;
    }

    // getters + setters (generate if cần)
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public String getFullName() { return fullName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getPassword() { return password; }
}
