package com.example.inzightapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.inzightapp.R;
import com.google.android.material.button.MaterialButton;

/**
 * Overlay dialog for showing tutorial tooltips
 */
public class TutorialOverlay {
    
    private Dialog dialog;
    private Context context;
    private OnTutorialActionListener listener;
    
    public interface OnTutorialActionListener {
        void onNext();
        void onSkip();
        void onGotIt();
    }
    
    public TutorialOverlay(Context context) {
        this.context = context;
    }
    
    public void setListener(OnTutorialActionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Show tutorial overlay with message and highlight target view
     */
    public void show(String title, String message, String buttonText, View targetView) {
        if (context == null) return;
        
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tutorial_overlay);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000"))); // Semi-transparent black
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }
        
        TextView tvTitle = dialog.findViewById(R.id.tvTutorialTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvTutorialMessage);
        MaterialButton btnAction = dialog.findViewById(R.id.btnTutorialAction);
        MaterialButton btnSkip = dialog.findViewById(R.id.btnTutorialSkip);
        
        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);
        if (btnAction != null) {
            btnAction.setText(buttonText);
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNext();
                }
                dismiss();
            });
        }
        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSkip();
                }
                dismiss();
            });
        }
        
        // Position tooltip near target view if provided
        if (targetView != null && window != null) {
            int[] location = new int[2];
            targetView.getLocationOnScreen(location);
            
            // Calculate position for tooltip
            View tooltipContainer = dialog.findViewById(R.id.tooltipContainer);
            if (tooltipContainer != null) {
                // Position tooltip above or below target view
                WindowManager.LayoutParams params = window.getAttributes();
                params.y = location[1] - 200; // Position above target
                window.setAttributes(params);
            }
        }
        
        dialog.setCancelable(false);
        dialog.show();
    }
    
    /**
     * Show simple tutorial message
     */
    public void showSimple(String title, String message) {
        show(title, message, "Got it", null);
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}

