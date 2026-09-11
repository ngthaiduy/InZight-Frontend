package com.example.inzightapp.utils;

import java.util.regex.Pattern;

/**
 * Utility class to detect language from text
 */
public class LanguageDetector {
    
    // Vietnamese character patterns
    private static final Pattern VIETNAMESE_PATTERN = Pattern.compile(
        "[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ]",
        Pattern.CASE_INSENSITIVE
    );
    
    // Common Vietnamese words
    private static final String[] VIETNAMESE_KEYWORDS = {
        "tôi", "bạn", "của", "và", "là", "một", "có", "không", "được", "với",
        "cho", "này", "đó", "như", "về", "từ", "sẽ", "đã", "đang", "hay",
        "tiền", "chi", "tiêu", "thu", "nhập", "tiết", "kiệm", "giao", "dịch",
        "ví", "tài", "khoản", "ngân", "hàng", "mua", "bán", "giá", "triệu", "nghìn"
    };
    
    /**
     * Detect if text is Vietnamese or English
     * @param text Input text
     * @return "vi" for Vietnamese, "en" for English
     */
    public static String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en"; // Default to English
        }
        
        String lowerText = text.toLowerCase();
        
        // Check for Vietnamese characters first (most reliable indicator)
        if (VIETNAMESE_PATTERN.matcher(text).find()) {
            return "vi";
        }
        
        // Check for common English words/phrases (if found, likely English)
        String[] englishIndicators = {
            "i am", "i'm", "i have", "i will", "i can", "i want", "i need",
            "how", "what", "when", "where", "why", "who", "which",
            "the", "a", "an", "is", "are", "was", "were", "be", "been",
            "saving", "spending", "expense", "income", "transaction",
            "currently", "today", "yesterday", "tomorrow", "this", "that"
        };
        
        int englishIndicatorCount = 0;
        for (String indicator : englishIndicators) {
            if (lowerText.contains(indicator)) {
                englishIndicatorCount++;
            }
        }
        
        // If found multiple English indicators, likely English
        if (englishIndicatorCount >= 2) {
            return "en";
        }
        
        // Check for Vietnamese keywords
        int vietnameseKeywordCount = 0;
        for (String keyword : VIETNAMESE_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                vietnameseKeywordCount++;
            }
        }
        
        // If found multiple Vietnamese keywords, likely Vietnamese
        if (vietnameseKeywordCount >= 2) {
            return "vi";
        }
        
        // Check for common Vietnamese phrases
        String[] vietnamesePhrases = {
            "bao nhiêu", "như thế nào", "là gì", "của tôi", "cho tôi",
            "hôm nay", "hôm qua", "ngày mai", "tuần này", "tháng này"
        };
        
        for (String phrase : vietnamesePhrases) {
            if (lowerText.contains(phrase)) {
                return "vi";
            }
        }
        
        // Default to English
        return "en";
    }
    
    /**
     * Get language instruction for AI prompt
     * @param language Detected language ("vi" or "en")
     * @return Instruction string to add to prompt
     */
    public static String getLanguageInstruction(String language) {
        if ("vi".equals(language)) {
            return "IMPORTANT: Bạn PHẢI trả lời bằng tiếng Việt. Không được trả lời bằng tiếng Anh. ";
        } else {
            return "IMPORTANT: You MUST respond in English. Do not respond in Vietnamese. ";
        }
    }
    
    /**
     * Get transaction extraction instruction for AI prompt
     * @param hasTransaction Whether a transaction was detected
     * @param language Detected language
     * @return Instruction string about transaction extraction
     */
    public static String getTransactionInstruction(boolean hasTransaction, String language) {
        if (hasTransaction) {
            if ("vi".equals(language)) {
                return "Người dùng đã cung cấp thông tin giao dịch. Hãy xác nhận và hỏi thêm thông tin nếu cần. ";
            } else {
                return "The user has provided transaction information. Please acknowledge and ask for additional details if needed. ";
            }
        }
        return "";
    }
}

