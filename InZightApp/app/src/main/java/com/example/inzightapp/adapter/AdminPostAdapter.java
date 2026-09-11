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
import com.example.inzightapp.model.response.PostResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.PostViewHolder> {

    private List<PostResponse> list = new ArrayList<>();
    private final OnPostAction listener;

    public interface OnPostAction {
        void onDelete(PostResponse post);
    }

    public AdminPostAdapter(OnPostAction listener) {
        this.listener = listener;
    }

    public void setPosts(List<PostResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostResponse item = list.get(position);

        // 1. Tên người đăng
        String name = item.getFullName() != null ? item.getFullName() : item.getUsername();
        holder.tvAuthor.setText(name);

        // 2. Nội dung bài viết
        holder.tvContent.setText(item.getContent());

        // 3. Avatar User
        Glide.with(holder.itemView.getContext())
                .load(item.getAvatarUrl())
                .placeholder(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(holder.imgAvatar);

        // 4. Ảnh bài viết (Ẩn nếu không có ảnh)
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.imgPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .into(holder.imgPostImage);
        } else {
            holder.imgPostImage.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvAuthor;
        ImageView imgPostImage, imgAvatar;
        ImageButton btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID trong file item_admin_post.xml khớp với dưới đây:
            tvContent = itemView.findViewById(R.id.tvPostTitle); // Reuse ID cũ hoặc đổi thành tvPostContent
            tvAuthor = itemView.findViewById(R.id.tvPostAuthor);
            imgPostImage = itemView.findViewById(R.id.imgPostThumbnail);
            imgAvatar = itemView.findViewById(R.id.imgAuthorAvatar); // ID mới cho Avatar
            btnDelete = itemView.findViewById(R.id.btnDeletePost);
        }
    }
}