package com.example.inzightapp.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.ChatMessageAdapter;
import com.example.inzightapp.ai.TransactionAIExtractor;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.utils.LanguageDetector;
import com.example.inzightapp.api.chat.ChatApiService;
import com.example.inzightapp.api.finance.CategoryApiService;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.api.finance.WalletApiService;
import com.example.inzightapp.model.request.ChatMessageRequest;
import com.example.inzightapp.model.request.TransactionRequest;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.example.inzightapp.model.response.CategoryResponse;
import com.example.inzightapp.model.response.WalletResponse;
import com.example.inzightapp.websocket.StompManager;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinbotChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageView btnSend;
    private ChatMessageAdapter adapter;
    private Long currentUserId;
    private static final Long FINBOT_ID = 24L; // Special ID for Finbot (trùng với BE)
    private ChatApiService chatApiService;
    private TransactionApiService transactionApiService;
    private WalletApiService walletApiService;
    private CategoryApiService categoryApiService;
    private StompManager stompManager;
    private Gson gson = new Gson();
    private List<WalletResponse> wallets = new ArrayList<>();
    private WalletResponse defaultWallet;
    private List<CategoryResponse> categories = new ArrayList<>();
    
    // Store detected language for current conversation
    private String currentLanguage = "en"; // Default to English

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finbot_chat);

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", 1L);

        // Initialize views
        initViews();
        setupRecyclerView();
        
        // Set listener for transaction save
        adapter.setOnTransactionSaveListener(this::saveTransactionFromChat);
        setupListeners();
        setupApi();
        connectWebSocket();
        loadWallets();
        loadCategories();
        loadChatHistory();
        
        // Kiểm tra và hỏi về chi tiêu hôm nay sau khi load xong
        checkAndAskDailyExpenseOnStart();
    }
    
    /**
     * Kiểm tra và tự động hỏi về chi tiêu hôm nay khi vào chat lần đầu trong ngày
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkAndAskDailyExpenseOnStart() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String lastAskedDate = prefs.getString("finbot_last_asked_date", "");
        String today = java.time.LocalDate.now().toString();
        
        // Nếu chưa hỏi hôm nay, đánh dấu sẽ hỏi sau khi load history xong
        if (!today.equals(lastAskedDate)) {
            shouldAskDailyExpense = true;
        }
    }
    
    private boolean shouldAskDailyExpense = false;

    private void initViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(this, currentUserId);
        adapter.setFinbotMode(true); // Finbot luôn bên trái, user bên phải
        adapter.setFinbotId(FINBOT_ID);
        recyclerViewChat.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable/disable send button based on text
                boolean hasText = s.length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupApi() {
        chatApiService = ApiClient.getClient(this).create(ChatApiService.class);
        transactionApiService = ApiClient.getClient(this).create(TransactionApiService.class);
        walletApiService = ApiClient.getClient(this).create(WalletApiService.class);
        categoryApiService = ApiClient.getClient(this).create(CategoryApiService.class);
    }
    
    private void loadWallets() {
        walletApiService.getWallets().enqueue(new retrofit2.Callback<List<WalletResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<WalletResponse>> call, retrofit2.Response<List<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    wallets = response.body();
                    defaultWallet = wallets.get(0); // Lấy ví đầu tiên làm mặc định
                    android.util.Log.d("FinbotChat", "Loaded wallets: " + wallets.size() + ", default: " + defaultWallet.getName());
                } else {
                    // User chưa có wallet nào, tự động tạo wallet mặc định
                    android.util.Log.d("FinbotChat", "No wallets found, creating default wallet...");
                    createDefaultWallet();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<WalletResponse>> call, Throwable t) {
                android.util.Log.e("FinbotChat", "Failed to load wallets", t);
                // Thử tạo wallet mặc định nếu load fail
                createDefaultWallet();
            }
        });
    }
    
    /**
     * Tạo wallet mặc định cho user mới
     */
    private void createDefaultWallet() {
        com.example.inzightapp.model.request.WalletRequest request = 
            new com.example.inzightapp.model.request.WalletRequest("My Wallet", java.math.BigDecimal.ZERO, "VND");
        
        walletApiService.createWallet(request).enqueue(new retrofit2.Callback<WalletResponse>() {
            @Override
            public void onResponse(retrofit2.Call<WalletResponse> call, retrofit2.Response<WalletResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletResponse newWallet = response.body();
                    defaultWallet = newWallet;
                    wallets.clear();
                    wallets.add(newWallet);
                    android.util.Log.d("FinbotChat", "✅ Default wallet created: " + newWallet.getName() + " (ID: " + newWallet.getId() + ")");
                } else {
                    android.util.Log.e("FinbotChat", "Failed to create default wallet: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<WalletResponse> call, Throwable t) {
                android.util.Log.e("FinbotChat", "Failed to create default wallet", t);
            }
        });
    }
    
    private void loadCategories() {
        // Load cả EXPENSE và INCOME categories
        categoryApiService.getCategories("EXPENSE").enqueue(new retrofit2.Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<CategoryResponse>> call, retrofit2.Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.addAll(response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<CategoryResponse>> call, Throwable t) {
                // Ignore
            }
        });
        
        categoryApiService.getCategories("INCOME").enqueue(new retrofit2.Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<CategoryResponse>> call, retrofit2.Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.addAll(response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<CategoryResponse>> call, Throwable t) {
                // Ignore
            }
        });
    }

    private void connectWebSocket() {
        stompManager = StompManager.getInstance(this);
        String wsUrl = buildWsUrl();
        stompManager.connect(wsUrl, this::subscribeTopic, () ->
                Toast.makeText(this, getString(R.string.cannot_connect_realtime), Toast.LENGTH_SHORT).show());
    }

    private String buildWsUrl() {
        String base = ApiClient.BASE_URL;
        if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("emulator")) {
            base = "http://10.0.2.2:8080/";
        } else if (!base.endsWith("/")) {
            base = base + "/";
        }
        if (base.startsWith("http://")) {
            return base.replace("http://", "ws://").replaceAll("/$", "") + "/ws";
        }
        if (base.startsWith("https://")) {
            return base.replace("https://", "wss://").replaceAll("/$", "") + "/ws";
        }
        return "ws://10.0.2.2:8080/ws";
    }

    private void subscribeTopic() {
        if (currentUserId == null) return;
        stompManager.subscribe("/topic/chat/" + currentUserId, msg -> {
            ChatMessageResponse incoming = gson.fromJson(msg.getPayload(), ChatMessageResponse.class);
            runOnUiThread(() -> {
                if (incoming != null) {
                    // Nhận cả message từ Finbot và user (bao gồm cả transaction card)
                    // Transaction card có senderId = FINBOT_ID, receiverId = currentUserId
                    boolean isFromFinbot = FINBOT_ID.equals(incoming.getSenderId());
                    boolean isToCurrentUser = currentUserId.equals(incoming.getReceiverId());
                    boolean isTransactionCard = incoming.getContent() != null && 
                        (incoming.getContent().equals("TRANSACTION_CARD") || 
                         incoming.getContent().startsWith("TRANSACTION_CARD:"));
                    
                    // Nhận message từ Finbot hoặc transaction card
                    if (isFromFinbot || (isTransactionCard && isToCurrentUser)) {
                        // Chỉ add nếu chưa có (tránh duplicate từ REST + WS)
                        if (!adapter.hasMessage(incoming.getId())) {
                            adapter.addMessage(incoming);
                            scrollToBottom();
                        }
                    }
                }
            });
        });
    }

    private void loadChatHistory() {
        // Load lịch sử chat với FinBot (receiverId = FINBOT_ID)
        android.util.Log.d("FinbotChat", "Loading chat history with FinBot...");
        chatApiService.getHistory(FINBOT_ID).enqueue(new retrofit2.Callback<List<ChatMessageResponse>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(retrofit2.Call<List<ChatMessageResponse>> call, retrofit2.Response<List<ChatMessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessageResponse> history = response.body();
                    android.util.Log.d("FinbotChat", "✅ Loaded " + history.size() + " messages from history");
                    
                    // Log tất cả messages trong history để debug
                    for (ChatMessageResponse msg : history) {
                        android.util.Log.d("FinbotChat", "📨 Message in history - ID: " + msg.getId() + 
                            ", Sender: " + msg.getSenderId() + ", Receiver: " + msg.getReceiverId() +
                            ", Content: " + (msg.getContent() != null ? 
                                (msg.getContent().length() > 200 ? msg.getContent().substring(0, 200) + "..." : msg.getContent()) : "NULL"));
                        if (msg.getContent() != null && msg.getContent().startsWith("TRANSACTION_CARD:")) {
                            android.util.Log.d("FinbotChat", "📋 ⭐ TRANSACTION CARD FOUND in history - ID: " + msg.getId());
                        }
                    }
                    
                    // Load thêm messages từ user (để lấy transaction cards có thể bị lưu với receiverId = currentUserId)
                    // Transaction cards có thể có receiverId = currentUserId thay vì FINBOT_ID
                    loadUserMessagesForTransactionCards(history);
                } else {
                    android.util.Log.d("FinbotChat", "❌ No history found or response failed");
                    // Nếu không có lịch sử, vẫn thử load transaction cards từ user messages
                    loadUserMessagesForTransactionCards(new ArrayList<>());
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFailure(retrofit2.Call<List<ChatMessageResponse>> call, Throwable t) {
                android.util.Log.e("FinbotChat", "❌ Failed to load chat history", t);
                // Nếu lỗi, vẫn thử load transaction cards từ user messages
                loadUserMessagesForTransactionCards(new ArrayList<>());
            }
        });
    }
    
    /**
     * Load messages từ user để tìm transaction cards có thể bị lưu với receiverId = currentUserId
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadUserMessagesForTransactionCards(List<ChatMessageResponse> existingHistory) {
        // Load messages với receiverId = currentUserId để tìm transaction cards
        chatApiService.getHistory(currentUserId).enqueue(new retrofit2.Callback<List<ChatMessageResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<ChatMessageResponse>> call, retrofit2.Response<List<ChatMessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessageResponse> userMessages = response.body();
                    android.util.Log.d("FinbotChat", "✅ Loaded " + userMessages.size() + " messages from user history");
                    
                    // Tìm transaction cards trong user messages
                    List<ChatMessageResponse> transactionCards = new ArrayList<>();
                    for (ChatMessageResponse msg : userMessages) {
                        if (msg.getContent() != null && msg.getContent().startsWith("TRANSACTION_CARD:")) {
                            // Đảm bảo senderId = FINBOT_ID để hiển thị đúng
                            msg.setSenderId(FINBOT_ID);
                            msg.setReceiverId(currentUserId);
                            transactionCards.add(msg);
                            android.util.Log.d("FinbotChat", "📋 ⭐ Found transaction card in user messages - ID: " + msg.getId());
                        }
                    }
                    
                    // Merge với existing history và sort theo createdAt
                    List<ChatMessageResponse> allMessages = new ArrayList<>(existingHistory);
                    allMessages.addAll(transactionCards);
                    
                    // Sort theo createdAt (oldest first)
                    allMessages.sort((m1, m2) -> {
                        if (m1.getCreatedAt() == null || m2.getCreatedAt() == null) return 0;
                        return m1.getCreatedAt().compareTo(m2.getCreatedAt());
                    });
                    
                    android.util.Log.d("FinbotChat", "✅ Merged " + allMessages.size() + " total messages (including " + transactionCards.size() + " transaction cards)");
                    
                    // Adapter sẽ tự động parse transaction cards từ history trong setMessageList()
                    adapter.setMessageList(allMessages);
                    scrollToBottom();
                    
                    // Nếu không có messages nào, hiển thị welcome
                    if (allMessages.isEmpty()) {
                        loadWelcomeMessages();
                    } else if (shouldAskDailyExpense) {
                        // Kiểm tra xem đã hỏi về chi tiêu hôm nay chưa
                        checkAndAskDailyExpense();
                    }
                } else {
                    // Nếu không load được user messages, vẫn dùng existing history
                    if (existingHistory.isEmpty()) {
                        loadWelcomeMessages();
                    } else {
                        adapter.setMessageList(existingHistory);
                        scrollToBottom();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<ChatMessageResponse>> call, Throwable t) {
                android.util.Log.e("FinbotChat", "❌ Failed to load user messages for transaction cards", t);
                // Nếu không load được, vẫn dùng existing history
                if (existingHistory.isEmpty()) {
                    loadWelcomeMessages();
                } else {
                    adapter.setMessageList(existingHistory);
                    scrollToBottom();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadWelcomeMessages() {
        List<ChatMessageResponse> initialMessages = new ArrayList<>();
        
        // Finbot's welcome message - với tính cách "lầy lội"
        ChatMessageResponse finbotMsg1 = new ChatMessageResponse();
        finbotMsg1.setId(1L);
        finbotMsg1.setSenderId(FINBOT_ID);
        finbotMsg1.setSenderName("Finbot");
        finbotMsg1.setContent(getString(R.string.finbot_welcome_1));
        finbotMsg1.setCreatedAt(java.time.Instant.now().toString());
        initialMessages.add(finbotMsg1);

        // Finbot's instruction message - giữ nguyên nhưng có thể thêm chút hài hước
        ChatMessageResponse finbotMsg2 = new ChatMessageResponse();
        finbotMsg2.setId(2L);
        finbotMsg2.setSenderId(FINBOT_ID);
        finbotMsg2.setSenderName("Finbot");
        finbotMsg2.setContent(getString(R.string.finbot_welcome_2));
        finbotMsg2.setCreatedAt(java.time.Instant.now().toString());
        initialMessages.add(finbotMsg2);

        adapter.setMessageList(initialMessages);
        scrollToBottom();
        
        // Sau khi hiển thị welcome, hỏi về chi tiêu hôm nay nếu cần
        if (shouldAskDailyExpense) {
            checkAndAskDailyExpense();
        }
    }
    
    /**
     * Kiểm tra và tự động hỏi về chi tiêu hôm nay nếu chưa hỏi
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkAndAskDailyExpense() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String lastAskedDate = prefs.getString("finbot_last_asked_date", "");
        String today = java.time.LocalDate.now().toString();
        
        // Nếu chưa hỏi hôm nay, tự động gửi câu hỏi
        if (!today.equals(lastAskedDate)) {
            // Delay một chút để UI đã load xong
            recyclerViewChat.postDelayed(() -> {
                sendAutoQuestion();
                // Lưu ngày đã hỏi
                prefs.edit().putString("finbot_last_asked_date", today).apply();
            }, 1000);
        }
    }
    
    /**
     * Gửi câu hỏi tự động về chi tiêu hôm nay
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendAutoQuestion() {
        String question = getString(R.string.finbot_daily_question);
        
        // Tạo message từ Finbot
        ChatMessageResponse finbotQuestion = new ChatMessageResponse();
        finbotQuestion.setId(System.currentTimeMillis());
        finbotQuestion.setSenderId(FINBOT_ID);
        finbotQuestion.setSenderName("Finbot");
        finbotQuestion.setContent(question);
        finbotQuestion.setCreatedAt(java.time.Instant.now().toString());
        
        // Thêm vào adapter
        adapter.addMessage(finbotQuestion);
        scrollToBottom();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Add user message
        ChatMessageResponse userMessage = new ChatMessageResponse();
        userMessage.setId(System.currentTimeMillis());
        userMessage.setSenderId(currentUserId);
        userMessage.setReceiverId(FINBOT_ID); // Set receiver là Finbot
        userMessage.setContent(content);
        userMessage.setCreatedAt(java.time.Instant.now().toString());
        android.util.Log.d("FinbotChat", "Adding user message - SenderId: " + userMessage.getSenderId() + ", ReceiverId: " + userMessage.getReceiverId() + ", Content: " + content);
        adapter.addMessage(userMessage);
        etMessage.setText("");
        scrollToBottom();

        // Phân tích transaction từ tin nhắn người dùng (TRƯỚC khi gọi AI backend)
        TransactionAIExtractor.ExtractedTransaction extractedTransaction = null;
        try {
            extractedTransaction = TransactionAIExtractor.extract(content);
            android.util.Log.d("FinbotChat", "User message: \"" + content + "\"");
            android.util.Log.d("FinbotChat", "Extracted transaction: " + 
                (extractedTransaction != null && extractedTransaction.getAmount() != null ? 
                    extractedTransaction.getAmount() + " - " + extractedTransaction.getSuggestedCategory() : "null"));
        } catch (Exception e) {
            android.util.Log.e("FinbotChat", "Error extracting transaction", e);
        }
        
        // Nếu phát hiện transaction, hiển thị suggestion card và lưu vào DB
        if (extractedTransaction != null && extractedTransaction.getAmount() != null) {
            android.util.Log.d("FinbotChat", "✅ Found transaction! Creating card. Amount: " + extractedTransaction.getAmount());
            saveTransactionCardToBackend(extractedTransaction, content);
        } else {
            android.util.Log.d("FinbotChat", "❌ No transaction detected from: \"" + content + "\"");
        }

        // Phát hiện ngôn ngữ và thêm instruction cho AI
        String detectedLanguage = LanguageDetector.detectLanguage(content);
        currentLanguage = detectedLanguage; // Store for use in notifications
        adapter.setCurrentLanguage(detectedLanguage); // Update adapter language for transaction cards
        
        // Build comprehensive system prompt
        StringBuilder systemPromptBuilder = new StringBuilder();
        
        if ("vi".equals(detectedLanguage)) {
            systemPromptBuilder.append("BẠN LÀ FINBOT - TRỢ LÝ TÀI CHÍNH THÔNG MINH.\n");
            systemPromptBuilder.append("QUAN TRỌNG: Bạn PHẢI trả lời bằng TIẾNG VIỆT. KHÔNG được trả lời bằng tiếng Anh.\n");
            systemPromptBuilder.append("Nhiệm vụ của bạn:\n");
            systemPromptBuilder.append("1. Trả lời các câu hỏi về tài chính bằng tiếng Việt\n");
            systemPromptBuilder.append("2. Giúp người dùng ghi chép giao dịch\n");
            systemPromptBuilder.append("3. Đưa ra lời khuyên tài chính hữu ích\n");
            systemPromptBuilder.append("4. Luôn trả lời bằng tiếng Việt, không bao giờ dùng tiếng Anh\n\n");
        } else {
            systemPromptBuilder.append("YOU ARE FINBOT - AN INTELLIGENT FINANCIAL ASSISTANT.\n");
            systemPromptBuilder.append("IMPORTANT: You MUST respond in ENGLISH. Do NOT respond in Vietnamese.\n");
            systemPromptBuilder.append("Your tasks:\n");
            systemPromptBuilder.append("1. Answer financial questions in English\n");
            systemPromptBuilder.append("2. Help users record transactions\n");
            systemPromptBuilder.append("3. Provide useful financial advice\n");
            systemPromptBuilder.append("4. Always respond in English, never use Vietnamese\n\n");
        }
        
        boolean hasTransaction = extractedTransaction != null && extractedTransaction.getAmount() != null;
        if (hasTransaction) {
            if ("vi".equals(detectedLanguage)) {
                systemPromptBuilder.append("LƯU Ý: Người dùng vừa cung cấp thông tin giao dịch. Hãy xác nhận và hỏi thêm thông tin nếu cần.\n\n");
            } else {
                systemPromptBuilder.append("NOTE: The user has provided transaction information. Please acknowledge and ask for additional details if needed.\n\n");
            }
        }
        
        systemPromptBuilder.append("User message: ");
        systemPromptBuilder.append(content);
        
        String contentWithSystemPrompt = systemPromptBuilder.toString();
        
        android.util.Log.d("FinbotChat", "Detected language: " + detectedLanguage);
        android.util.Log.d("FinbotChat", "Has transaction: " + hasTransaction);
        android.util.Log.d("FinbotChat", "System prompt sent to AI");
        
        // Lưu user message vào database trước
        ChatMessageRequest userMessageRequest = new ChatMessageRequest(currentUserId, FINBOT_ID, content);
        final Long tempMessageId = userMessage.getId(); // Lưu temporary ID để update sau
        chatApiService.sendMessage(userMessageRequest).enqueue(new retrofit2.Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ChatMessageResponse> call, retrofit2.Response<ChatMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessageResponse savedUserMessage = response.body();
                    android.util.Log.d("FinbotChat", "✅ User message saved to database with ID: " + savedUserMessage.getId());
                    // Update message trong adapter với ID từ backend để tránh duplicate khi load history
                    adapter.updateMessageId(tempMessageId, savedUserMessage.getId());
                } else {
                    android.util.Log.w("FinbotChat", "⚠️ Failed to save user message to database: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatMessageResponse> call, Throwable t) {
                android.util.Log.e("FinbotChat", "❌ Error saving user message to database", t);
            }
        });
        
        // Gọi AI backend sau khi đã lưu user message
        ChatMessageRequest request = new ChatMessageRequest(currentUserId, FINBOT_ID, contentWithSystemPrompt);
        TransactionAIExtractor.ExtractedTransaction finalExtractedTransaction = extractedTransaction;
        chatApiService.chatWithAi(request).enqueue(new retrofit2.Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ChatMessageResponse> call, retrofit2.Response<ChatMessageResponse> response) {
                if (response.isSuccessful()) {
                    ChatMessageResponse ai = response.body();
                    
                    // Xử lý trường hợp response body null hoặc empty
                    if (ai == null) {
                        android.util.Log.w("FinbotChat", "AI response is null, waiting for WebSocket message");
                        // Nếu response null, có thể WebSocket sẽ push message sau
                        return;
                    }
                    
                    // Luôn hiển thị reply từ AI (không filter nữa để AI có thể trả lời tự do)
                    if (ai.getSenderId() == null) {
                        ai.setSenderId(FINBOT_ID);
                    }
                    // Chỉ add nếu chưa có (WS có thể đã push trước)
                    if (!adapter.hasMessage(ai.getId())) {
                        adapter.addMessage(ai);
                        scrollToBottom();
                    }
                } else {
                    // Response không thành công, log để debug
                    android.util.Log.e("FinbotChat", "AI API returned error: " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("FinbotChat", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FinbotChat", "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatMessageResponse> call, Throwable t) {
                // Log lỗi chi tiết hơn
                android.util.Log.e("FinbotChat", "Failed to call AI: " + t.getMessage(), t);
                
                // Nếu lỗi là do parse JSON (response null/empty), có thể WebSocket sẽ push message sau
                if (t.getMessage() != null && t.getMessage().contains("End of input")) {
                    android.util.Log.w("FinbotChat", "AI response was empty/null, waiting for WebSocket message");
                }
            }
        });
    }
    
    /**
     * Lưu transaction card vào backend dưới dạng message đặc biệt
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveTransactionCardToBackend(TransactionAIExtractor.ExtractedTransaction transaction, String originalMessage) {
        try {
            // Tạo JSON object chứa transaction data
            com.google.gson.JsonObject jsonData = new com.google.gson.JsonObject();
            jsonData.addProperty("amount", transaction.getAmount().toString());
            jsonData.addProperty("type", transaction.getType() != null ? transaction.getType() : "EXPENSE");
            jsonData.addProperty("category", transaction.getSuggestedCategory() != null ? transaction.getSuggestedCategory() : "");
            jsonData.addProperty("note", transaction.getNote() != null ? transaction.getNote() : "");
            
            if (transaction.getTransactionDate() != null) {
                java.time.Instant instant = transaction.getTransactionDate().toInstant();
                jsonData.addProperty("date", instant.toString());
            } else {
                jsonData.addProperty("date", java.time.Instant.now().toString());
            }
            
            // Content format: "TRANSACTION_CARD:{json_data}"
            String cardContent = "TRANSACTION_CARD:" + gson.toJson(jsonData);
            
            // Gửi message này lên backend
            ChatMessageRequest cardRequest = new ChatMessageRequest(FINBOT_ID, currentUserId, cardContent);
            chatApiService.sendMessage(cardRequest).enqueue(new retrofit2.Callback<ChatMessageResponse>() {
                @Override
                public void onResponse(retrofit2.Call<ChatMessageResponse> call, retrofit2.Response<ChatMessageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ChatMessageResponse savedCard = response.body();
                        // Store transaction data với message ID từ backend
                        adapter.setTransactionForMessage(savedCard.getId(), transaction);
                        // Add vào UI
                        adapter.addMessage(savedCard);
                        scrollToBottom();
                        android.util.Log.d("FinbotChat", "Transaction card saved with ID: " + savedCard.getId());
                        
                        // Tự động lưu transaction sau khi card được lưu thành công
                        // Delay một chút để card hiển thị trước
                        recyclerViewChat.postDelayed(() -> {
                            autoSaveTransaction(transaction);
                        }, 500);
                    } else {
                        // Fallback: hiển thị card local nếu backend fail
                        ChatMessageResponse localCard = createLocalTransactionCard(transaction);
                        adapter.addMessage(localCard);
                        scrollToBottom();
                        // Vẫn thử auto save nếu có thể
                        recyclerViewChat.postDelayed(() -> {
                            autoSaveTransaction(transaction);
                        }, 500);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ChatMessageResponse> call, Throwable t) {
                    android.util.Log.e("FinbotChat", "Failed to save transaction card to backend", t);
                    // Fallback: hiển thị card local
                    ChatMessageResponse localCard = createLocalTransactionCard(transaction);
                    adapter.addMessage(localCard);
                    scrollToBottom();
                    // Vẫn thử auto save nếu có thể
                    recyclerViewChat.postDelayed(() -> {
                        autoSaveTransaction(transaction);
                    }, 500);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("FinbotChat", "Error saving transaction card", e);
            // Fallback: hiển thị card local
            ChatMessageResponse localCard = createLocalTransactionCard(transaction);
            adapter.addMessage(localCard);
            scrollToBottom();
        }
    }
    
    /**
     * Tạo transaction card local (không lưu vào DB) - dùng khi backend fail
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private ChatMessageResponse createLocalTransactionCard(TransactionAIExtractor.ExtractedTransaction transaction) {
        ChatMessageResponse card = new ChatMessageResponse();
        Long cardId = System.currentTimeMillis() + 10000; // Unique ID for transaction card
        card.setId(cardId);
        card.setSenderId(FINBOT_ID);
        card.setContent("TRANSACTION_CARD"); // Special marker for adapter
        card.setCreatedAt(java.time.Instant.now().toString());
        
        // Store transaction data with message ID in adapter
        adapter.setTransactionForMessage(cardId, transaction);
        
        return card;
    }
    
    /**
     * Tự động lưu transaction khi phát hiện từ chat (không hiển thị Toast, chỉ gửi thông báo từ Finbot)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void autoSaveTransaction(TransactionAIExtractor.ExtractedTransaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            android.util.Log.e("FinbotChat", "Cannot auto-save: transaction or amount is null");
            return;
        }
        
        // Đợi wallets và categories được load xong
        if (defaultWallet == null) {
            android.util.Log.d("FinbotChat", "Waiting for wallet to load or create...");
            // Thử tạo wallet mặc định nếu chưa có
            if (wallets.isEmpty()) {
                createDefaultWallet();
            }
            // Retry sau 1 giây để đợi wallet được tạo
            recyclerViewChat.postDelayed(() -> {
                if (defaultWallet == null) {
                    android.util.Log.e("FinbotChat", "Cannot auto-save: wallet still null after retry");
                    sendFinbotMessage("⚠️ Vui lòng tạo ví trước khi lưu giao dịch. Vào Settings để tạo ví.");
                } else {
                    // Wallet đã được tạo, thử lại
                    autoSaveTransaction(transaction);
                }
            }, 1500); // Delay 1.5s để đợi wallet được tạo
            return;
        }
        
        String transactionType = transaction.getType() != null ? transaction.getType() : "EXPENSE";
        
        // Tìm category phù hợp
        Long categoryId = findCategoryId(transaction.getSuggestedCategory(), transactionType);
        
        if (categoryId == null) {
            android.util.Log.e("FinbotChat", "Cannot auto-save: category not found");
            // Gửi thông báo lỗi từ Finbot theo ngôn ngữ của user
            String errorMessage = "vi".equals(currentLanguage) ? 
                "⚠️ Không tìm thấy category phù hợp. Vui lòng thử lại sau." :
                "⚠️ Category not found. Please try again later.";
            sendFinbotMessage(errorMessage);
            return;
        }
        
        TransactionRequest request = new TransactionRequest();
        request.setAmount(transaction.getAmount());
        request.setType(transactionType);
        request.setNote(transaction.getNote() != null ? transaction.getNote() : "");
        request.setWalletId(defaultWallet.getId());
        request.setCategoryId(categoryId);
        
        // Convert Date to ISO-8601 Instant format for backend
        java.util.Date txDate = transaction.getTransactionDate() != null ? 
            transaction.getTransactionDate() : new java.util.Date();
        java.time.Instant instant = txDate.toInstant();
        request.setTransactionDate(instant.toString()); // ISO-8601 format
        
        android.util.Log.d("FinbotChat", "Auto-saving transaction: " + transaction.getAmount() + " - " + transaction.getNote());
        
        transactionApiService.createTransaction(request).enqueue(new retrofit2.Callback<com.example.inzightapp.model.response.TransactionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.inzightapp.model.response.TransactionResponse> call, 
                                  retrofit2.Response<com.example.inzightapp.model.response.TransactionResponse> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("FinbotChat", "✅ Transaction auto-saved successfully");
                    
                    // Gửi thông báo xác nhận từ Finbot theo ngôn ngữ của user
                    String confirmationMessage;
                    if ("vi".equals(currentLanguage)) {
                        confirmationMessage = "✅ Đã lưu giao dịch: " + formatAmount(transaction.getAmount()) + " - " + 
                            (transaction.getNote() != null && !transaction.getNote().isEmpty() ? transaction.getNote() : transactionType);
                    } else {
                        confirmationMessage = "✅ Transaction saved: " + formatAmount(transaction.getAmount()) + " - " + 
                            (transaction.getNote() != null && !transaction.getNote().isEmpty() ? transaction.getNote() : transactionType);
                    }
                    sendFinbotMessage(confirmationMessage);
                } else {
                    android.util.Log.e("FinbotChat", "Failed to auto-save transaction: " + response.code());
                    sendFinbotMessage("⚠️ Lỗi khi lưu giao dịch. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.inzightapp.model.response.TransactionResponse> call, Throwable t) {
                android.util.Log.e("FinbotChat", "Failed to auto-save transaction", t);
                sendFinbotMessage("⚠️ Lỗi kết nối. Vui lòng thử lại sau.");
            }
        });
    }
    
    /**
     * Lưu transaction từ chat (khi user click nút Save trên card - giữ lại để có thể dùng sau)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveTransactionFromChat(TransactionAIExtractor.ExtractedTransaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            Toast.makeText(this, getString(R.string.cannot_save_transaction), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (defaultWallet == null) {
            Toast.makeText(this, getString(R.string.please_select_wallet_first), Toast.LENGTH_SHORT).show();
            return;
        }
        
        String transactionType = transaction.getType() != null ? transaction.getType() : "EXPENSE";
        
        // Tìm category phù hợp
        Long categoryId = findCategoryId(transaction.getSuggestedCategory(), transactionType);
        
        if (categoryId == null) {
            Toast.makeText(this, getString(R.string.category_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        
        TransactionRequest request = new TransactionRequest();
        request.setAmount(transaction.getAmount());
        request.setType(transactionType);
        request.setNote(transaction.getNote() != null ? transaction.getNote() : "");
        request.setWalletId(defaultWallet.getId());
        request.setCategoryId(categoryId);
        
        // Convert Date to ISO-8601 Instant format for backend
        java.util.Date txDate = transaction.getTransactionDate() != null ? 
            transaction.getTransactionDate() : new java.util.Date();
        java.time.Instant instant = txDate.toInstant();
        request.setTransactionDate(instant.toString()); // ISO-8601 format
        
        transactionApiService.createTransaction(request).enqueue(new retrofit2.Callback<com.example.inzightapp.model.response.TransactionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.inzightapp.model.response.TransactionResponse> call, 
                                  retrofit2.Response<com.example.inzightapp.model.response.TransactionResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FinbotChatActivity.this, getString(R.string.transaction_saved_success), Toast.LENGTH_SHORT).show();
                    
                    // Thêm tin nhắn xác nhận từ Finbot theo ngôn ngữ của user
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String confirmationMessage;
                        if ("vi".equals(currentLanguage)) {
                            confirmationMessage = "✅ Đã lưu giao dịch: " + formatAmount(transaction.getAmount()) + " - " + 
                                (transaction.getNote() != null ? transaction.getNote() : transaction.getType());
                        } else {
                            confirmationMessage = "✅ Transaction saved: " + formatAmount(transaction.getAmount()) + " - " + 
                                (transaction.getNote() != null ? transaction.getNote() : transaction.getType());
                        }
                        sendFinbotMessage(confirmationMessage);
                    }
                } else {
                    Toast.makeText(FinbotChatActivity.this, getString(R.string.error_saving_transaction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.inzightapp.model.response.TransactionResponse> call, Throwable t) {
                Toast.makeText(FinbotChatActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Gửi tin nhắn từ Finbot (helper method)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendFinbotMessage(String content) {
        ChatMessageResponse msg = new ChatMessageResponse();
        msg.setId(System.currentTimeMillis());
        msg.setSenderId(FINBOT_ID);
        msg.setSenderName("Finbot");
        msg.setContent(content);
        msg.setCreatedAt(java.time.Instant.now().toString());
        adapter.addMessage(msg);
        scrollToBottom();
    }
    
    /**
     * Tìm category ID từ suggested category name
     */
    private Long findCategoryId(String suggestedCategoryName, String transactionType) {
        if (suggestedCategoryName == null || suggestedCategoryName.isEmpty()) {
            // Nếu không có suggested category, lấy category đầu tiên của type
            for (CategoryResponse cat : categories) {
                if (cat.getType() != null && cat.getType().equals(transactionType)) {
                    return cat.getId();
                }
            }
            return null;
        }
        
        // Tìm category theo tên (exact match hoặc contains)
        for (CategoryResponse cat : categories) {
            if (cat.getType() != null && cat.getType().equals(transactionType)) {
                String catName = cat.getName().toLowerCase();
                String suggested = suggestedCategoryName.toLowerCase();
                
                // Exact match
                if (catName.equals(suggested)) {
                    return cat.getId();
                }
                
                // Contains match (ví dụ: "Food & Dining" chứa "Food")
                if (catName.contains(suggested) || suggested.contains(catName)) {
                    return cat.getId();
                }
                
                // Fuzzy match cho các category phổ biến
                if ((suggested.contains("food") || suggested.contains("ăn") || suggested.contains("uống")) && 
                    (catName.contains("food") || catName.contains("dining"))) {
                    return cat.getId();
                }
                
                if ((suggested.contains("bills") || suggested.contains("điện") || suggested.contains("utility")) && 
                    (catName.contains("bill") || catName.contains("utility"))) {
                    return cat.getId();
                }
                
                if ((suggested.contains("transport") || suggested.contains("xăng") || suggested.contains("xe")) && 
                    (catName.contains("transport"))) {
                    return cat.getId();
                }
                
                if ((suggested.contains("shopping") || suggested.contains("mua")) && 
                    (catName.contains("shopping"))) {
                    return cat.getId();
                }
                
                if ((suggested.contains("salary") || suggested.contains("lương")) && 
                    (catName.contains("salary"))) {
                    return cat.getId();
                }
            }
        }
        
        // Fallback: lấy category đầu tiên của type
        for (CategoryResponse cat : categories) {
            if (cat.getType() != null && cat.getType().equals(transactionType)) {
                return cat.getId();
            }
        }
        
        return null;
    }
    
    private String formatAmount(BigDecimal amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(amount.longValue()) + " đ";
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerViewChat.post(() -> 
                recyclerViewChat.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }
}

