package com.example.inzightapp.model.response;

import java.util.List;

public class CommentResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private String content;
    private int likeCount;
    private boolean liked;



    // Thêm 2 trường thời gian
    private String createdAt;
    private String updatedAt;

    // Các reply con
    private List<ReplyResponse> replies;

    // Getter
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getContent() { return content; }
    public int getLikeCount() { return likeCount; }
    public CharSequence getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<ReplyResponse> getReplies() { return replies; }

    // Setter (nếu cần chỉnh từ client)
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setContent(String content) { this.content = content; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setReplies(List<ReplyResponse> replies) { this.replies = replies; }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean b) {
        this.liked = b;
    }
}
