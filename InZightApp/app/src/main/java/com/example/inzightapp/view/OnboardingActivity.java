package com.example.inzightapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.inzightapp.MainActivity;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.OnboardingAdapter;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPagerOnboarding;
    private LinearLayout indicatorContainer;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra xem user đã đăng nhập chưa (có token không)
        if (checkIfUserLoggedIn()) {
            // Nếu đã đăng nhập, chuyển thẳng đến MainActivity
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_onboarding);
        
        initViews();
        setupViewPager();
        setupIndicators();
        setupListeners();
    }
    
    private void initViews() {
        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
    }
    
    private void setupViewPager() {
        adapter = new OnboardingAdapter(this);
        viewPagerOnboarding.setAdapter(adapter);
        
        // Set page transformer for smooth animation
        viewPagerOnboarding.setPageTransformer(new DepthPageTransformer());
        
        // Listen to page changes
        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                updateButtons(position);
            }
        });
    }
    
    private void setupIndicators() {
        indicatorContainer.removeAllViews();
        int count = adapter.getItemCount();
        
        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            int size = 12;
            int margin = 8;
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_unselected);
            
            indicatorContainer.addView(indicator);
        }
        
        updateIndicators(0);
    }
    
    private void updateIndicators(int position) {
        int count = indicatorContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View indicator = indicatorContainer.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_selected);
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_unselected);
            }
        }
    }
    
    private void updateButtons(int position) {
        int lastPosition = adapter.getItemCount() - 1;
        
        if (position == lastPosition) {
            btnNext.setText("Get Started");
            btnSkip.setVisibility(View.GONE);
        } else {
            btnNext.setText("Next");
            btnSkip.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPagerOnboarding.getCurrentItem();
            int lastPosition = adapter.getItemCount() - 1;
            
            if (currentItem < lastPosition) {
                // Move to next page
                viewPagerOnboarding.setCurrentItem(currentItem + 1, true);
            } else {
                // Last page - go to LoginActivity
                navigateToLogin();
            }
        });
        
        btnSkip.setOnClickListener(v -> {
            // Skip to LoginActivity
            navigateToLogin();
        });
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
    
    /**
     * Page transformer for smooth slide animation
     */
    private static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;
        
        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();
            
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0f);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                page.setAlpha(1f);
                page.setTranslationX(0f);
                page.setScaleX(1f);
                page.setScaleY(1f);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                page.setAlpha(1 - position);
                
                // Counteract the default slide transition
                page.setTranslationX(pageWidth * -position);
                
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0f);
            }
        }
    }
}

