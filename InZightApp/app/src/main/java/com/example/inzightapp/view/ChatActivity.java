package com.example.inzightapp.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.ChatMessageAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.chat.ChatApiService;
import com.example.inzightapp.model.request.ChatMessageRequest;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.example.inzightapp.websocket.StompManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageView btnSend, btnCamera, btnMic, btnGallery;
    private CircleImageView imgAvatar;
    private TextView tvContactName;
    private ChatMessageAdapter adapter;
    private ChatApiService chatApiService;
    private Long currentUserId;
    private Long receiverId;
    private String receiverName;
    private String receiverAvatar;
    private StompManager stompManager;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        receiverId = getIntent().getLongExtra("receiverId", -1);
        receiverName = getIntent().getStringExtra("receiverName");
        receiverAvatar = getIntent().getStringExtra("receiverAvatar");

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", -1);

        // Initialize views
        initViews();
        setupRecyclerView();
        setupListeners();
        setupApi();
        connectWebSocket();
        loadMessages();
    }

    private void initViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnCamera = findViewById(R.id.btnCamera);
        btnMic = findViewById(R.id.btnMic);
        btnGallery = findViewById(R.id.btnGallery);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvContactName = findViewById(R.id.tvContactName);

        // Set contact name
        if (receiverName != null) {
            tvContactName.setText(receiverName);
        }

        // Load avatar
        if (receiverAvatar != null && !receiverAvatar.isEmpty()) {
            Glide.with(this).load(receiverAvatar).into(imgAvatar);
        } else {
            String avatarUrl = "https://i.pravatar.cc/150?u=" + (receiverId != null ? receiverId : "");
            Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_default_avatar).into(imgAvatar);
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(this, currentUserId);
        recyclerViewChat.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide send button based on text
                boolean hasText = s.length() > 0;
                btnSend.setVisibility(hasText ? View.VISIBLE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCamera.setOnClickListener(v -> 
            Toast.makeText(this, "Camera feature coming soon", Toast.LENGTH_SHORT).show());
        btnMic.setOnClickListener(v -> 
            Toast.makeText(this, "Voice message feature coming soon", Toast.LENGTH_SHORT).show());
        btnGallery.setOnClickListener(v -> 
            Toast.makeText(this, "Gallery feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupApi() {
        chatApiService = ApiClient.getClient(this).create(ChatApiService.class);
    }

    private void connectWebSocket() {
        stompManager = StompManager.getInstance(this);
        String wsUrl = buildWsUrl();
        stompManager.connect(wsUrl, this::subscribeTopic,
                () -> Toast.makeText(this, "Không thể kết nối realtime", Toast.LENGTH_SHORT).show());
    }

    private String buildWsUrl() {
        String base = resolveBaseUrl();
        // Nếu baseUrl emulator, ApiClient sẽ dùng http://10.0.2.2:8080/
        if (base.startsWith("http://")) {
            return base.replace("http://", "ws://").replaceAll("/$", "") + "/ws";
        }
        if (base.startsWith("https://")) {
            return base.replace("https://", "wss://").replaceAll("/$", "") + "/ws";
        }
        return "ws://10.0.2.2:8080/ws";
    }

    private String resolveBaseUrl() {
        if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("emulator")) {
            return "http://10.0.2.2:8080/";
        }
        return ApiClient.BASE_URL.endsWith("/") ? ApiClient.BASE_URL : ApiClient.BASE_URL + "/";
    }

    private void subscribeTopic() {
        if (currentUserId == null) return;
        String destination = "/topic/chat/" + currentUserId;
        stompManager.subscribe(destination, msg -> {
            ChatMessageResponse incoming = gson.fromJson(msg.getPayload(), ChatMessageResponse.class);
            runOnUiThread(() -> handleIncomingMessage(incoming));
        });
    }

    private void handleIncomingMessage(ChatMessageResponse message) {
        if (message == null) return;
        // Chỉ nhận tin của cuộc hội thoại hiện tại
        boolean related = (receiverId != null &&
                ((message.getSenderId() != null && message.getSenderId().equals(receiverId))
                        || (message.getReceiverId() != null && message.getReceiverId().equals(receiverId))));
        if (!related) return;
        adapter.addMessage(message);
        scrollToBottom();
    }

    private void loadMessages() {
        if (receiverId == -1) {
            adapter.setMessageList(new ArrayList<>());
            return;
        }

        chatApiService.getHistory(receiverId).enqueue(new Callback<List<ChatMessageResponse>>() {
            @Override
            public void onResponse(Call<List<ChatMessageResponse>> call, Response<List<ChatMessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMessageList(response.body());
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessageResponse>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Không tải được lịch sử chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        if (receiverId == -1) {
            // Mock: Add message to list immediately
            ChatMessageResponse newMessage = new ChatMessageResponse();
            newMessage.setId(System.currentTimeMillis());
            newMessage.setSenderId(currentUserId);
            newMessage.setContent(content);
            newMessage.setCreatedAt(java.time.Instant.now().toString());
            adapter.addMessage(newMessage);
            etMessage.setText("");
            scrollToBottom();
            return;
        }

        ChatMessageRequest request = new ChatMessageRequest(currentUserId, receiverId, content);
        // Optimistic UI update
        ChatMessageResponse pending = new ChatMessageResponse();
        pending.setId(System.currentTimeMillis());
        pending.setSenderId(currentUserId);
        pending.setReceiverId(receiverId);
        pending.setContent(content);
        pending.setCreatedAt(java.time.Instant.now().toString());
        adapter.addMessage(pending);
        etMessage.setText("");
        scrollToBottom();

        chatApiService.sendMessage(request).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                // Server sẽ đẩy realtime, nên không cần xử lý thêm
            }

            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerViewChat.post(() -> 
                recyclerViewChat.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không disconnect toàn bộ vì có thể fragment khác dùng; chỉ dừng listener
    }
}

