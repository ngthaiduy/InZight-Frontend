package com.example.inzightapp.adapter.Social;

import static java.time.LocalDateTime.parse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.CommentLikeApiService;
import com.example.inzightapp.model.response.CommentResponse;
import com.example.inzightapp.utils.TimeUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private List<CommentResponse> commentList;

    public CommentAdapter(Context context, List<CommentResponse> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentResponse comment = commentList.get(position);

        holder.tvUsername.setText(comment.getUsername());
        holder.tvContent.setText(comment.getContent());
        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));

        // Display correct like icon
        if (comment.isLiked()) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
            holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
            holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.black));
        }

        // Format and show comment time
        holder.tvTime.setText(TimeUtils.formatTime(parse(comment.getCreatedAt())));

        // Like button click
        holder.btnLike.setOnClickListener(v -> {
            CommentLikeApiService api = ApiClient.getClient(context).create(CommentLikeApiService.class);
            Animation bounce = AnimationUtils.loadAnimation(context, R.anim.scale_bounce);

            api.toggleLike(comment.getId()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        boolean isLiked = comment.isLiked();
                        comment.setLiked(!isLiked);

                        int count = comment.getLikeCount();
                        if (!isLiked) {
                            comment.setLikeCount(count + 1);
                            holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                            holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
                        } else {
                            comment.setLikeCount(Math.max(count - 1, 0));
                            holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
                            holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.black));
                        }

                        // Play bounce animation
                        holder.btnLike.startAnimation(bounce);
                        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
                    } else {
                        Toast.makeText(context, "Unable to like this comment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Load avatar
        Glide.with(context)
                .load(comment.getAvatarUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public void updateData(List<CommentResponse> newList) {
        this.commentList = newList;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnLike;
        TextView tvUsername, tvContent, tvTime, tvLikeCount;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }
    }
}
