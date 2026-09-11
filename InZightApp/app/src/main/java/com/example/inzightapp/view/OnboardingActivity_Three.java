package com.example.inzightapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.R;

public class OnboardingActivity_Three extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboardingpage_three);

        Button btnGetStarted = findViewById(R.id.btnContinue);

        btnGetStarted.setOnClickListener(v -> {
            // Sau khi bấm, chuyển sang OnboardingActivity_Four
            Intent intent = new Intent(OnboardingActivity_Three.this, OnboardingActivity_Four.class);
            startActivity(intent);
            finish();
        });
    }
}