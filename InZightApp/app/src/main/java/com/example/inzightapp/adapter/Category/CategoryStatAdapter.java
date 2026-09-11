package com.example.inzightapp.adapter.Category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.model.response.CategoryStatistic;
import java.text.DecimalFormat;
import java.util.List;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private final List<CategoryStatistic> list;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public CategoryStatAdapter(List<CategoryStatistic> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStatistic item = list.get(position);

        // Gán icon drawable dựa vào tên
        int iconRes = getIconForCategory(item.getCategoryName());
        holder.imgIcon.setImageResource(iconRes);

        holder.tvName.setText(item.getCategoryName());
        String formatted = df.format(item.getAmount());
        holder.tvAmount.setText(formatted);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvCategoryAmount);
        }
    }

    private int getIconForCategory(String name) {
        return com.example.inzightapp.utils.CategoryUtils.getIconForCategory(name);
    }
}
