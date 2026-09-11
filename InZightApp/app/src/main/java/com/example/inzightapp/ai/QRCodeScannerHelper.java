package com.example.inzightapp.ai;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;

import java.util.List;

/**
 * Helper class để quét mã QR từ hình ảnh
 */
public class QRCodeScannerHelper {
    
    private static final String TAG = "QRCodeScanner";
    private BarcodeScanner scanner;
    
    public interface QRScanCallback {
        void onSuccess(String qrContent);
        void onError(String error);
    }
    
    public QRCodeScannerHelper() {
        // Cấu hình để scan QR code và các loại barcode khác
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC,
                        Barcode.FORMAT_DATA_MATRIX,
                        Barcode.FORMAT_PDF417
                )
                .enableAllPotentialBarcodes() // Enable detection of all potential barcodes
                .build();
        
        scanner = BarcodeScanning.getClient(options);
    }
    
    /**
     * Quét QR code từ Bitmap
     */
    public void scanQRFromBitmap(Bitmap bitmap, QRScanCallback callback) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            scanQRFromImage(image, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage from bitmap", e);
            if (callback != null) {
                callback.onError("Lỗi xử lý hình ảnh: " + e.getMessage());
            }
        }
    }
    
    /**
     * Quét QR code từ InputImage
     */
    private void scanQRFromImage(InputImage image, QRScanCallback callback) {
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes.isEmpty()) {
                        if (callback != null) {
                            callback.onError("Không tìm thấy mã QR trong hình ảnh");
                        }
                        return;
                    }
                    
                    // Lấy QR code đầu tiên
                    Barcode barcode = barcodes.get(0);
                    String rawValue = barcode.getRawValue();
                    
                    if (rawValue != null && !rawValue.isEmpty()) {
                        Log.d(TAG, "QR Code found: " + rawValue);
                        if (callback != null) {
                            callback.onSuccess(rawValue);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Mã QR không có nội dung");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error scanning QR code", e);
                    if (callback != null) {
                        callback.onError("Lỗi quét mã QR: " + e.getMessage());
                    }
                });
    }
    
    /**
     * Phân tích nội dung QR code và trích xuất thông tin giao dịch
     * Hỗ trợ các format QR code phổ biến:
     - MoMo QR: momo://transfer?...
     - VietQR: 000201010212...
     - Payment QR: bank://payment?...
     */
    public TransactionAIExtractor.ExtractedTransaction extractTransactionFromQR(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }
        
        // Nếu QR code là URL/payment link, sử dụng TransactionAIExtractor để parse
        // Hoặc nếu là plain text chứa thông tin giao dịch
        return TransactionAIExtractor.extract(qrContent);
    }
    
    /**
     * Đóng scanner để giải phóng tài nguyên
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

