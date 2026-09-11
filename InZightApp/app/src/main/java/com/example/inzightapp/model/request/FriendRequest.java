package com.example.inzightapp.model.request;

public class FriendRequest {
    private Long friendId;

    public FriendRequest(Long friendId) {
        this.friendId = friendId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
