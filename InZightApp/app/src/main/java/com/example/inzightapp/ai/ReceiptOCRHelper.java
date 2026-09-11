package com.example.inzightapp.ai;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.Text;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class để OCR (Optical Character Recognition) từ hình ảnh hóa đơn
 */
public class ReceiptOCRHelper {
    
    private static final String TAG = "ReceiptOCR";
    private TextRecognizer recognizer;
    
    public interface OCRCallback {
        void onSuccess(String extractedText);
        void onError(String error);
    }
    
    public ReceiptOCRHelper() {
        // Khởi tạo Text Recognizer (hỗ trợ tiếng Việt và tiếng Anh)
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    
    /**
     * OCR từ Bitmap
     */
    public void recognizeText(Bitmap bitmap, OCRCallback callback) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            recognizeTextFromImage(image, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage from bitmap", e);
            if (callback != null) {
                callback.onError("Lỗi xử lý hình ảnh: " + e.getMessage());
            }
        }
    }
    
    /**
     * OCR từ InputImage
     */
    private void recognizeTextFromImage(InputImage image, OCRCallback callback) {
        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String extractedText = visionText.getText();
                        Log.d(TAG, "OCR Text extracted: " + extractedText);
                        
                        if (extractedText != null && !extractedText.trim().isEmpty()) {
                            if (callback != null) {
                                callback.onSuccess(extractedText);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Không thể đọc được chữ từ hình ảnh");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error in OCR", e);
                        if (callback != null) {
                            callback.onError("Lỗi OCR: " + e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * Phân tích văn bản từ OCR và trích xuất thông tin giao dịch
     * Đơn giản hóa: chỉ lấy tổng tiền, ngày, và phân tích category từ nội dung
     */
    public TransactionAIExtractor.ExtractedTransaction extractTransactionFromReceipt(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            Log.w(TAG, "OCR text is null or empty");
            return null;
        }
        
        Log.d(TAG, "Starting extraction. OCR text length: " + ocrText.length());
        
        try {
            // Trích xuất thông tin cơ bản: tổng tiền và ngày
            ReceiptInfo receiptInfo = extractReceiptInfo(ocrText);
            
            // Kiểm tra có tổng tiền không
            if (receiptInfo.getTotalAmount() == null) {
                Log.w(TAG, "Cannot find total amount in receipt. OCR text preview: " + 
                    ocrText.substring(0, Math.min(300, ocrText.length())));
                // Thử tìm lại bằng cách tìm số lớn nhất
                BigDecimal largestAmount = findLargestAmount(ocrText);
                if (largestAmount != null) {
                    receiptInfo.setTotalAmount(largestAmount);
                    Log.d(TAG, "Using largest amount found: " + largestAmount);
                } else {
                    Log.e(TAG, "Cannot find any amount in receipt");
                    return null;
                }
            } else {
                Log.d(TAG, "Found total amount: " + receiptInfo.getTotalAmount());
            }
            
            // Tạo transaction
            TransactionAIExtractor.ExtractedTransaction transaction = 
                new TransactionAIExtractor.ExtractedTransaction();
            
            // Số tiền
            transaction.setAmount(receiptInfo.getTotalAmount());
            
            // Luôn là EXPENSE (bill thanh toán)
            transaction.setType("EXPENSE");
            
            // Phân tích category từ nội dung OCR
            String category = analyzeCategoryFromText(ocrText);
            transaction.setSuggestedCategory(category);
            
            // Ngày
            Date transactionDate = new Date(); // Mặc định là hôm nay
            if (receiptInfo.getDate() != null && !receiptInfo.getDate().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    transactionDate = sdf.parse(receiptInfo.getDate());
                } catch (ParseException e) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        transactionDate = sdf.parse(receiptInfo.getDate());
                    } catch (ParseException e2) {
                        // Dùng ngày hiện tại
                        transactionDate = new Date();
                    }
                }
            }
            transaction.setTransactionDate(transactionDate);
            
            // Ghi chú: Tên nhà hàng + món (nếu có)
            String note = buildReceiptNote(receiptInfo);
            if (note == null || note.isEmpty()) {
                // Nếu không có note, tạo note dựa trên category
                note = createNoteFromCategory(category, receiptInfo);
            }
            transaction.setNote(note);
            
            return transaction;
        } catch (Exception e) {
            Log.e(TAG, "Error in extractTransactionFromReceipt", e);
            return null;
        }
    }
    
