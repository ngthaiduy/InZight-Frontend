package com.example.inzightapp.adapter.payment;

// PaymentPlanAdapter.java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.model.request.PaymentPlan;
import com.example.inzightapp.databinding.ItemPaymentPlanBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PaymentPlanAdapter extends RecyclerView.Adapter<PaymentPlanAdapter.PaymentPlanViewHolder> {

    public interface OnPlanSelectedListener {
        void onPlanSelected(@NonNull PaymentPlan plan);
    }

    private final List<PaymentPlan> plans;
    private final OnPlanSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public PaymentPlanAdapter(@NonNull List<PaymentPlan> plans,
                              @NonNull OnPlanSelectedListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentPlanViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPaymentPlanBinding binding = ItemPaymentPlanBinding.inflate(inflater, parent, false);
        return new PaymentPlanViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentPlanViewHolder holder, int position) {
        PaymentPlan plan = plans.get(position);
        holder.bind(plan, position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (oldPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPos);
            }
            notifyItemChanged(selectedPosition);
            listener.onPlanSelected(plan);
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public PaymentPlan getSelectedPlan() {
        if (selectedPosition >= 0 && selectedPosition < plans.size()) {
            return plans.get(selectedPosition);
        }
        return null;
    }

    static class PaymentPlanViewHolder extends RecyclerView.ViewHolder {

        private final ItemPaymentPlanBinding binding;

        public PaymentPlanViewHolder(@NonNull ItemPaymentPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull PaymentPlan plan, boolean selected) {
            Context context = binding.getRoot().getContext();

            binding.tvPlanName.setText(plan.getName());
            binding.tvPlanPrice.setText(plan.getPriceDisplay());
            binding.tvPlanBillingInfo.setText(plan.getBillingInfo());

            if (plan.isRecommended()) {
                binding.tvPlanTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlanTag.setVisibility(View.GONE);
            }

            MaterialCardView card = binding.cardPlan;

            if (selected) {
                card.setCardElevation(12f);
                card.setStrokeWidth(0);
                card.setUseCompatPadding(true);
                card.setForeground(null);
                card.setBackgroundResource(R.drawable.bg_plan_card_selected);

                Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.scale_selected);
                card.startAnimation(scaleAnim);

                binding.ivSelectedIndicator.setVisibility(View.VISIBLE);
            } else {
                card.setCardElevation(6f);
                card.setStrokeWidth(0);
                card.setBackgroundResource(R.drawable.bg_plan_card_normal);
                binding.ivSelectedIndicator.setVisibility(View.INVISIBLE);
            }

            int titleColor = selected
                    ? ContextCompat.getColor(context, android.R.color.white)
                    : ContextCompat.getColor(context, R.color.textPrimary);
            int secondaryColor = selected
                    ? ContextCompat.getColor(context, R.color.textSecondaryOnDark)
                    : ContextCompat.getColor(context, R.color.textSecondary);

            binding.tvPlanName.setTextColor(titleColor);
            binding.tvPlanPrice.setTextColor(titleColor);
            binding.tvPlanBillingInfo.setTextColor(secondaryColor);
        }
    }
}