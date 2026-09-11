package com.example.inzightapp.ai;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Transaction Extractor - Phân tích tin nhắn SMS/notification để trích xuất thông tin giao dịch
 * Giống như tính năng của MoMo
 */
public class TransactionAIExtractor {

    // Pattern để nhận diện các ngân hàng/ví điện tử phổ biến
    private static final Pattern[] BANK_PATTERNS = {
        Pattern.compile("(MoMo|MOMO)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(Vietcombank|VCB)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(Techcombank|TCB)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(BIDV)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(Agribank)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(Vietinbank|VTB)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(ZaloPay)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(VNPAY)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(TPBank)", Pattern.CASE_INSENSITIVE)
    };

    // Pattern để trích xuất số tiền - cải thiện cho chat (ví dụ: "40k", "100k", "1tr", "1,000,000")
    private static final Pattern[] AMOUNT_PATTERNS = {
        // "40k", "100k", "1tr", "2.5tr" - pattern cho chat (không cần space)
        Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*([ktr]|nghìn|triệu|ngàn)\\b", Pattern.CASE_INSENSITIVE),
        // "saving 5,000,000", "savings of 5000000", "currently saving 1,000,000"
        Pattern.compile("(?:saving|savings|saved|save|tiết kiệm|tiết kiệm được|đang tiết kiệm|đã tiết kiệm)[: ]*(?:of|là|là|bằng)?[: ]*(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)", Pattern.CASE_INSENSITIVE),
        // "tong 225,000", "total 225000"
        Pattern.compile("(?:tong|tổng|total|thanh toan|thanh toán|tổng cộng)[: ]*(?:cong|vnd|dong)?[: ]*(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)", Pattern.CASE_INSENSITIVE),
        // "225,000 VND", "225000 dong"
        Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s*(?:VND|dong|vnđ)", Pattern.CASE_INSENSITIVE),
        // "so tien: 225000", "amount: 5000000"
        Pattern.compile("(?:so tien|số tiền|amount)[: ]*(\\d{1,3}(?:[.,]\\d{3})*)", Pattern.CASE_INSENSITIVE),
        // "I have 5,000,000", "I am saving 1,000,000"
        Pattern.compile("(?:i am|i'm|i have|i currently|tôi đang|tôi có|tôi đã)[: ]*(?:saving|savings|saved|tiết kiệm)?[: ]*(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)", Pattern.CASE_INSENSITIVE),
        // Fallback: bất kỳ số nào >= 4 chữ số với dấu phẩy (5,000,000) hoặc >= 6 chữ số (1000000)
        Pattern.compile("\\b(\\d{1,3}(?:[.,]\\d{3}){2,}(?:[.,]\\d{2})?|\\d{6,})\\b", Pattern.CASE_INSENSITIVE)
    };

    // Từ khóa cho giao dịch thu (INCOME)
    private static final String[] INCOME_KEYWORDS = {
        "nhan duoc", "nhận được", "received", "chuyen khoan den", "chuyển khoản đến",
        "nap tien", "nạp tiền", "deposit", "thanh toan thanh cong", "thanh toán thành công",
        "hoan tien", "hoàn tiền", "refund", "tra lai", "trả lại", "luong", "lương",
        "salary", "tien thuong", "tiền thưởng", "bonus", "tang tien", "tặng tiền",
        "tiet kiem", "tiết kiệm", "savings", "so tien tiet kiem", "số tiền tiết kiệm",
        "tien tiet kiem cua toi", "tiền tiết kiệm của tôi", "my savings", "saving money",
        "saving", "saved", "i am saving", "i'm saving", "currently saving", "i have saved",
        "đang tiết kiệm", "đã tiết kiệm", "tiết kiệm được"
    };

