package com.example.inzightapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

import com.example.inzightapp.R;

/**
 * Helper class để hiển thị loading dialog (vòng tròn chờ đợi) cho toàn hệ thống
 */
public class LoadingDialog {
    private Dialog dialog;
    private Context context;

    public LoadingDialog(Context context) {
        this.context = context;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.0f); // Không làm mờ background
        }
        
        // Không cho phép dismiss khi click bên ngoài
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Hiển thị loading dialog
     */
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            try {
                dialog.show();
            } catch (Exception e) {
                // Ignore nếu context đã bị destroy
            }
        }
    }

    /**
     * Ẩn loading dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                // Ignore nếu context đã bị destroy
            }
        }
    }

    /**
     * Kiểm tra xem dialog có đang hiển thị không
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}

