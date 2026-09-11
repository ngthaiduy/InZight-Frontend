package com.example.inzightapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.model.response.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    private List<CategoryResponse> list = new ArrayList<>();
    private final OnCategoryAction listener;

    public interface OnCategoryAction {
        void onDelete(CategoryResponse category);
    }

    public AdminCategoryAdapter(OnCategoryAction listener) {
        this.listener = listener;
    }

    public void setCategories(List<CategoryResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryResponse item = list.get(position);

        holder.tvName.setText(item.getName());

        // Hiển thị loại (nếu có)
        if (item.getType() != null) {
            holder.tvType.setText(item.getType());
            holder.tvType.setVisibility(View.VISIBLE);
        } else {
            holder.tvType.setVisibility(View.GONE);
        }

        // Load Icon
        if (item.getIconUrl() != null && !item.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getIconUrl())
                    .placeholder(R.drawable.bg_role_badge) // Hình mặc định
                    .into(holder.imgIcon);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType;
        ImageView imgIcon; // Biến này không được null
        ImageButton btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);

            // Nếu bạn thêm tvCategoryType vào XML thì find nó, nếu không thì bỏ qua
            tvType = itemView.findViewById(R.id.tvCategoryType);

            // DÒNG NÀY RẤT QUAN TRỌNG: Phải khớp ID với XML
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);

            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}