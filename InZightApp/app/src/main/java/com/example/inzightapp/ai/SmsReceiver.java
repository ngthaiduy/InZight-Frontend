package com.example.inzightapp.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BroadcastReceiver để nhận và xử lý SMS từ ngân hàng/ví điện tử
 */
public class SmsReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SmsReceiver";
    private static final String PREF_NAME = "AI_Transaction_Prefs";
    private static final String KEY_PROCESSED_SMS = "processed_sms_ids";
    private static final String KEY_AI_ENABLED = "ai_transaction_enabled";

    // Số điện thoại ngân hàng/ví điện tử phổ biến (có thể mở rộng)
    private static final Set<String> BANK_SENDER_PATTERNS = new HashSet<>();
    
    static {
        BANK_SENDER_PATTERNS.add("MoMo");
        BANK_SENDER_PATTERNS.add("MOMO");
        BANK_SENDER_PATTERNS.add("VIETCOMBANK");
        BANK_SENDER_PATTERNS.add("VCB");
        BANK_SENDER_PATTERNS.add("TECHCOMBANK");
        BANK_SENDER_PATTERNS.add("TCB");
        BANK_SENDER_PATTERNS.add("BIDV");
        BANK_SENDER_PATTERNS.add("AGRIBANK");
        BANK_SENDER_PATTERNS.add("VIETINBANK");
        BANK_SENDER_PATTERNS.add("ZALOPAY");
        BANK_SENDER_PATTERNS.add("VNPAY");
        BANK_SENDER_PATTERNS.add("TPBANK");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra xem tính năng AI có được bật không
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean aiEnabled = prefs.getBoolean(KEY_AI_ENABLED, true);
        
        if (!aiEnabled) {
            return; // Tính năng AI đã bị tắt
        }

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                
                for (SmsMessage message : messages) {
                    String sender = message.getDisplayOriginatingAddress();
                    String messageBody = message.getMessageBody();
                    long timestamp = message.getTimestampMillis();
                    
                    // Kiểm tra xem tin nhắn có phải từ ngân hàng/ví không
                    if (isFromBank(sender, messageBody)) {
                        processTransactionSms(context, sender, messageBody, timestamp);
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra xem tin nhắn có phải từ ngân hàng/ví điện tử không
     */
    private boolean isFromBank(String sender, String messageBody) {
        if (sender == null || messageBody == null) {
            return false;
        }
        
        String upperSender = sender.toUpperCase();
        String upperBody = messageBody.toUpperCase();
        
        // Kiểm tra sender
        for (String pattern : BANK_SENDER_PATTERNS) {
            if (upperSender.contains(pattern)) {
                return true;
            }
        }
        
        // Kiểm tra nội dung tin nhắn
        for (String pattern : BANK_SENDER_PATTERNS) {
            if (upperBody.contains(pattern)) {
                return true;
            }
        }
        
        // Kiểm tra các từ khóa tài chính
        String[] financialKeywords = {
            "CHUYEN KHOAN", "CHUYỂN KHOẢN", "NHAN TIEN", "NHẬN TIỀN",
            "SO DU", "SỐ DƯ", "GIAO DICH", "GIAO DỊCH", "THANH TOAN", "THANH TOÁN",
            "VND", "DONG", "ĐỒNG", "SO TIEN", "SỐ TIỀN"
        };
        
        for (String keyword : financialKeywords) {
            if (upperBody.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Xử lý tin nhắn giao dịch
     */
    private void processTransactionSms(Context context, String sender, String messageBody, long timestamp) {
        try {
            // Tạo ID duy nhất cho tin nhắn (sender + timestamp)
            String smsId = sender + "_" + timestamp;
            
            // Kiểm tra xem tin nhắn đã được xử lý chưa
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Set<String> processedSms = prefs.getStringSet(KEY_PROCESSED_SMS, new HashSet<>());
            
            if (processedSms.contains(smsId)) {
                Log.d(TAG, "SMS already processed: " + smsId);
                return;
            }
            
            // Phân tích tin nhắn bằng AI
            TransactionAIExtractor.ExtractedTransaction transaction = 
                TransactionAIExtractor.extract(messageBody);
            
            if (transaction == null) {
                Log.d(TAG, "Could not extract transaction from SMS");
                return;
            }
            
            // Lưu giao dịch vào SharedPreferences để hiển thị trong app
            saveExtractedTransaction(context, transaction, messageBody);
            
            // Đánh dấu tin nhắn đã xử lý
            processedSms.add(smsId);
            prefs.edit().putStringSet(KEY_PROCESSED_SMS, processedSms).apply();
            
            // Giới hạn số lượng SMS đã xử lý (giữ tối đa 100)
            if (processedSms.size() > 100) {
                List<String> list = new ArrayList<>(processedSms);
                Set<String> newSet = new HashSet<>(list.subList(list.size() - 100, list.size()));
                prefs.edit().putStringSet(KEY_PROCESSED_SMS, newSet).apply();
            }
            
            Log.d(TAG, "Transaction extracted: " + transaction.getAmount() + " " + transaction.getType());
            
            // Hiển thị notification (tùy chọn - có thể implement sau)
            // showNotification(context, transaction);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS", e);
        }
    }

    /**
     * Lưu giao dịch đã trích xuất vào SharedPreferences
     */
    private void saveExtractedTransaction(Context context, 
                                         TransactionAIExtractor.ExtractedTransaction transaction,
                                         String originalMessage) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Lấy danh sách giao dịch đang chờ
        String pendingTransactionsJson = prefs.getString("pending_transactions", "[]");
        // TODO: Implement JSON serialization nếu cần lưu nhiều giao dịch
        // Hiện tại lưu giao dịch mới nhất vào key riêng
        prefs.edit()
            .putString("latest_transaction_amount", transaction.getAmount().toString())
            .putString("latest_transaction_type", transaction.getType())
            .putString("latest_transaction_category", transaction.getSuggestedCategory())
            .putString("latest_transaction_note", transaction.getNote())
            .putLong("latest_transaction_date", transaction.getTransactionDate().getTime())
            .putString("latest_transaction_bank", transaction.getBank() != null ? transaction.getBank() : "")
            .putString("latest_transaction_message", originalMessage)
            .putBoolean("has_pending_transaction", true)
            .apply();
    }

    /**
     * Bật/tắt tính năng AI tự động
     */
    public static void setAiEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply();
    }

    /**
     * Kiểm tra xem tính năng AI có được bật không
     */
    public static boolean isAiEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AI_ENABLED, true);
    }
}

