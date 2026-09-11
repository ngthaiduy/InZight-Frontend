package com.example.inzightapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.auth.AuthApiService;
import com.example.inzightapp.model.request.RegisterRequest;
import com.example.inzightapp.model.response.InitRegisterResponse;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class CreatePasswordActivity extends AppCompatActivity {

    private TextInputEditText edtPassword, edtConfirmPassword;
    private Button btnSend;
    private AuthApiService authApiService;

    private String fullName, contact, dateOfBirth, gender, username;

    private static final String TAG = "CreatePassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSend = findViewById(R.id.btnSend);

        fullName = getIntent().getStringExtra("fullName");
        contact = getIntent().getStringExtra("contact");
        dateOfBirth = getIntent().getStringExtra("dateOfBirth");
        gender = getIntent().getStringExtra("gender");
        username = getIntent().getStringExtra("username");

        authApiService = ApiClient.getClient(this).create(AuthApiService.class);

        btnSend.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(this, "Please fill password fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build RegisterRequest - contact đã được validate là email hợp lệ ở CreateAccountActivity
            RegisterRequest req = new RegisterRequest(
                    username,
                    contact, // contact đã là email hợp lệ
                    fullName,
                    dateOfBirth,
                    gender,
                    password
            );

            // Call API
            Call<InitRegisterResponse> call = authApiService.initRegister(req);
            call.enqueue(new Callback<InitRegisterResponse>() {
                @Override
                public void onResponse(Call<InitRegisterResponse> call, Response<InitRegisterResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String registrationToken = response.body().getRegistrationToken();

                        // Start VerifyOtpActivity with token and credentials (so we can auto-login after verify)
                        Intent i = new Intent(CreatePasswordActivity.this, VerifyOtpActivity.class);
                        i.putExtra("registrationToken", registrationToken);
                        i.putExtra("username", username);
                        i.putExtra("password", password); // keep locally for auto-login
                        startActivity(i);
                        finish();
                    } else {
                        // Đọc error message từ response body
                        String errorMessage = "Đăng ký thất bại";
                        try {
                            ResponseBody errorBody = response.errorBody();
                            if (errorBody != null) {
                                errorMessage = errorBody.string();
                                Log.e(TAG, "Error response: " + errorMessage);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                        
                        Toast.makeText(CreatePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Init register failed code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<InitRegisterResponse> call, Throwable t) {
                    Toast.makeText(CreatePasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        });
    }

    private boolean isEmail(String s) {
        if (s == null) return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();
    }
}
