package com.example.inzightapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.ai.TransactionAIExtractor;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<ChatMessageResponse> messageList = new ArrayList<>();
    private Long currentUserId; // ID của user hiện tại để xác định tin nhắn gửi/nhận
    private boolean finbotMode = false; // nếu true, chỉ phân biệt Finbot và user
    private long finbotId = -999L;
    private Map<Long, TransactionAIExtractor.ExtractedTransaction> transactionMap = new HashMap<>();
    private OnTransactionSaveListener onTransactionSaveListener;
    private String currentLanguage = "en"; // Default to English

    public ChatMessageAdapter(Context context, Long currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
    }

    public void setFinbotMode(boolean finbotMode) {
        this.finbotMode = finbotMode;
        notifyDataSetChanged();
    }

    public void setFinbotId(long finbotId) {
        this.finbotId = finbotId;
        notifyDataSetChanged();
    }
    
    public void setCurrentLanguage(String language) {
        this.currentLanguage = language != null ? language : "en";
        notifyDataSetChanged(); // Refresh to update transaction card text
    }
    
    public void setPendingTransaction(TransactionAIExtractor.ExtractedTransaction transaction) {
        // Không dùng nữa, sẽ lưu theo message ID
    }
    
    public void setTransactionForMessage(Long messageId, TransactionAIExtractor.ExtractedTransaction transaction) {
        if (messageId != null && transaction != null) {
            transactionMap.put(messageId, transaction);
        }
    }
    
    public void setOnTransactionSaveListener(OnTransactionSaveListener listener) {
        this.onTransactionSaveListener = listener;
    }
    
    public interface OnTransactionSaveListener {
        void onSaveTransaction(TransactionAIExtractor.ExtractedTransaction transaction);
    }

    public void setMessageList(List<ChatMessageResponse> messages) {
        this.messageList = messages != null ? messages : new ArrayList<>();
        android.util.Log.d("ChatMessageAdapter", "setMessageList: Loading " + this.messageList.size() + " messages");
        
        // Log tất cả messages để debug
        for (int i = 0; i < this.messageList.size(); i++) {
            ChatMessageResponse msg = this.messageList.get(i);
            android.util.Log.d("ChatMessageAdapter", "Message[" + i + "] - ID: " + msg.getId() + 
                ", Sender: " + msg.getSenderId() + ", Receiver: " + msg.getReceiverId() + 
                ", Content: " + (msg.getContent() != null ? (msg.getContent().length() > 150 ? msg.getContent().substring(0, 150) + "..." : msg.getContent()) : "NULL"));
        }
        
        // Parse transaction cards từ messages và restore transaction data
        int cardCount = 0;
        for (ChatMessageResponse msg : this.messageList) {
            if (msg.getContent() != null && msg.getContent().startsWith("TRANSACTION_CARD:")) {
                cardCount++;
                android.util.Log.d("ChatMessageAdapter", "Found transaction card message ID: " + msg.getId() + ", content: " + msg.getContent().substring(0, Math.min(100, msg.getContent().length())));
                try {
                    // Parse JSON từ content
                    String jsonStr = msg.getContent().substring("TRANSACTION_CARD:".length());
                    Gson gson = new Gson();
                    JsonObject jsonData = gson.fromJson(jsonStr, JsonObject.class);
                    
                    android.util.Log.d("ChatMessageAdapter", "Parsed JSON: " + jsonData.toString());
                    
                    // Tạo ExtractedTransaction từ JSON
                    TransactionAIExtractor.ExtractedTransaction transaction = parseTransactionFromJson(jsonData);
                    
                    if (transaction != null && transaction.getAmount() != null) {
                        // Store transaction data với message ID
                        setTransactionForMessage(msg.getId(), transaction);
                        android.util.Log.d("ChatMessageAdapter", "✅ Restored transaction card ID: " + msg.getId() + ", amount: " + transaction.getAmount());
                    } else {
                        android.util.Log.e("ChatMessageAdapter", "❌ Failed to parse transaction or amount is null");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ChatMessageAdapter", "❌ Error parsing transaction card from message ID: " + msg.getId(), e);
                }
            }
        }
        
        android.util.Log.d("ChatMessageAdapter", "Total transaction cards found: " + cardCount + ", restored to map: " + transactionMap.size());
        android.util.Log.d("ChatMessageAdapter", "Transaction map keys: " + transactionMap.keySet().toString());
        
        notifyDataSetChanged();
    }
    
    /**
     * Parse ExtractedTransaction từ JSON
     */
    private TransactionAIExtractor.ExtractedTransaction parseTransactionFromJson(JsonObject json) {
        try {
            TransactionAIExtractor.ExtractedTransaction transaction = new TransactionAIExtractor.ExtractedTransaction();
            
            if (json.has("amount")) {
                transaction.setAmount(new java.math.BigDecimal(json.get("amount").getAsString()));
            }
            
            if (json.has("type")) {
                transaction.setType(json.get("type").getAsString());
            }
            
            if (json.has("category")) {
                transaction.setSuggestedCategory(json.get("category").getAsString());
            }
            
            if (json.has("note")) {
                transaction.setNote(json.get("note").getAsString());
            }
            
            if (json.has("date")) {
                try {
                    java.time.Instant instant = java.time.Instant.parse(json.get("date").getAsString());
                    transaction.setTransactionDate(java.util.Date.from(instant));
                } catch (Exception e) {
                    transaction.setTransactionDate(new java.util.Date());
                }
            } else {
                transaction.setTransactionDate(new java.util.Date());
            }
            
            return transaction;
        } catch (Exception e) {
            android.util.Log.e("ChatMessageAdapter", "Error parsing transaction from JSON", e);
            return null;
        }
    }

    public void addMessage(ChatMessageResponse message) {
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        
        // Nếu là transaction card, parse và restore transaction data
        if (message.getContent() != null && message.getContent().startsWith("TRANSACTION_CARD:")) {
            try {
                String jsonStr = message.getContent().substring("TRANSACTION_CARD:".length());
                Gson gson = new Gson();
                JsonObject jsonData = gson.fromJson(jsonStr, JsonObject.class);
                TransactionAIExtractor.ExtractedTransaction transaction = parseTransactionFromJson(jsonData);
                if (transaction != null) {
                    setTransactionForMessage(message.getId(), transaction);
                }
            } catch (Exception e) {
                android.util.Log.e("ChatMessageAdapter", "Error parsing transaction card in addMessage", e);
            }
        }
        
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    /**
     * Kiểm tra xem message với id này đã tồn tại chưa (tránh duplicate)
     */
    public boolean hasMessage(Long messageId) {
        if (messageId == null || messageList == null) return false;
        for (ChatMessageResponse msg : messageList) {
            if (messageId.equals(msg.getId())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update message ID trong adapter (khi message được lưu vào database và có ID mới)
     */
    public void updateMessageId(Long oldId, Long newId) {
        if (oldId == null || newId == null || messageList == null) return;
        for (int i = 0; i < messageList.size(); i++) {
            ChatMessageResponse msg = messageList.get(i);
            if (oldId.equals(msg.getId())) {
                msg.setId(newId);
                notifyItemChanged(i);
                android.util.Log.d("ChatMessageAdapter", "Updated message ID from " + oldId + " to " + newId);
                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= messageList.size()) {
            return 0;
        }
        ChatMessageResponse message = messageList.get(position);
        // Nếu là transaction card (format: "TRANSACTION_CARD" hoặc "TRANSACTION_CARD:{json}")
        if (message.getContent() != null && 
            (message.getContent().equals("TRANSACTION_CARD") || message.getContent().startsWith("TRANSACTION_CARD:"))) {
            android.util.Log.d("ChatMessageAdapter", "getItemViewType: Position " + position + " is TRANSACTION_CARD (ID: " + message.getId() + ")");
            return 1; // Transaction card type
        }
        return 0; // Normal message type
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            // Transaction card view
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_transaction_card, parent, false);
            return new TransactionCardViewHolder(view);
        } else {
            // Normal message view
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageResponse message = messageList.get(position);
        
        if (holder instanceof TransactionCardViewHolder) {
            // Bind transaction card
            TransactionCardViewHolder cardHolder = (TransactionCardViewHolder) holder;
            TransactionAIExtractor.ExtractedTransaction transaction = transactionMap.get(message.getId());
            
            android.util.Log.d("ChatMessageAdapter", "onBindViewHolder TransactionCard - Message ID: " + message.getId() + ", transaction in map: " + (transaction != null ? "YES" : "NO"));
            
            // Nếu chưa có trong map, parse từ content (trường hợp load từ DB)
            if (transaction == null && message.getContent() != null && message.getContent().startsWith("TRANSACTION_CARD:")) {
                android.util.Log.d("ChatMessageAdapter", "Transaction not in map, parsing from content...");
                try {
                    String jsonStr = message.getContent().substring("TRANSACTION_CARD:".length());
                    Gson gson = new Gson();
                    JsonObject jsonData = gson.fromJson(jsonStr, JsonObject.class);
                    transaction = parseTransactionFromJson(jsonData);
                    if (transaction != null && transaction.getAmount() != null) {
                        setTransactionForMessage(message.getId(), transaction);
                        android.util.Log.d("ChatMessageAdapter", "✅ Parsed and restored transaction from content, amount: " + transaction.getAmount());
                    } else {
                        android.util.Log.e("ChatMessageAdapter", "❌ Failed to parse transaction from content or amount is null");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ChatMessageAdapter", "❌ Error parsing transaction card in onBindViewHolder", e);
                }
            }
            
            if (transaction != null && transaction.getAmount() != null) {
                bindTransactionCard(cardHolder, transaction, message.getId());
                android.util.Log.d("ChatMessageAdapter", "✅ Bound transaction card successfully");
            } else {
                android.util.Log.e("ChatMessageAdapter", "❌ Cannot bind: transaction is null or amount is null. Message ID: " + message.getId() + ", Content: " + (message.getContent() != null ? message.getContent().substring(0, Math.min(100, message.getContent().length())) : "null"));
                // Hiển thị message lỗi trong card (tạm thời để debug)
                cardHolder.tvAmount.setText("Error loading transaction");
            }
        } else if (holder instanceof MessageViewHolder) {
            // Bind normal message
            MessageViewHolder msgHolder = (MessageViewHolder) holder;
            bindNormalMessage(msgHolder, message);
        }
    }
    
    private void bindNormalMessage(MessageViewHolder holder, ChatMessageResponse message) {
        // Xác định tin nhắn là gửi đi hay nhận được
        boolean isSent;
        if (finbotMode) {
            // Trong finbot mode: Finbot bên trái, user bên phải
            Long senderId = message.getSenderId();
            if (senderId != null && senderId.equals(Long.valueOf(finbotId))) {
                isSent = false; // Finbot luôn bên trái
            } else {
                isSent = true;  // User hiện tại bên phải (bao gồm cả null senderId)
            }
            android.util.Log.d("ChatMessageAdapter", "bindNormalMessage finbotMode - Sender: " + senderId + 
                ", finbotId: " + finbotId + ", isSent: " + isSent + ", content: " + 
                (message.getContent() != null ? (message.getContent().length() > 50 ? message.getContent().substring(0, 50) + "..." : message.getContent()) : "NULL"));
        } else {
            isSent = currentUserId != null && message.getSenderId() != null
                    && message.getSenderId().equals(currentUserId);
        }

        if (isSent) {
            // Tin nhắn gửi đi - hiển thị bên phải
            holder.layoutSent.setVisibility(View.VISIBLE);
            holder.layoutReceived.setVisibility(View.GONE);
            holder.tvSentMessage.setText(message.getContent() != null ? message.getContent() : "");
        } else {
            // Tin nhắn nhận được - hiển thị bên trái
            holder.layoutReceived.setVisibility(View.VISIBLE);
            holder.layoutSent.setVisibility(View.GONE);
            holder.tvReceivedMessage.setText(message.getContent() != null ? message.getContent() : "");
        }
    }
    
    private void bindTransactionCard(TransactionCardViewHolder holder, TransactionAIExtractor.ExtractedTransaction transaction, Long messageId) {
        // Hiển thị số tiền
        if (transaction.getAmount() != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String amountText = formatter.format(transaction.getAmount().longValue()) + " đ";
            holder.tvAmount.setText(amountText);
            
            // Màu sắc cho Income (xanh lá) và Expense (đỏ/cam)
            if (transaction.getType() != null && transaction.getType().equals("INCOME")) {
                holder.tvAmount.setTextColor(0xFF4CAF50); // Green for income
            } else {
                holder.tvAmount.setTextColor(0xFFFF5722); // Orange/Red for expense
            }
        }
        
        // Hiển thị ghi chú
        if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
            holder.tvNote.setText(transaction.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }
        
        // Hiển thị type với màu sắc phù hợp - theo ngôn ngữ hiện tại
        String typeText;
        int typeColor = 0xFFFF5722; // Orange/Red for expense
        if (transaction.getType() != null && transaction.getType().equals("INCOME")) {
            typeText = "vi".equals(currentLanguage) ? "Thu nhập" : "Income";
            typeColor = 0xFF4CAF50; // Green for income
        } else {
            typeText = "vi".equals(currentLanguage) ? "Chi tiêu" : "Expense";
        }
        holder.tvType.setText(typeText);
        
        // Set background color cho type badge
        if (holder.layoutTypeBadge != null) {
            holder.layoutTypeBadge.setBackgroundColor(typeColor);
        }
        
        // Hiển thị category
        if (transaction.getSuggestedCategory() != null && !transaction.getSuggestedCategory().isEmpty()) {
            holder.tvCategory.setText(transaction.getSuggestedCategory());
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }
        
        // Hiển thị ngày (format ngắn gọn: dd/MM)
        if (transaction.getTransactionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            holder.tvDate.setText(sdf.format(transaction.getTransactionDate()));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            // Hiển thị ngày hiện tại
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new java.util.Date()));
            holder.tvDate.setVisibility(View.VISIBLE);
        }
        
        // Button save - Ẩn đi vì đã tự động lưu
        holder.btnSave.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSent, layoutReceived;
        TextView tvSentMessage, tvReceivedMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutSent = itemView.findViewById(R.id.layoutSent);
            layoutReceived = itemView.findViewById(R.id.layoutReceived);
            tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
            tvReceivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
        }
    }
    
    static class TransactionCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvType, tvNote, tvCategory, tvDate;
        Button btnSave;
        View layoutTypeBadge;

        public TransactionCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnSave = itemView.findViewById(R.id.btnSave);
            layoutTypeBadge = itemView.findViewById(R.id.layoutTypeBadge);
        }
    }
}

