package com.example.inzightapp.adapter.Category;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.model.response.CategoryResponse;

import java.util.List;

public class CategoryCompactAdapter extends RecyclerView.Adapter<CategoryCompactAdapter.ViewHolder> {

    private final List<CategoryResponse> categories;
    private final OnCategoryClickListener listener;
    private final OnShowAllListener showAllListener;
    private int selectedPosition = -1;

    // Listener for category item click
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryResponse category);
    }

    // Listener for "Show All" button
    public interface OnShowAllListener {
        void onShowAllCategories();
    }

    public CategoryCompactAdapter(List<CategoryResponse> categories,
                                  OnCategoryClickListener listener,
                                  OnShowAllListener showAllListener) {
        this.categories = categories;
        this.listener = listener;
        this.showAllListener = showAllListener;
    }

    public void setSelectedCategory(CategoryResponse category) {
        // Update the selected item after choosing a category from the popup
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == category.getId()) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_compact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position < categories.size()) {
            CategoryResponse cat = categories.get(position);
            holder.tvCategoryName.setText(cat.getName());

            // Use hardcoded icons based on category name
            holder.imgCategoryIcon.setImageResource(
                    com.example.inzightapp.utils.CategoryUtils.getIconForCategory(cat.getName())
            );

            // Highlight selected category
            if (position == selectedPosition) {
                holder.itemView.setBackgroundResource(R.drawable.bg_category_compact_selected);
                holder.tvCategoryName.setTextColor(Color.parseColor("#0052CC"));
            } else {
                holder.itemView.setBackgroundResource(R.drawable.bg_category_compact_unselected);
                holder.tvCategoryName.setTextColor(Color.BLACK);
            }

            holder.itemView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(cat);
            });

        } else {
            // “Add” button at the end
            holder.tvCategoryName.setText("Add");
            holder.imgCategoryIcon.setImageResource(R.drawable.ic_add);
            holder.itemView.setBackgroundResource(R.drawable.bg_category_compact_unselected);
            holder.tvCategoryName.setTextColor(Color.BLACK);

            holder.itemView.setOnClickListener(v -> showAllListener.onShowAllCategories());
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1; // Add one extra item for the “Add” button
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryIcon;
        TextView tvCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
