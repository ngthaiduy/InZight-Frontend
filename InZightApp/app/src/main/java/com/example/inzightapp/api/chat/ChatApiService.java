package com.example.inzightapp.api.chat;

import com.example.inzightapp.model.request.ChatMessageRequest;
import com.example.inzightapp.model.response.ChatMessageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {

    @GET("chats/history/{receiverId}")
    Call<List<ChatMessageResponse>> getHistory(@Path("receiverId") Long receiverId);

    @POST("chats/send")
    Call<ChatMessageResponse> sendMessage(@Body ChatMessageRequest request);

    @POST("chats/ai")
    Call<ChatMessageResponse> chatWithAi(@Body ChatMessageRequest request);
}
