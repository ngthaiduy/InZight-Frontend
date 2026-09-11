package com.example.inzightapp.adapter.payment;

// PaymentConfirmBottomSheet.java
import android.app.Dialog;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.inzightapp.R;
import com.example.inzightapp.databinding.BottomsheetPaymentConfirmBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PaymentConfirmBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PLAN_NAME = "arg_plan_name";
    private static final String ARG_PLAN_PRICE = "arg_plan_price";
    private static final String ARG_PLAN_BILLING = "arg_plan_billing";

    private BottomsheetPaymentConfirmBinding binding;
    private OnPaymentConfirmListener paymentConfirmListener;

    public interface OnPaymentConfirmListener {
        void onPaymentConfirmed();
    }

    public void setPaymentConfirmListener(OnPaymentConfirmListener listener) {
        this.paymentConfirmListener = listener;
    }

    public static PaymentConfirmBottomSheet newInstance(@NonNull String planName,
                                                        @NonNull String planPrice,
                                                        @NonNull String billingInfo) {
        PaymentConfirmBottomSheet sheet = new PaymentConfirmBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PLAN_NAME, planName);
        args.putString(ARG_PLAN_PRICE, planPrice);
        args.putString(ARG_PLAN_BILLING, billingInfo);
        sheet.setArguments(args);
        return sheet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.getBehavior().setFitToContents(true);
        dialog.getBehavior().setDraggable(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomsheetPaymentConfirmBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String planName = args != null ? args.getString(ARG_PLAN_NAME, "") : "";
        String planPrice = args != null ? args.getString(ARG_PLAN_PRICE, "") : "";
        String billingInfo = args != null ? args.getString(ARG_PLAN_BILLING, "") : "";

        binding.tvPlanName.setText(planName);
        binding.tvPlanPrice.setText(planPrice);
        binding.tvPlanBillingInfo.setText(billingInfo);

        // Áp dụng gradient cho text giá tiền
        binding.tvPlanPrice.post(() -> {
            int startColor = ContextCompat.getColor(requireContext(), R.color.gradientStart);
            int endColor = ContextCompat.getColor(requireContext(), R.color.gradientEnd);
            Shader shader = new LinearGradient(
                    0, 0,
                    binding.tvPlanPrice.getPaint().measureText(planPrice), 
                    binding.tvPlanPrice.getTextSize(),
                    startColor,
                    endColor,
                    Shader.TileMode.CLAMP
            );
            binding.tvPlanPrice.getPaint().setShader(shader);
            binding.tvPlanPrice.invalidate();
        });

        binding.btnConfirm.setOnClickListener(v -> {
            if (paymentConfirmListener != null) {
                paymentConfirmListener.onPaymentConfirmed();
            }
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.rootContainer.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slide_up)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}