    /**
     * Tạo ghi chú dựa trên category nếu không có thông tin từ bill
     */
    private String createNoteFromCategory(String category, ReceiptInfo info) {
        String baseNote = "";
        
        switch (category) {
            case "Food & Dining":
                baseNote = info.getRestaurantName() != null ? info.getRestaurantName() : "Ăn uống";
                break;
            case "Bills & Utilities":
                baseNote = "Hóa đơn tiện ích";
                break;
            case "Transportation":
                baseNote = "Chi phí đi lại";
                break;
            case "Shopping":
                baseNote = "Mua sắm";
                break;
            case "Healthcare":
                baseNote = "Y tế";
                break;
            case "Entertainment":
                baseNote = "Giải trí";
                break;
            case "Education":
                baseNote = "Giáo dục";
                break;
            default:
                baseNote = "Chi tiêu khác";
        }
        
        return baseNote;
    }
    
    /**
     * Class chứa thông tin chi tiết từ bill
     */
    public static class ReceiptInfo {
        private String restaurantName;
        private List<String> items;
        private BigDecimal totalAmount;
        private String date;
        private String address;
        
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        
        public List<String> getItems() { return items != null ? items : new ArrayList<>(); }
        public void setItems(List<String> items) { this.items = items; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    /**
     * Trích xuất thông tin cơ bản từ bill: Tổng tiền và Ngày
     * Đơn giản hóa để chỉ lấy thông tin cần thiết
     */
    private ReceiptInfo extractReceiptInfo(String ocrText) {
        ReceiptInfo info = new ReceiptInfo();
        List<String> items = new ArrayList<>();
        
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return info;
        }
        
        try {
            String[] lines = ocrText.split("\n");
            BigDecimal totalAmount = null;
            String extractedDate = null;
            String restaurantName = null;
        
        // Pattern để tìm tổng tiền - cải thiện cho bill Việt Nam
        // Thứ tự ưu tiên: TIỀN MẶT > T.Cộng > Tổng cộng > Total
        Pattern[] totalPatterns = {
            // "TIỀN MẶT 225,000" - ưu tiên cao nhất (tiền thanh toán thực tế)
            Pattern.compile("(?:tiền\\s*mặt|tiền mat|cash)[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE),
            // "T.Cộng 225,000" hoặc "T.CỘNG 225,000" - tổng cộng
            Pattern.compile("t\\.?cộng[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE),
            // "Tổng cộng: 225,000" hoặc "TỔNG CỘNG: 225,000"
            Pattern.compile("(?:tong\\s*cong|tổng\\s*cộng|tong\\s*cộng)[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE),
            // "Total: 225,000" hoặc "TOTAL: 225,000"
            Pattern.compile("(?:total|tong)[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE),
            // "Total Price: 225,000" 
            Pattern.compile("(?:total\\s*price|tong\\s*price|tổng\\s*giá)[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE),
            // "Thanh toán: 225,000"
            Pattern.compile("(?:thanh\\s*toan|thanh\\s*toán|thanh\\s*toàn)[: ]*[=:]?[ ]*(\\d{1,3}(?:[.,]\\d{3})+)", Pattern.CASE_INSENSITIVE)
        };
        
        // Pattern để tìm tên nhà hàng (thường ở dòng đầu, chữ in hoa)
        Pattern restaurantPattern = Pattern.compile(
            "^([A-Z][A-Z0-9\\s&.,'-]{2,45})$", Pattern.MULTILINE
        );
        
        boolean foundItems = false;
        int lineIndex = 0;
        boolean foundRestaurantName = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            
            // Tìm tên nhà hàng (thường ở 5 dòng đầu, toàn chữ in hoa)
            if (lineIndex < 5 && restaurantName == null) {
                // Kiểm tra nếu dòng chứa chủ yếu chữ in hoa (tên nhà hàng)
                String upperTrimmed = trimmed.toUpperCase();
                if (trimmed.equals(upperTrimmed) || trimmed.matches("^[A-Z][A-Za-z].*")) {
                    // Loại bỏ các từ không phải tên nhà hàng
                    String lowerTrimmed = trimmed.toLowerCase();
                    if (!lowerTrimmed.matches(".*(hóa đơn|invoice|receipt|bill|payment|thanh toán|địa chỉ|address|đt|phone|số|ngày|date|giờ|time|bàn|table|phòng|room|khách hàng|customer|thu ngân|cashier|vip).*")) {
                        // Loại bỏ số điện thoại và địa chỉ
                        if (!trimmed.matches(".*\\d{3}[.\\s]\\d{3}[.\\s]\\d{4}.*")) {
                            if (trimmed.length() >= 3 && trimmed.length() <= 50) {
                                restaurantName = trimmed;
                                info.setRestaurantName(restaurantName);
                            }
                        }
                    }
                }
            }
            
            // Tìm tổng tiền - thử nhiều pattern (ưu tiên theo thứ tự trong mảng)
            for (int i = 0; i < totalPatterns.length; i++) {
                Pattern totalPattern = totalPatterns[i];
                Matcher totalMatcher = totalPattern.matcher(trimmed);
                if (totalMatcher.find()) {
                    try {
                        String amountStr = totalMatcher.group(1).replaceAll("[.,]", "");
                        BigDecimal foundAmount = new BigDecimal(amountStr);
                        
                        // Chỉ chấp nhận số tiền hợp lý (từ 5,000 VND trở lên)
                        if (foundAmount.compareTo(new BigDecimal("5000")) >= 0) {
                            // Ưu tiên pattern đầu tiên (TIỀN MẶT) hoặc số lớn hơn
                            if (totalAmount == null || i == 0 || foundAmount.compareTo(totalAmount) > 0) {
                                totalAmount = foundAmount;
                                Log.d(TAG, "Found total amount: " + totalAmount + " using pattern index: " + i);
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing amount from pattern", e);
                        // Ignore, thử pattern tiếp theo
                    }
                }
            }
            
            // Tìm các món ăn/item - cải thiện pattern cho bill Việt Nam
            // Format thường: "Coca                2    25,000    50,000"
            // Hoặc: "Coca x2 25,000" hoặc "Coca - 25,000"
            Pattern[] itemPatterns = {
                // Pattern 1: Tên món + số lượng + đơn giá + tổng (bill có cột)
                Pattern.compile("^([A-Za-zÀ-ỹ0-9\\s&.-]{3,35})\\s+\\d+\\s+\\d{1,3}(?:[.,]\\d{3})*\\s+(\\d{1,3}(?:[.,]\\d{3})*)$", Pattern.CASE_INSENSITIVE),
                // Pattern 2: Tên món + số lượng + giá
                Pattern.compile("^([A-Za-zÀ-ỹ0-9\\s&.-]{3,35})\\s+(?:x\\s*)?\\d+\\s+\\d{1,3}(?:[.,]\\d{3})*", Pattern.CASE_INSENSITIVE),
                // Pattern 3: Tên món + giá (đơn giản)
                Pattern.compile("^([A-Za-zÀ-ỹ0-9\\s&.-]{3,35})\\s+(?:[-:]\\s*)?\\d{1,3}(?:[.,]\\d{3})+", Pattern.CASE_INSENSITIVE)
            };
            
            // Loại bỏ các dòng header/tiêu đề và tổng
            String lowerTrimmed = trimmed.toLowerCase();
            boolean isHeader = lowerTrimmed.matches(".*(tên hàng|item name|sl|quantity|đơn giá|unit price|t\\.tiền|total price|tong|tổng|total|cộng|cong|bàn|table|phòng|room|ngày|date|số hđ|invoice).*");
            
            if (!isHeader && !lowerTrimmed.matches(".*(tong|total|tax|vat|tip|thue|phí|dịch vụ|cám ơn|thank|cảm ơn|hẹn gặp).*")) {
                for (Pattern itemPattern : itemPatterns) {
                    Matcher itemMatcher = itemPattern.matcher(trimmed);
                    if (itemMatcher.find()) {
                        String itemName = itemMatcher.group(1).trim();
                        
                        // Loại bỏ các ký tự không cần thiết và số
                        itemName = itemName.replaceAll("\\d+[.,]?\\d*", "").trim();
                        itemName = itemName.replaceAll("^[\\s\\-:]+|[\\s\\-:]+$", ""); // Loại bỏ dấu - : ở đầu cuối
                        
                        // Chỉ lấy tên item hợp lệ
                        if (itemName.length() >= 2 && itemName.length() <= 40 && items.size() < 15) {
                            // Kiểm tra không trùng
                            if (!items.contains(itemName)) {
                                items.add(itemName);
                                foundItems = true;
                            }
                            break; // Tìm thấy rồi thì không cần thử pattern khác
                        }
                    }
                }
            }
            
            // Tìm địa chỉ (thường chứa số nhà, đường)
            if (trimmed.matches(".*\\d+.*(?:đường|street|road|phố).*") && info.getAddress() == null) {
                info.setAddress(trimmed);
            }
            
            // Tìm ngày (dd/mm/yyyy hoặc dd-mm-yyyy) - ưu tiên dòng có "Ngày" hoặc "Date"
            if (extractedDate == null) {
                // Tìm trong dòng có "Ngày" hoặc "Date" trước
                lowerTrimmed = trimmed.toLowerCase();
                if (lowerTrimmed.contains("ngày") || lowerTrimmed.contains("date") || lowerTrimmed.contains("ngay")) {
                    Pattern datePattern = Pattern.compile("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}");
                    Matcher dateMatcher = datePattern.matcher(trimmed);
                    if (dateMatcher.find()) {
                        extractedDate = dateMatcher.group();
                        info.setDate(extractedDate);
                        Log.d(TAG, "Found date: " + extractedDate);
                    }
                }
            }
            
            lineIndex++;
        }
        
        // Tìm một số items để dùng cho note (không cần nhiều)
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            
            String lowerTrimmed = trimmed.toLowerCase();
            // Loại bỏ header và tổng
            if (lowerTrimmed.matches(".*(tên hàng|item|sl|quantity|đơn giá|tong|tổng|total|cộng|ngày|date).*")) {
                continue;
            }
            
            // Tìm dòng có thể là item (có chữ và số)
            if (trimmed.matches(".*[A-Za-zÀ-ỹ].*\\d+.*") && items.size() < 5) {
                // Lấy phần chữ (tên món)
                String itemName = trimmed.replaceAll("\\d+[.,]?\\d*", "").trim();
                itemName = itemName.replaceAll("^[\\s\\-:]+|[\\s\\-:]+$", "");
                if (itemName.length() >= 2 && itemName.length() <= 30) {
                    if (!items.contains(itemName)) {
                        items.add(itemName);
                    }
                }
            }
        }
        
        info.setItems(items);
        
        // Tìm ngày nếu chưa tìm thấy (tìm trong toàn bộ text)
        if (extractedDate == null) {
            Pattern datePattern = Pattern.compile("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}");
            Matcher dateMatcher = datePattern.matcher(ocrText);
            if (dateMatcher.find()) {
                extractedDate = dateMatcher.group();
                info.setDate(extractedDate);
                Log.d(TAG, "Found date in full text: " + extractedDate);
            }
        }
        
        // Xử lý tổng tiền
        if (totalAmount != null) {
            info.setTotalAmount(totalAmount);
            
            // Kiểm tra xem có tổng lớn hơn không (nếu có nhiều tổng, ví dụ: tiền giờ + tiền đồ uống)
            BigDecimal finalTotal = findLargestAmount(ocrText);
            if (finalTotal != null && finalTotal.compareTo(totalAmount) > 0) {
                // Nếu số lớn hơn nhiều (có thể là tổng thực tế), ưu tiên số lớn hơn
                // Nhưng chỉ nếu nó hợp lý (không quá 5 lần)
                if (finalTotal.compareTo(totalAmount.multiply(new BigDecimal("5"))) <= 0 && 
                    finalTotal.compareTo(new BigDecimal("1000000")) <= 0) { // Không quá 1 triệu (tránh lỗi)
                    // Ưu tiên số lớn hơn nếu nó gần với "TIỀN MẶT" hoặc "T.CỘNG" cuối cùng
                    String lowerText = ocrText.toLowerCase();
                    int lastCashIndex = Math.max(
                        lowerText.lastIndexOf("tiền mặt"),
                        Math.max(lowerText.lastIndexOf("t.cộng"), lowerText.lastIndexOf("tong cong"))
                    );
                    if (lastCashIndex >= 0) {
                        // Tìm số sau vị trí "TIỀN MẶT" hoặc "T.CỘNG" cuối
                        String afterCash = ocrText.substring(lastCashIndex);
                        Pattern afterCashPattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+)");
                        Matcher afterCashMatcher = afterCashPattern.matcher(afterCash);
                        if (afterCashMatcher.find()) {
                            try {
                                String amountStr = afterCashMatcher.group(1).replaceAll("[.,]", "");
                                BigDecimal cashAmount = new BigDecimal(amountStr);
                                if (cashAmount.compareTo(new BigDecimal("5000")) >= 0) {
                                    info.setTotalAmount(cashAmount);
                                    Log.d(TAG, "Updated total amount from TIỀN MẶT: " + cashAmount);
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        } else {
            // Nếu không tìm thấy tổng bằng pattern, tìm số lớn nhất (có thể là tổng)
            totalAmount = findLargestAmount(ocrText);
            if (totalAmount != null) {
                info.setTotalAmount(totalAmount);
                Log.d(TAG, "Using largest amount as total: " + totalAmount);
            }
        }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting receipt info", e);
            // Vẫn trả về info đã có, có thể sẽ empty
        }
        
        return info;
    }
    
    /**
     * Tìm số tiền lớn nhất trong text (có thể là tổng)
     */
    private BigDecimal findLargestAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Pattern để tìm số tiền (hỗ trợ cả 225,000 và 225000)
        Pattern amountPattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+)");
        Matcher matcher = amountPattern.matcher(text);
        BigDecimal largest = null;
        
        while (matcher.find()) {
            try {
                String amountStr = matcher.group(1).replaceAll("[.,]", "");
                BigDecimal amount = new BigDecimal(amountStr);
                
                // Chỉ xem xét số tiền hợp lý (từ 5,000 VND trở lên, tối đa 100 triệu)
                if (amount.compareTo(new BigDecimal("5000")) >= 0 && 
                    amount.compareTo(new BigDecimal("100000000")) <= 0) {
                    if (largest == null || amount.compareTo(largest) > 0) {
                        largest = amount;
                        Log.d(TAG, "Found candidate amount: " + largest);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing amount: " + matcher.group(1), e);
            }
        }
        
        if (largest != null) {
            Log.d(TAG, "Largest amount found: " + largest);
        }
        
        return largest;
    }
    
    /**
     * Xây dựng ghi chú từ thông tin bill - lấy đầy đủ thông tin, không giới hạn độ dài
     */
    private String buildReceiptNote(ReceiptInfo info) {
        StringBuilder note = new StringBuilder();
        
        // Ưu tiên: Tên nhà hàng/cửa hàng
        if (info.getRestaurantName() != null && !info.getRestaurantName().isEmpty()) {
            note.append(info.getRestaurantName());
        }
        
        // Thêm items - lấy nhiều hơn để có thông tin đầy đủ
        if (!info.getItems().isEmpty()) {
            if (note.length() > 0) note.append(" - ");
            // Lấy tất cả items (hoặc tối đa 10 items để không quá dài)
            int itemCount = Math.min(10, info.getItems().size());
            List<String> selectedItems = info.getItems().subList(0, itemCount);
            String itemsStr = String.join(", ", selectedItems);
            // Không truncate, hiển thị đầy đủ
            note.append(itemsStr);
            // Nếu còn items, thêm dấu ...
            if (info.getItems().size() > itemCount) {
                note.append("...");
            }
        }
        
        return note.length() > 0 ? note.toString() : null;
    }
    
    /**
     * Phân tích category từ nội dung OCR text - đơn giản và chính xác hơn
     */
    public String analyzeCategoryFromText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "Other Expense";
        }
        
        String lowerText = ocrText.toLowerCase();
        
        // Kiểm tra các category theo thứ tự ưu tiên
        // 1. Ăn uống / Food
        if (lowerText.matches(".*(ăn|an|uống|nhà hàng|restaurant|quán|foody|grab|now|baemin|món|com|phở|bún|bánh|coca|sprite|soda|tonic|đồ ăn|thức ăn|ăn uống|nước ngọt).*")) {
            return "Food & Dining";
        }
        
        // 2. Tiền điện / Bills
        if (lowerText.matches(".*(tiền điện|điện|hóa đơn điện|tiền nước|hóa đơn|bill|tiền internet|internet|tiền wifi|wifi|tiền nhà|nhà trọ|phí sinh hoạt|utility).*")) {
            return "Bills & Utilities";
        }
        
        // 3. Xăng xe / Transportation
        if (lowerText.matches(".*(xăng|gas|fuel|nhiên liệu|xe|car|taxi|uber|grab|xe máy|đổ xăng|đổ dầu|parking|đỗ xe).*")) {
            return "Transportation";
        }
        
        // 4. Mua sắm / Shopping
        if (lowerText.matches(".*(mua sắm|shopping|thời trang|fashion|clothes|quần áo|giày dép|siêu thị|supermarket|market|chợ).*")) {
            return "Shopping";
        }
        
        // 5. Y tế / Healthcare
        if (lowerText.matches(".*(y tế|health|medical|hospital|bệnh viện|thuốc|medicine|pharmacy|nhà thuốc|khám bệnh).*")) {
            return "Healthcare";
        }
        
        // 6. Giải trí / Entertainment
        if (lowerText.matches(".*(giải trí|entertainment|movie|cinema|phim|xem phim|game|gaming|karaoke|nhạc).*")) {
            return "Entertainment";
        }
        
        // 7. Giáo dục / Education
        if (lowerText.matches(".*(giáo dục|education|school|học phí|tuition|book|sách|học hành|trường học).*")) {
            return "Education";
        }
        
        // Mặc định: Other Expense
        return "Other Expense";
    }
    
    /**
     * Chuẩn hóa văn bản từ OCR
     */
    private String normalizeReceiptText(String text) {
        // Loại bỏ các ký tự đặc biệt không cần thiết
        String normalized = text.replaceAll("[^\\p{L}\\p{N}\\s.,:+-]", " ");
        
        // Loại bỏ nhiều khoảng trắng liên tiếp
        normalized = normalized.replaceAll("\\s+", " ");
        
        // Kết hợp các dòng thành một chuỗi
        normalized = normalized.replaceAll("\n", " ");
        
        return normalized.trim();
    }
    
    /**
     * Trích xuất thông tin ghi chú từ hóa đơn
     * Thường là tên cửa hàng, địa chỉ, hoặc mô tả sản phẩm
     */
    private String extractReceiptNote(String text) {
        List<String> lines = new ArrayList<>();
        String[] parts = text.split("\n");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() > 3 && trimmed.length() < 100) {
                lines.add(trimmed);
            }
        }
        
        // Lấy 3-5 dòng đầu tiên làm note
        if (lines.size() > 0) {
            int endIndex = Math.min(5, lines.size());
            return String.join(" - ", lines.subList(0, endIndex));
        }
        
        return null;
    }
    
    /**
     * Đóng recognizer để giải phóng tài nguyên
     */
    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}

