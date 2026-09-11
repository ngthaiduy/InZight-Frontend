package com.example.inzightapp.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Custom view to display an animated arrow pointing to a target view
 */
public class TutorialArrowView extends View {
    
    private Paint arrowPaint;
    private Paint circlePaint;
    private Path arrowPath;
    private float arrowX, arrowY;
    private float targetX, targetY;
    private float targetWidth, targetHeight;
    private float circleRadius = 60f;
    private ObjectAnimator pulseAnimator;
    private ObjectAnimator arrowAnimator;
    
    public TutorialArrowView(Context context) {
        super(context);
        init();
    }
    
    public TutorialArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TutorialArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Arrow paint
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.parseColor("#2F6BFF"));
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setStrokeWidth(8f);
        
        // Circle paint (highlight around target)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#402F6BFF")); // Semi-transparent blue
        circlePaint.setStyle(Paint.Style.FILL);
        
        arrowPath = new Path();
        
        // Set background to transparent
        setBackgroundColor(Color.TRANSPARENT);
    }
    
    /**
     * Point arrow to a target view
     */
    public void pointTo(View targetView) {
        if (targetView == null) return;
        
        // Get target view location relative to screen
        int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        
        // Get parent location to convert screen coordinates to view coordinates
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            int[] parentLocation = new int[2];
            parent.getLocationOnScreen(parentLocation);
            
            targetX = location[0] - parentLocation[0] + targetView.getWidth() / 2f;
            targetY = location[1] - parentLocation[1] + targetView.getHeight() / 2f;
            targetWidth = targetView.getWidth();
            targetHeight = targetView.getHeight();
        } else {
            // Fallback: use screen coordinates
            targetX = location[0] + targetView.getWidth() / 2f;
            targetY = location[1] + targetView.getHeight() / 2f;
            targetWidth = targetView.getWidth();
            targetHeight = targetView.getHeight();
        }
        
        // Calculate arrow position (above the target)
        arrowX = targetX;
        arrowY = targetY - targetHeight / 2f - circleRadius - 80f;
        
        invalidate();
        startAnimation();
    }
    
    private void startAnimation() {
        // Pulse animation for circle
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        pulseAnimator = ObjectAnimator.ofFloat(this, "circleRadius", circleRadius, circleRadius + 15f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
        
        // Arrow bounce animation
        if (arrowAnimator != null) {
            arrowAnimator.cancel();
        }
        arrowAnimator = ObjectAnimator.ofFloat(this, "arrowOffset", 0f, 20f);
        arrowAnimator.setDuration(800);
        arrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arrowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        arrowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        arrowAnimator.start();
    }
    
    private float arrowOffset = 0f;
    
    public void setArrowOffset(float offset) {
        this.arrowOffset = offset;
        invalidate();
    }
    
    public void setCircleRadius(float radius) {
        this.circleRadius = radius;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (targetX == 0 && targetY == 0) return;
        
        // Draw highlight circle around target
        canvas.drawCircle(targetX, targetY, circleRadius, circlePaint);
        
        // Draw arrow pointing to target
        float currentArrowY = arrowY - arrowOffset;
        drawArrow(canvas, arrowX, currentArrowY, targetX, targetY - targetHeight / 2f - circleRadius);
    }
    
    private void drawArrow(Canvas canvas, float x1, float y1, float x2, float y2) {
        arrowPath.reset();
        
        // Calculate arrow direction
        float dx = x2 - x1;
        float dy = y2 - y1;
        float angle = (float) Math.atan2(dy, dx);
        
        // Arrow head size
        float arrowHeadLength = 30f;
        float arrowHeadAngle = (float) Math.PI / 6; // 30 degrees
        
        // Draw arrow line
        arrowPath.moveTo(x1, y1);
        arrowPath.lineTo(x2 - arrowHeadLength * (float) Math.cos(angle), 
                        y2 - arrowHeadLength * (float) Math.sin(angle));
        
        // Draw arrow head
        float x3 = x2 - arrowHeadLength * (float) Math.cos(angle - arrowHeadAngle);
        float y3 = y2 - arrowHeadLength * (float) Math.sin(angle - arrowHeadAngle);
        float x4 = x2 - arrowHeadLength * (float) Math.cos(angle + arrowHeadAngle);
        float y4 = y2 - arrowHeadLength * (float) Math.sin(angle + arrowHeadAngle);
        
        arrowPath.moveTo(x2, y2);
        arrowPath.lineTo(x3, y3);
        arrowPath.lineTo(x4, y4);
        arrowPath.close();
        
        canvas.drawPath(arrowPath, arrowPaint);
    }
    
    public void stopAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (arrowAnimator != null) {
            arrowAnimator.cancel();
            arrowAnimator = null;
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}