    // Từ khóa cho giao dịch chi (EXPENSE) - mở rộng cho tiếng Việt
    private static final String[] EXPENSE_KEYWORDS = {
        "chi tieu", "chi tiêu", "expense", "thanh toan", "thanh toán", "payment",
        "chuyen khoan", "chuyển khoản", "transfer", "mua", "purchase", "buy",
        "rut tien", "rút tiền", "withdraw", "tru tien", "trừ tiền", "deduct",
        "tiền điện", "tiền nước", "tiền nhà", "tiền internet", "tiền wifi",
        "hóa đơn", "bill", "phí", "nộp", "trả", "đóng tiền", "tiền thuê"
    };

    // Từ khóa để nhận diện danh mục - cải thiện cho tiếng Việt
    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // Danh mục EXPENSE - ưu tiên theo thứ tự
        CATEGORY_KEYWORDS.put("an|ăn|uống|nhà hàng|restaurant|quán|foody|grab food|now|baemin|món|com|phở|bún|bánh|nước|coca|sprite|tonic|soda|đồ ăn|thức ăn|ăn uống", "Food & Dining");
        CATEGORY_KEYWORDS.put("tiền điện|điện|hóa đơn điện|hoadon dien|tiền nước|nước|utility|hóa đơn|bill|tiền internet|internet|tiền wifi|wifi|tiền nhà|nhà trọ|phí sinh hoạt", "Bills & Utilities");
        CATEGORY_KEYWORDS.put("xăng|gas|fuel|nhiên liệu|xe|car|taxi|uber|grab|xe máy|xe ô tô|đổ xăng|đổ dầu|dầu|parking|đỗ xe|bãi đỗ", "Transportation");
        CATEGORY_KEYWORDS.put("mua sắm|shopping|thời trang|fashion|clothes|quần áo|giày dép|đồ dùng|siêu thị|supermarket|market|chợ", "Shopping");
        CATEGORY_KEYWORDS.put("y tế|health|medical|hospital|bệnh viện|thuốc|medicine|pharmacy|nhà thuốc|khám bệnh|bác sĩ", "Healthcare");
        CATEGORY_KEYWORDS.put("giải trí|entertainment|movie|cinema|phim|xem phim|game|gaming|karaoke|nhạc|ca nhạc|concert", "Entertainment");
        CATEGORY_KEYWORDS.put("giáo dục|education|school|học phí|tuition|book|sách|học hành|sinh viên|trường học|đồ dùng học tập", "Education");
        CATEGORY_KEYWORDS.put("cafe|coffee|trà sữa|trà|drink|đồ uống|starbucks|highlands|the coffee house", "Food & Dining");
        
