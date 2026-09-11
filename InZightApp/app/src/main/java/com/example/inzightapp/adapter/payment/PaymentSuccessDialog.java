package com.example.inzightapp.adapter.payment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.inzightapp.R;
import com.google.android.material.button.MaterialButton;

public class PaymentSuccessDialog extends DialogFragment {

    public static PaymentSuccessDialog newInstance() {
        return new PaymentSuccessDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getContext() == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }

        MaterialButton btnGotIt = dialog.findViewById(R.id.btnGotIt);
        View iconView = dialog.findViewById(R.id.ivSuccessIcon);
        
        if (btnGotIt != null) {
            btnGotIt.setOnClickListener(v -> dismiss());
        }

        // Animation khi hiển thị dialog - áp dụng cho icon
        dialog.setOnShowListener(dialogInterface -> {
            if (iconView != null && getContext() != null) {
                iconView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_bounce));
            }
        });

        return dialog;
    }
}

