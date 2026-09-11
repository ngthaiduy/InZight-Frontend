package com.example.inzightapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.model.response.ScenarioRecommendationResponse;

import java.util.List;

public class JobRecommendationAdapter extends RecyclerView.Adapter<JobRecommendationAdapter.ViewHolder> {
    
    private List<ScenarioRecommendationResponse.JobRecommendation> items;
    
    public JobRecommendationAdapter(List<ScenarioRecommendationResponse.JobRecommendation> items) {
        this.items = items != null ? items : new java.util.ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_recommendation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScenarioRecommendationResponse.JobRecommendation item = items.get(position);
        
        holder.tvJobTitle.setText(item.title);
        holder.tvEarning.setText(item.estimatedEarning);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<ScenarioRecommendationResponse.JobRecommendation> newItems) {
        this.items = newItems != null ? newItems : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle;
        TextView tvEarning;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvEarning = itemView.findViewById(R.id.tvJobEarning);
        }
    }
}

