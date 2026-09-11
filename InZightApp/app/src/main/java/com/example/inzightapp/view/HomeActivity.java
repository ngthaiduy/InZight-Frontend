package com.example.inzightapp.view;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.example.inzightapp.R;
import com.example.inzightapp.utils.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup bottom navigation với indicator
        BottomNavHelper.setupBottomNavWithIndicator(bottomNavigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // load Home
                return true;
            } else if (id == R.id.nav_social) {
                // load Social
                return true;
            } else if (id == R.id.nav_transaction) {
                // load Finance
                return true;
            } else if (id == R.id.nav_message) {
                // load Message
                return true;
            } else if (id == R.id.nav_setting) {
                // load Setting
                return true;
            }
            // Cập nhật indicator sau khi chọn item
            bottomNavigation.post(() -> BottomNavHelper.updateIndicators(bottomNavigation));
            return false;
        });
    }
}
