package com.example.inzightapp.adapter;

import android.graphics.Color;
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
import com.example.inzightapp.model.response.UserResponse;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<UserResponse> userList = new ArrayList<>();
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEdit(UserResponse user);
        void onDelete(UserResponse user);
    }

    public AdminUserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserResponse> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponse user = userList.get(position);

        // ... set text ...

        // Logic đổi màu badge
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            holder.tvRole.setText("ADMIN");
            holder.tvRole.setTextColor(Color.parseColor("#1E88E5")); // Blue
            holder.tvRole.setBackgroundResource(R.drawable.bg_role_badge); // Nền xanh nhạt
        } else {
            holder.tvRole.setText("USER");
            holder.tvRole.setTextColor(Color.parseColor("#757575")); // Grey
            // Có thể tạo thêm drawable nền xám nếu muốn
            holder.tvRole.setBackgroundResource(R.drawable.bg_role_badge);
        }

        // Load ảnh avatar (dùng Glide hoặc Picasso)
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.mipmap.ic_launcher) // Ảnh mặc định
                    .circleCrop() // Bo tròn ảnh
                    .into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgAvatar;
        TextView tvUsername, tvEmail, tvRole;
        ImageButton btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}