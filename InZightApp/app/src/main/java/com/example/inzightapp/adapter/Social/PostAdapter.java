package com.example.inzightapp.adapter.Social;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.CommentApiService;
import com.example.inzightapp.api.social.LikeApiService;
import com.example.inzightapp.api.social.PostApiService;
import com.example.inzightapp.api.social.ShareApiService;
import com.example.inzightapp.eventInput.PostMenuBottomSheet;
import com.example.inzightapp.model.response.PostResponse;
import com.example.inzightapp.view.CommentActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final String currentUsername;
    private List<PostResponse> postList = new ArrayList<>();

    public PostAdapter(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        this.currentUsername = prefs.getString("username", "");
    }

    public void setPostList(List<PostResponse> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PostResponse post = postList.get(position);

        // --- API service setup
        LikeApiService likeApi = ApiClient.getClient(context).create(LikeApiService.class);
        ShareApiService shareApi = ApiClient.getClient(context).create(ShareApiService.class);
        CommentApiService commentApi = ApiClient.getClient(context).create(CommentApiService.class);

        // --- Hiển thị thông tin bài viết
        holder.tvUsername.setText(post.getFullName());
        holder.tvCaption.setText(post.getContent());
        holder.tvLikes.setText(post.getLikeCount() + " likes");
        holder.tvComments.setText("View all " + post.getComments().size() + " comments");

        // Load post image - xử lý cả URI và URL
        String imageUrl = post.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Kiểm tra xem là URI content:// hay URL HTTP/HTTPS
                if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                    // Đây là URI local - có thể không có quyền truy cập
                    // URI từ Google Photos picker thường không có quyền truy cập lâu dài
                    // Nên bỏ qua và hiển thị placeholder
                    Log.w("PostAdapter", "Skipping local URI (may not have permission): " + imageUrl);
                    holder.imgPost.setImageResource(R.drawable.bg_placeholder);
                } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    // Đây là URL HTTP/HTTPS - load bình thường
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.bg_placeholder)
                            .error(R.drawable.bg_placeholder)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    Log.e("PostAdapter", "Failed to load image: " + imageUrl, e);
                                    return false; // Let Glide handle the error (show error placeholder)
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(holder.imgPost);
                } else {
                    // URL không hợp lệ - hiển thị placeholder
                    Log.w("PostAdapter", "Invalid image URL format: " + imageUrl);
                    holder.imgPost.setImageResource(R.drawable.bg_placeholder);
                }
            } catch (Exception e) {
                Log.e("PostAdapter", "Error loading post image: " + imageUrl, e);
                holder.imgPost.setImageResource(R.drawable.bg_placeholder);
            }
        } else {
            // Không có ảnh - ẩn ImageView hoặc hiển thị placeholder
            holder.imgPost.setImageResource(R.drawable.bg_placeholder);
        }

        // Load avatar
        String avatarUrl = post.getAvatarUrl() != null ? post.getAvatarUrl()
                : "https://i.pravatar.cc/150?u=" + post.getUserId();
        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(holder.imgAvatar);

        // --- Nút 3 chấm mở BottomSheet
        holder.btnMenu.setOnClickListener(v -> {
            boolean isOwner = post.getUsername().equals(currentUsername);

            PostMenuBottomSheet sheet = new PostMenuBottomSheet(isOwner, new PostMenuBottomSheet.PostMenuListener() {
                @Override
                public void onHidePost() {
                    hidePost(position);
                }

                @Override
                public void onDeletePost() {
                    showDeleteConfirmation(post.getId(), position);
                }
            });

            sheet.show(((FragmentActivity) context).getSupportFragmentManager(), "PostMenu");
        });

        // --- Like animation + API
        holder.btnLike.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(context, R.anim.scale_bounce);
            likeApi.toggleLike(post.getId()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        boolean isLiked = post.isLiked();
                        post.setLiked(!isLiked);

                        int count = post.getLikeCount();
                        post.setLikeCount(isLiked ? Math.max(count - 1, 0) : count + 1);
                        holder.tvLikes.setText(post.getLikeCount() + " likes");

                        holder.btnLike.setImageResource(post.isLiked()
                                ? R.drawable.ic_heart_filled
                                : R.drawable.ic_heart_outline);
                        holder.btnLike.setColorFilter(ContextCompat.getColor(context,
                                post.isLiked() ? R.color.red : R.color.black));

                        holder.btnLike.startAnimation(bounce);
                    } else {
                        Toast.makeText(context, "Unable to like this post", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // --- Comment
        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });

        // --- Share
        holder.btnShare.setOnClickListener(v ->
                shareApi.sharePost(post.getId()).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Unable to share this post", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );

        // --- Cập nhật nút like trạng thái ban đầu
        holder.btnLike.setImageResource(post.isLiked()
                ? R.drawable.ic_heart_filled
                : R.drawable.ic_heart_outline);
        holder.btnLike.setColorFilter(ContextCompat.getColor(context,
                post.isLiked() ? R.color.red : R.color.black));
    }

    // --- Ẩn bài viết
    private void hidePost(int position) {
        postList.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Post hidden", Toast.LENGTH_SHORT).show();
    }

    // --- Xác nhận & xóa bài viết
    private void showDeleteConfirmation(Long postId, int position) {
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        com.google.android.material.button.MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnDelete = dialog.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            deletePost(postId, position);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


    private void deletePost(Long postId, int position) {
        PostApiService api = ApiClient.getClient(context).create(PostApiService.class);
        api.deletePost(postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    postList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgPost;
        TextView tvUsername, tvCaption, tvLikes, tvComments;
        ImageButton btnLike, btnComment, btnShare, btnBookmark, btnMenu;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPost = itemView.findViewById(R.id.imgPost);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