        // Danh mục INCOME
        CATEGORY_KEYWORDS.put("lương|salary|wage|tiền lương|lương tháng", "Salary");
        CATEGORY_KEYWORDS.put("thưởng|bonus|tiền thưởng|thưởng tháng", "Bonus");
        CATEGORY_KEYWORDS.put("kinh doanh|business|profit|lợi nhuận|doanh thu", "Business");
        CATEGORY_KEYWORDS.put("đầu tư|investment|dividend|cổ tức|lãi suất", "Investment");
        CATEGORY_KEYWORDS.put("quà|gift|tiền quà|tặng|nhận quà", "Gift");
        CATEGORY_KEYWORDS.put("tiết kiệm|tiet kiem|savings|số tiền tiết kiệm|so tien tiet kiem|tiền tiết kiệm của tôi|tien tiet kiem cua toi|my savings|saving money", "Savings");
    }

    /**
     * Phân tích tin nhắn và trích xuất thông tin giao dịch
     */
    public static ExtractedTransaction extract(String message) {
        return extractFromReceipt(message, null);
    }
    
    /**
     * Phân tích từ receipt/bill với thông tin chi tiết
     */
    public static ExtractedTransaction extractFromReceipt(String message, Object receiptInfo) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        String normalizedMessage = normalizeMessage(message);
        ExtractedTransaction transaction = new ExtractedTransaction();
        
        // Nếu có receiptInfo, sử dụng tổng tiền từ đó
        BigDecimal amount = null;
        if (receiptInfo instanceof ReceiptOCRHelper.ReceiptInfo) {
            ReceiptOCRHelper.ReceiptInfo receipt = (ReceiptOCRHelper.ReceiptInfo) receiptInfo;
            amount = receipt.getTotalAmount();
        }
        
        // Nếu không có từ receiptInfo, trích xuất từ message
        if (amount == null) {
            amount = extractAmount(normalizedMessage);
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // Không phải tin nhắn giao dịch
        }
        transaction.setAmount(amount);

        // Xác định loại giao dịch (INCOME hoặc EXPENSE)
        // Nếu là receipt, luôn là EXPENSE
        String type = "EXPENSE";
        if (receiptInfo == null) {
            type = determineTransactionType(normalizedMessage);
        }
        
        // Trích xuất danh mục - ưu tiên "Food & Dining" cho bill
        String category;
        if (receiptInfo != null) {
            // Bill thường là Food & Dining
            category = "Food & Dining";
            
            // Kiểm tra xem có keyword nào khác không
            String tempCategory = extractCategory(normalizedMessage, type);
            if (!tempCategory.equals("Other Expense")) {
                category = tempCategory;
            }
        } else {
            category = extractCategory(normalizedMessage, type);
            // Đảm bảo type phù hợp với category
            // Nếu category là EXPENSE nhưng type là INCOME, sửa lại
            if (type.equals("INCOME") && 
                !category.equals("Salary") && !category.equals("Bonus") && 
                !category.equals("Business") && !category.equals("Investment") && 
                !category.equals("Gift") && !category.equals("Savings") && !category.equals("Other Income")) {
                type = "EXPENSE";
            }
            // Nếu category là INCOME nhưng type là EXPENSE, sửa lại
            if (type.equals("EXPENSE") && 
                (category.equals("Salary") || category.equals("Bonus") || 
                 category.equals("Business") || category.equals("Investment") || 
                 category.equals("Gift") || category.equals("Savings"))) {
                type = "INCOME";
            }
        }
        
        transaction.setType(type);
        transaction.setSuggestedCategory(category);

        // Trích xuất ghi chú/ghi chú tự động
        String note = extractNote(normalizedMessage, amount, type);
        transaction.setNote(note);

        // Trích xuất ngày giờ (nếu có)
        Date transactionDate = extractDate(normalizedMessage);
        
        // Nếu có receiptInfo và có date, sử dụng date từ receipt
        if (receiptInfo instanceof ReceiptOCRHelper.ReceiptInfo) {
            ReceiptOCRHelper.ReceiptInfo receipt = (ReceiptOCRHelper.ReceiptInfo) receiptInfo;
            if (receipt.getDate() != null && !receipt.getDate().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    transactionDate = sdf.parse(receipt.getDate());
                } catch (ParseException e) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        transactionDate = sdf.parse(receipt.getDate());
                    } catch (ParseException e2) {
                        // Use current date
                    }
                }
            }
        }
        
        transaction.setTransactionDate(transactionDate != null ? transactionDate : new Date());

        // Trích xuất ngân hàng/ví
        String bank = extractBank(normalizedMessage);
        transaction.setBank(bank);

        return transaction;
    }

    /**
     * Chuẩn hóa tin nhắn: loại bỏ dấu, chuyển thành chữ thường
     */
    private static String normalizeMessage(String message) {
        return message.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s.,]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Trích xuất số tiền từ tin nhắn - hỗ trợ "k" (nghìn) và "tr" (triệu)
     */
    private static BigDecimal extractAmount(String message) {
        for (int i = 0; i < AMOUNT_PATTERNS.length; i++) {
            Pattern pattern = AMOUNT_PATTERNS[i];
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1);
                    
                    // Xử lý số có dấu phẩy (5,000,000) - remove commas
                    // Nếu có nhiều hơn 1 dấu phẩy, đó là thousand separator, remove tất cả
                    if (numberStr.contains(",") && numberStr.split(",").length > 2) {
                        numberStr = numberStr.replace(",", "");
                    } else if (numberStr.contains(",") && numberStr.contains(".")) {
                        // Có cả dấu phẩy và dấu chấm - dấu phẩy là thousand separator
                        numberStr = numberStr.replace(",", "");
                    } else if (numberStr.contains(",") && !numberStr.contains(".")) {
                        // Chỉ có dấu phẩy - kiểm tra xem có phải thousand separator không
                        // Nếu có 3 chữ số sau dấu phẩy cuối, đó là thousand separator
                        String[] parts = numberStr.split(",");
                        if (parts.length > 1 && parts[parts.length - 1].length() == 3) {
                            numberStr = numberStr.replace(",", "");
                        } else {
                            // Có thể là decimal separator, giữ lại
                            numberStr = numberStr.replace(",", ".");
                        }
                    }
                    
                    BigDecimal number = new BigDecimal(numberStr);
                    
                    // Pattern đầu tiên có multiplier (k, tr, nghìn, triệu)
                    if (i == 0 && matcher.groupCount() >= 2) {
                        String multiplier = matcher.group(2).toLowerCase();
                        BigDecimal multiplierValue;
                        
                        if (multiplier.equals("k") || multiplier.equals("nghìn") || multiplier.equals("ngàn")) {
                            multiplierValue = new BigDecimal("1000");
                        } else if (multiplier.equals("tr") || multiplier.equals("triệu")) {
                            multiplierValue = new BigDecimal("1000000");
                        } else {
                            multiplierValue = BigDecimal.ONE;
                        }
                        
                        number = number.multiply(multiplierValue);
                        // Nếu có multiplier, accept ngay (40k = 40,000 >= 5000)
                        return number;
                    }
                    
                    // Chỉ chấp nhận số tiền hợp lý (từ 5,000 VND trở lên)
                    // Vì trong chat người dùng thường nói "40k" = 40,000 hoặc "5,000,000"
                    if (number.compareTo(new BigDecimal("5000")) >= 0) {
                        return number;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua, thử pattern tiếp theo
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return null;
    }

    /**
     * Xác định loại giao dịch: INCOME hoặc EXPENSE
     * Cải thiện: kiểm tra category trước, sau đó mới kiểm tra keywords
     */
    private static String determineTransactionType(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Đếm từ khóa INCOME
        int incomeScore = 0;
        for (String keyword : INCOME_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                incomeScore++;
            }
        }
        
        // Đếm từ khóa EXPENSE
        int expenseScore = 0;
        for (String keyword : EXPENSE_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                expenseScore++;
            }
        }
        
        // Kiểm tra category keywords - nếu có category EXPENSE thì force EXPENSE
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (lowerMessage.contains(keyword)) {
                    String category = entry.getValue();
                    // Nếu category là EXPENSE, tăng điểm EXPENSE
                    if (!category.equals("Salary") && !category.equals("Bonus") && 
                        !category.equals("Business") && !category.equals("Investment") && 
                        !category.equals("Gift") && !category.equals("Savings")) {
                        expenseScore += 2; // Tăng điểm vì đây là expense category
                    } else {
                        // Category là INCOME
                        incomeScore += 2;
                    }
                }
            }
        }
        
        // Nếu có dấu + trước số tiền, thường là thu
        if (message.contains("+")) {
            incomeScore += 2;
        }
        
        // Nếu có dấu - trước số tiền, thường là chi
        if (message.contains("-")) {
            expenseScore += 2;
        }
        
        // Mặc định là EXPENSE nếu không có keyword nào (hầu hết giao dịch là chi tiêu)
        if (incomeScore == 0 && expenseScore == 0) {
            return "EXPENSE";
        }
        
        return incomeScore >= expenseScore ? "INCOME" : "EXPENSE";
    }

    /**
     * Trích xuất danh mục gợi ý
     */
    private static String extractCategory(String message, String type) {
        String lowerMessage = message.toLowerCase();
        
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (lowerMessage.contains(keyword)) {
                    // Kiểm tra xem danh mục có phù hợp với loại giao dịch không
                    String category = entry.getValue();
                    if (type.equals("INCOME") && 
                        (category.equals("Salary") || category.equals("Bonus") || 
                         category.equals("Business") || category.equals("Investment") || 
                         category.equals("Gift") || category.equals("Savings"))) {
                        return category;
                    } else if (type.equals("EXPENSE") && 
                               !category.equals("Salary") && !category.equals("Bonus") && 
                               !category.equals("Business") && !category.equals("Investment") && 
                               !category.equals("Gift") && !category.equals("Savings")) {
                        return category;
                    }
                }
            }
        }
        
        // Mặc định
        return type.equals("INCOME") ? "Other Income" : "Other Expense";
    }

    /**
     * Trích xuất ghi chú từ tin nhắn - cải thiện để lấy đầy đủ thông tin
     */
    private static String extractNote(String message, BigDecimal amount, String type) {
        StringBuilder note = new StringBuilder();
        
        // Lấy phần ngân hàng/ví
        String bank = extractBank(message);
        if (bank != null && !bank.isEmpty()) {
            note.append(bank).append(": ");
        }
        
        // Loại bỏ số tiền và các từ khóa không cần thiết
        // Cần loại bỏ cả "40k", "100k", "1tr" trong chat
        String cleanMessage = message
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*[ktr]\\b", "") // Loại bỏ "40k", "100k", "1tr"
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*(?:nghìn|triệu|ngàn)\\b", "") // Loại bỏ "40 nghìn", "1 triệu"
                .replaceAll("\\d{1,3}(?:[.,]\\d{3})*", "") // Loại bỏ số thông thường
                .replaceAll("\\s*[ktr]\\b", "") // Loại bỏ k, tr đơn lẻ
                .replaceAll("vnd|dong|vnđ", "")
                .replaceAll("(?:so tien|số tiền|amount)[: ]*", "")
                .replaceAll("(?:tong|tổng|total|thanh toan|thanh toán|tổng cộng)[: ]*", "")
                .replaceAll("\\s+", " ") // Loại bỏ nhiều space
                .trim();
        
        // Không giới hạn độ dài - lấy toàn bộ nội dung có ý nghĩa
        // Chỉ loại bỏ các từ không cần thiết, giữ lại thông tin chi tiết
        if (!cleanMessage.isEmpty()) {
            note.append(cleanMessage);
        } else {
            note.append(type.equals("INCOME") ? "Thu nhập" : "Chi tiêu");
        }
        
        return note.toString();
    }

    /**
     * Trích xuất tên ngân hàng/ví
     */
    private static String extractBank(String message) {
        String upperMessage = message.toUpperCase();
        for (Pattern pattern : BANK_PATTERNS) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Trích xuất ngày giờ giao dịch từ tin nhắn
     */
    private static Date extractDate(String message) {
        // Pattern cho các định dạng ngày phổ biến
        String[] datePatterns = {
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        };
        
        // Pattern regex để tìm ngày
        Pattern datePattern = Pattern.compile("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}");
        Matcher matcher = datePattern.matcher(message);
        
        if (matcher.find()) {
            String dateStr = matcher.group();
            for (String pattern : datePatterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                    Date date = sdf.parse(dateStr);
                    if (date != null) {
                        return date;
                    }
                } catch (ParseException e) {
                    // Thử pattern tiếp theo
                }
            }
        }
        
        return null;
    }

    /**
     * Class chứa thông tin giao dịch đã trích xuất
     */
    public static class ExtractedTransaction {
        private BigDecimal amount;
        private String type; // "INCOME" hoặc "EXPENSE"
        private String suggestedCategory;
        private String note;
        private Date transactionDate;
        private String bank;

        // Getters và Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSuggestedCategory() { return suggestedCategory; }
        public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public Date getTransactionDate() { return transactionDate; }
        public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }

        public String getBank() { return bank; }
        public void setBank(String bank) { this.bank = bank; }
    }
}

