package com.example.inzightapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.MainActivity;
import com.example.inzightapp.R;

public class OnboardingActivity_One extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra xem user đã đăng nhập chưa (có token không)
        if (checkIfUserLoggedIn()) {
            // Nếu đã đăng nhập, chuyển thẳng đến MainActivity
            Intent intent = new Intent(OnboardingActivity_One.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        // Nếu chưa đăng nhập, hiển thị onboarding
        setContentView(R.layout.activity_onboardingpage_one);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v -> {
            // Sau khi bấm, chuyển sang OnboardingActivity_Two
            Intent intent = new Intent(OnboardingActivity_One.this, OnboardingActivity_Two.class);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Kiểm tra xem user đã đăng nhập chưa bằng cách check JWT token
     */
    private boolean checkIfUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        // Nếu có token và không rỗng, user đã đăng nhập
        return token != null && !token.isEmpty();
    }
}
