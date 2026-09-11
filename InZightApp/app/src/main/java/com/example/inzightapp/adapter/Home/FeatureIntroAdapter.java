package com.example.inzightapp.adapter.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;

import java.util.ArrayList;
import java.util.List;

public class FeatureIntroAdapter extends RecyclerView.Adapter<FeatureIntroAdapter.ViewHolder> {

    private final List<FeatureItem> features;
    private OnItemClickListener onItemClickListener;

    public FeatureIntroAdapter() {
        this.features = new ArrayList<>();
        // Thêm các chức năng
        // Lưu ý: Thêm hình background vào drawable folder và thay đổi backgroundRes
        features.add(new FeatureItem(
                R.drawable.ic_multi_goal_bx,
                "Multi-Goal Planning",
                "",
                R.mipmap.banner_multi_goal_foreground // Thay bằng hình của bạn: R.drawable.banner_multi_goal
        ));
        features.add(new FeatureItem(
                R.drawable.ic_optimizer_bx,
                "Optimizer",
                "",
                R.mipmap.banner_optimizer_foreground // Thay bằng hình của bạn: R.drawable.banner_optimizer
        ));
        features.add(new FeatureItem(
                R.drawable.ic_scenario_bx,
                "Scenario Analysis",
                "",
                R.mipmap.banner_scenario_foreground // Thay bằng hình của bạn: R.drawable.banner_scenario
        ));
        features.add(new FeatureItem(
                R.drawable.ic_retire_bx,
                "Retirement Calculator",
                "",
                R.mipmap.banner_retire_foreground// Thay bằng hình của bạn: R.drawable.banner_retire
        ));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feature_intro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeatureItem item = features.get(position);
        holder.imgIcon.setImageResource(item.getIconRes());
        holder.imgBackground.setImageResource(item.getBackgroundRes());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        
        // Thêm click listener cho toàn bộ item view
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        ImageView imgBackground;
        TextView tvTitle;
        TextView tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgFeatureIcon);
            imgBackground = itemView.findViewById(R.id.imgFeatureBackground);
            tvTitle = itemView.findViewById(R.id.tvFeatureTitle);
            tvDescription = itemView.findViewById(R.id.tvFeatureDescription);
        }
    }

    // Model class cho feature item
    public static class FeatureItem {
        private final int iconRes;
        private final String title;
        private final String description;
        private final int backgroundRes;

        public FeatureItem(int iconRes, String title, String description, int backgroundRes) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
            this.backgroundRes = backgroundRes;
        }

        public int getIconRes() {
            return iconRes;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getBackgroundRes() {
            return backgroundRes;
        }
    }
}

