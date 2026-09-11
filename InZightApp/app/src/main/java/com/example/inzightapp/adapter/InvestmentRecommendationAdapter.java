package com.example.inzightapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.model.response.ScenarioRecommendationResponse;
import com.example.inzightapp.utils.VietnameseCurrencyFormatter;

import java.util.List;

public class InvestmentRecommendationAdapter extends RecyclerView.Adapter<InvestmentRecommendationAdapter.ViewHolder> {
    
    private List<ScenarioRecommendationResponse.InvestmentRecommendation> items;
    
    public InvestmentRecommendationAdapter(List<ScenarioRecommendationResponse.InvestmentRecommendation> items) {
        this.items = items != null ? items : new java.util.ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investment_recommendation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScenarioRecommendationResponse.InvestmentRecommendation item = items.get(position);
        
        holder.tvTitle.setText(item.title);
        
        // Set icon based on icon name
        int iconRes = getIconResource(item.icon);
        if (iconRes != 0) {
            holder.ivIcon.setImageResource(iconRes);
            holder.ivIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivIcon.setVisibility(View.GONE);
        }
        
        // Show amount if available
        if (item.suggestedAmount > 0) {
            holder.tvAmount.setText(VietnameseCurrencyFormatter.format(item.suggestedAmount));
            holder.tvAmount.setVisibility(View.VISIBLE);
        } else {
            holder.tvAmount.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<ScenarioRecommendationResponse.InvestmentRecommendation> newItems) {
        this.items = newItems != null ? newItems : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }
    
    private int getIconResource(String iconName) {
        if (iconName == null) return 0;
        
        switch (iconName) {
            case "ic_finance":
                return R.drawable.ic_finance;
            case "ic_add":
                return R.drawable.ic_add;
            case "ic_wallet":
                return R.drawable.ic_wallet;
            case "ic_finbot":
            case "imagebot":
                return R.drawable.imagebot;
            default:
                return 0;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAmount;
        ImageView ivIcon;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvInvestmentTitle);
            tvAmount = itemView.findViewById(R.id.tvInvestmentAmount);
            ivIcon = itemView.findViewById(R.id.ivInvestmentIcon);
        }
    }
}

