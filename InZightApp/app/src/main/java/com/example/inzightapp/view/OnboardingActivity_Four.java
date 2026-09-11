package com.example.inzightapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.R;

public class OnboardingActivity_Four extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboardingpage_four);

        Button btnGetStarted = findViewById(R.id.btnContinue);

        btnGetStarted.setOnClickListener(v -> {
            // Sau khi bấm, chuyển sang OnboardingActivity_Three
            Intent intent = new Intent(OnboardingActivity_Four.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
