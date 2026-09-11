package com.example.inzightapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.MessageAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.chat.ChatApiService;
import com.example.inzightapp.api.social.FriendApiService;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.example.inzightapp.model.response.FriendResponse;
import com.example.inzightapp.view.ChatActivity;
import com.example.inzightapp.view.FinbotChatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment {

    private RecyclerView recyclerViewMessages;
    private EditText etSearch;
    private MessageAdapter messageAdapter;
    private List<ChatMessageResponse> allMessages = new ArrayList<>();
    private ChatApiService chatApiService;
    private FriendApiService friendApiService;
    private Long currentUserId;
    private static final Long FINBOT_ID = -999L; // Special ID for Finbot

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        // Initialize views
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        etSearch = view.findViewById(R.id.etSearch);

        // Setup RecyclerView
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new MessageAdapter(getContext());
        messageAdapter.setOnMessageClickListener(message -> {
            // Check if it's Finbot
            if (message.getSenderId() != null && message.getSenderId().equals(FINBOT_ID)) {
                // Open Finbot chat activity
                Intent intent = new Intent(getContext(), FinbotChatActivity.class);
                startActivity(intent);
            } else {
                // Open regular chat activity
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("receiverId", message.getSenderId());
                intent.putExtra("receiverName", message.getSenderName());
                intent.putExtra("receiverAvatar", message.getSenderName()); // You can add avatar URL later
                startActivity(intent);
            }
        });
        recyclerViewMessages.setAdapter(messageAdapter);

        // Setup search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMessages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Get current user ID
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", -1L);

        // Initialize API services
        chatApiService = ApiClient.getClient(getContext()).create(ChatApiService.class);
        friendApiService = ApiClient.getClient(getContext()).create(FriendApiService.class);

        // Load messages
        loadMessages();

        return view;
    }

    private void loadMessages() {
        // Start with Finbot (keep as is)
        List<ChatMessageResponse> messages = new ArrayList<>();
        ChatMessageResponse finbot = new ChatMessageResponse();
        finbot.setId(FINBOT_ID);
        finbot.setSenderId(FINBOT_ID);
        finbot.setSenderName("Finbot");
        finbot.setContent(getString(R.string.finbot_chat));
        finbot.setCreatedAt(java.time.Instant.now().toString());
        messages.add(finbot);

        // Load real user conversations
        if (currentUserId != null && currentUserId != -1L) {
            loadUserConversations(messages);
        } else {
            // If no user ID, just show Finbot
            allMessages = messages;
            messageAdapter.setMessageList(messages);
        }
    }

    private void loadUserConversations(List<ChatMessageResponse> messages) {
        // Get all friends
        friendApiService.getAllFriends().enqueue(new Callback<List<FriendResponse>>() {
            @Override
            public void onResponse(Call<List<FriendResponse>> call, Response<List<FriendResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FriendResponse> friends = response.body();
                    if (friends.isEmpty()) {
                        // No friends, just show Finbot
                        allMessages = messages;
                        messageAdapter.setMessageList(messages);
                        return;
                    }

                    // Map to track how many conversations we've loaded
                    final int[] loadedCount = {0};
                    final int totalFriends = friends.size();
                    final List<ChatMessageResponse> conversationMessages = new ArrayList<>();

                    // For each friend, get their last message
                    for (FriendResponse friend : friends) {
                        Long friendId = friend.getFriendId();
                        String friendName = friend.getFriendName();

                        // Get chat history with this friend
                        chatApiService.getHistory(friendId).enqueue(new Callback<List<ChatMessageResponse>>() {
                            @Override
                            public void onResponse(Call<List<ChatMessageResponse>> call, Response<List<ChatMessageResponse>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<ChatMessageResponse> history = response.body();
                                    
                                    if (!history.isEmpty()) {
                                        // Sort by timestamp to ensure we get the most recent message
                                        Collections.sort(history, new Comparator<ChatMessageResponse>() {
                                            @Override
                                            public int compare(ChatMessageResponse m1, ChatMessageResponse m2) {
                                                try {
                                                    if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                                                    if (m1.getCreatedAt() == null) return 1;
                                                    if (m2.getCreatedAt() == null) return -1;
                                                    java.time.Instant time1 = java.time.Instant.parse(m1.getCreatedAt());
                                                    java.time.Instant time2 = java.time.Instant.parse(m2.getCreatedAt());
                                                    return time1.compareTo(time2); // Ascending order (oldest first)
                                                } catch (Exception e) {
                                                    return 0;
                                                }
                                            }
                                        });
                                        
                                        // Get the last message (most recent after sorting)
                                        ChatMessageResponse lastMessage = history.get(history.size() - 1);
                                        
                                        // Create a conversation summary message
                                        ChatMessageResponse conversation = new ChatMessageResponse();
                                        conversation.setId(lastMessage.getId());
                                        
                                        // Determine sender/receiver based on the message
                                        // If current user sent it, friend is receiver, otherwise friend is sender
                                        if (lastMessage.getSenderId() != null && lastMessage.getSenderId().equals(currentUserId)) {
                                            // Current user sent the message, so friend is the receiver
                                            conversation.setSenderId(friendId);
                                            conversation.setSenderName(friendName);
                                            conversation.setReceiverId(currentUserId);
                                        } else {
                                            // Friend sent the message
                                            conversation.setSenderId(friendId);
                                            conversation.setSenderName(friendName);
                                            conversation.setReceiverId(currentUserId);
                                        }
                                        
                                        conversation.setContent(lastMessage.getContent());
                                        conversation.setCreatedAt(lastMessage.getCreatedAt());
                                        
                                        synchronized (conversationMessages) {
                                            conversationMessages.add(conversation);
                                        }
                                    }
                                }

                                // Check if all friends have been processed
                                synchronized (conversationMessages) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] >= totalFriends) {
                                        // All conversations loaded, now combine and sort
                                        List<ChatMessageResponse> finalMessages = new ArrayList<>(messages);
                                        finalMessages.addAll(conversationMessages);
                                        
                                        // Separate Finbot from other messages
                                        ChatMessageResponse finbotMessage = null;
                                        List<ChatMessageResponse> otherMessages = new ArrayList<>();
                                        for (ChatMessageResponse msg : finalMessages) {
                                            if (msg.getSenderId() != null && msg.getSenderId().equals(FINBOT_ID)) {
                                                finbotMessage = msg;
                                            } else {
                                                otherMessages.add(msg);
                                            }
                                        }
                                        
                                        // Sort other messages by timestamp (most recent first)
                                        Collections.sort(otherMessages, new Comparator<ChatMessageResponse>() {
                                            @Override
                                            public int compare(ChatMessageResponse m1, ChatMessageResponse m2) {
                                                try {
                                                    if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                                                    if (m1.getCreatedAt() == null) return 1;
                                                    if (m2.getCreatedAt() == null) return -1;
                                                    
                                                    // Parse timestamps and compare (most recent first)
                                                    java.time.Instant time1 = java.time.Instant.parse(m1.getCreatedAt());
                                                    java.time.Instant time2 = java.time.Instant.parse(m2.getCreatedAt());
                                                    return time2.compareTo(time1); // Descending order
                                                } catch (Exception e) {
                                                    return 0;
                                                }
                                            }
                                        });
                                        
                                        // Put Finbot at the top, then other messages
                                        finalMessages = new ArrayList<>();
                                        if (finbotMessage != null) {
                                            finalMessages.add(finbotMessage);
                                        }
                                        finalMessages.addAll(otherMessages);
                                        
                                        allMessages = finalMessages;
                                        messageAdapter.setMessageList(finalMessages);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ChatMessageResponse>> call, Throwable t) {
                                synchronized (conversationMessages) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] >= totalFriends) {
                                        // Even if some failed, show what we have
                                        List<ChatMessageResponse> finalMessages = new ArrayList<>(messages);
                                        finalMessages.addAll(conversationMessages);
                                        
                                        // Separate Finbot from other messages
                                        ChatMessageResponse finbotMessage = null;
                                        List<ChatMessageResponse> otherMessages = new ArrayList<>();
                                        for (ChatMessageResponse msg : finalMessages) {
                                            if (msg.getSenderId() != null && msg.getSenderId().equals(FINBOT_ID)) {
                                                finbotMessage = msg;
                                            } else {
                                                otherMessages.add(msg);
                                            }
                                        }
                                        
                                        // Sort other messages by timestamp
                                        Collections.sort(otherMessages, new Comparator<ChatMessageResponse>() {
                                            @Override
                                            public int compare(ChatMessageResponse m1, ChatMessageResponse m2) {
                                                try {
                                                    if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                                                    if (m1.getCreatedAt() == null) return 1;
                                                    if (m2.getCreatedAt() == null) return -1;
                                                    
                                                    java.time.Instant time1 = java.time.Instant.parse(m1.getCreatedAt());
                                                    java.time.Instant time2 = java.time.Instant.parse(m2.getCreatedAt());
                                                    return time2.compareTo(time1);
                                                } catch (Exception e) {
                                                    return 0;
                                                }
                                            }
                                        });
                                        
                                        // Put Finbot at the top, then other messages
                                        finalMessages = new ArrayList<>();
                                        if (finbotMessage != null) {
                                            finalMessages.add(finbotMessage);
                                        }
                                        finalMessages.addAll(otherMessages);
                                        
                                        allMessages = finalMessages;
                                        messageAdapter.setMessageList(finalMessages);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    // Failed to get friends, just show Finbot
                    allMessages = messages;
                    messageAdapter.setMessageList(messages);
                }
            }

            @Override
            public void onFailure(Call<List<FriendResponse>> call, Throwable t) {
                // Failed to get friends, just show Finbot
                allMessages = messages;
                messageAdapter.setMessageList(messages);
            }
        });
    }

    private void filterMessages(String query) {
        if (query.isEmpty()) {
            messageAdapter.setMessageList(allMessages);
        } else {
            List<ChatMessageResponse> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (ChatMessageResponse message : allMessages) {
                if (message.getSenderName() != null && 
                    message.getSenderName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(message);
                }
            }
            messageAdapter.setMessageList(filtered);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload messages when fragment resumes
        loadMessages();
    }
}
