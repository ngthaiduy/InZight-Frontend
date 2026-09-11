package com.example.inzightapp.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class VietnameseCurrencyFormatter {
    
    private static final DecimalFormat VN_FORMATTER;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        VN_FORMATTER = new DecimalFormat("#,###", symbols);
        VN_FORMATTER.setGroupingSize(3);
    }
    
    /**
     * Format số tiền với dấu chấm và thêm "VNĐ" ở cuối
     * Ví dụ: 2000 -> "2.000 VNĐ", 2000000 -> "2.000.000 VNĐ"
     */
    public static String format(double amount) {
        if (amount == 0) return "0 VNĐ";
        return VN_FORMATTER.format(amount) + " VNĐ";
    }
    
    /**
     * Format số tiền chỉ với dấu chấm (không có VNĐ)
     */
    public static String formatWithoutCurrency(double amount) {
        if (amount == 0) return "0";
        return VN_FORMATTER.format(amount);
    }
    
    /**
     * Parse số tiền từ string đã format (loại bỏ dấu chấm và VNĐ)
     */
    public static double parse(String formattedAmount) {
        if (formattedAmount == null || formattedAmount.trim().isEmpty()) {
            return 0;
        }
        // Remove "VNĐ", spaces, and dots
        String clean = formattedAmount.replace("VNĐ", "")
                                      .replace("VND", "")
                                      .replace(".", "")
                                      .replace(",", "")
                                      .trim();
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Tạo TextWatcher để format số tiền khi user nhập vào EditText
     */
    public static TextWatcher createTextWatcher(EditText editText) {
        return new TextWatcher() {
            private String current = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);
                    
                    // Remove all non-digit characters
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = formatWithoutCurrency(parsed);
                            current = formatted;
                            editText.setText(formatted);
                            
                            // Set cursor position to end
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            current = "";
                            editText.setText("");
                        }
                    } else {
                        current = "";
                        editText.setText("");
                    }
                    
                    editText.addTextChangedListener(this);
                }
            }
        };
    }
}

