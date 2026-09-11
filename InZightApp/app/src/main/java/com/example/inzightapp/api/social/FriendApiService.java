package com.example.inzightapp.api.social;

import com.example.inzightapp.model.response.FriendResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface FriendApiService {
    @GET("/friends")
    Call<List<FriendResponse>> getAllFriends();

    @POST("/friends/invite/{receiverId}")
    Call<String> inviteFriend(@Path("receiverId") Long receiverId);

    @POST("/friends/{senderId}/accept")
    Call<String> acceptRequest(@Path("senderId") Long senderId);

    @POST("/friends/{friendId}/block")
    Call<String> blockFriend(@Path("friendId") Long friendId);

    @POST("/friends/{friendId}/unblock")
    Call<String> unblockFriend(@Path("friendId") Long friendId);

    @DELETE("/friends/{friendId}")
    Call<String> removeFriend(@Path("friendId") Long friendId);
}
