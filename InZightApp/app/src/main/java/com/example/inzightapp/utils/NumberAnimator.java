package com.example.inzightapp.utils;

import android.animation.ValueAnimator;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberAnimator {
    
    public static void animateNumber(TextView textView, double targetValue, String suffix) {
        animateNumber(textView, 0, targetValue, suffix, 1000);
    }
    
    public static void animateNumber(TextView textView, double startValue, double targetValue, String suffix, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) startValue, (float) targetValue);
        animator.setDuration(duration);
        
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            String formatted = formatter.format(animatedValue);
            textView.setText(formatted + (suffix != null ? suffix : ""));
        });
        
        animator.start();
    }
    
    public static void animateCurrency(TextView textView, double targetValue) {
        animateNumber(textView, 0, targetValue, " VND", 1000);
    }
    
    public static void animatePercentage(TextView textView, double targetValue) {
        animateNumber(textView, 0, targetValue, "%", 1000);
    }
}

