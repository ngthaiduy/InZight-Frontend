package com.example.inzightapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ProgressBar;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Category.CategoryCompactAdapter;
import com.example.inzightapp.adapter.Category.CategoryGridAdapter;
import com.example.inzightapp.ai.TransactionAIExtractor;
import com.example.inzightapp.ai.SmsReceiver;
import com.example.inzightapp.ai.QRCodeScannerHelper;
import com.example.inzightapp.ai.ReceiptOCRHelper;
import com.example.inzightapp.utils.CameraHelper;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.model.request.TransactionRequest;
import com.example.inzightapp.model.response.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.*;

/**
 * Fragment để quản lý giao dịch được AI tự động phát hiện từ SMS
 * Tương tự tính năng của MoMo
 */
public class AITransactionFragment extends Fragment {

    private EditText etAmount, etNote, etSmsMessage;
    private Spinner spWallet;
    private RecyclerView rvCategoryCompact;
    private TextView tabExpense, tabIncome, tvDate, tvBank, tvAiStatus;
    private Button btnSave, btnAnalyze, btnScanQR, btnScanReceipt;
    private Switch switchAiEnabled;
    private LinearLayout cardExtractedTransaction, cardSmsInput;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    
    private String selectedType = "EXPENSE";
    private TransactionApiService api;
    private List<CategoryResponse> allCategories = new ArrayList<>();
    private CategoryResponse selectedCategory;
    private Calendar selectedDate = Calendar.getInstance();
    private TransactionAIExtractor.ExtractedTransaction extractedTransaction;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    
    // Helpers
    private QRCodeScannerHelper qrScanner;
    private ReceiptOCRHelper ocrHelper;
    private CameraHelper cameraHelper;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String currentScanMode = ""; // "qr" or "receipt"

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ai_transaction, container, false);

        etAmount = v.findViewById(R.id.etAmount);
        etNote = v.findViewById(R.id.etNote);
        etSmsMessage = v.findViewById(R.id.etSmsMessage);
        spWallet = v.findViewById(R.id.spWallet);
        rvCategoryCompact = v.findViewById(R.id.rvCategoryCompact);
        tabExpense = v.findViewById(R.id.tabExpense);
        tabIncome = v.findViewById(R.id.tabIncome);
        tvDate = v.findViewById(R.id.tvDate);
        tvBank = v.findViewById(R.id.tvBank);
        tvAiStatus = v.findViewById(R.id.tvAiStatus);
        btnSave = v.findViewById(R.id.btnSaveTransaction);
        btnAnalyze = v.findViewById(R.id.btnAnalyze);
        btnScanQR = v.findViewById(R.id.btnScanQR);
        btnScanReceipt = v.findViewById(R.id.btnScanReceipt);
        switchAiEnabled = v.findViewById(R.id.switchAiEnabled);
        cardExtractedTransaction = v.findViewById(R.id.cardExtractedTransaction);
        cardSmsInput = v.findViewById(R.id.cardSmsInput);
        imgPreview = v.findViewById(R.id.imgPreview);
        progressBar = v.findViewById(R.id.progressBar);
        
        ImageView btnPickDate = v.findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(view -> showDatePickerDialog());

        api = ApiClient.getClient(requireContext()).create(TransactionApiService.class);

        // Initialize helpers
        qrScanner = new QRCodeScannerHelper();
        ocrHelper = new ReceiptOCRHelper();
        cameraHelper = new CameraHelper(this);
        
        setupTabs();
        setupAmountFormatting();
        setupAiSwitch();
        setupCameraLaunchers();
        setupQRScanner();
        setupReceiptScanner();
        loadWallets();
        loadCategories();
        setupSaveButton();
        setupAnalyzeButton();
        checkPendingTransaction();

        // Hiển thị ngày hôm nay
        tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));

        ImageView btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWallets();
        checkPendingTransaction();
    }

    /**
     * Kiểm tra và hiển thị giao dịch đang chờ từ SMS
     */
    private void checkPendingTransaction() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AI_Transaction_Prefs", Context.MODE_PRIVATE);
        boolean hasPending = prefs.getBoolean("has_pending_transaction", false);
        
        if (hasPending) {
            String amountStr = prefs.getString("latest_transaction_amount", "");
            String type = prefs.getString("latest_transaction_type", "");
            String category = prefs.getString("latest_transaction_category", "");
            String note = prefs.getString("latest_transaction_note", "");
            String bank = prefs.getString("latest_transaction_bank", "");
            long dateTime = prefs.getLong("latest_transaction_date", System.currentTimeMillis());
            String originalMessage = prefs.getString("latest_transaction_message", "");
            
            if (!amountStr.isEmpty()) {
                // Hiển thị thông tin giao dịch
                displayExtractedTransaction(amountStr, type, category, note, bank, new Date(dateTime), originalMessage);
                // Xóa flag để không hiển thị lại
                prefs.edit().putBoolean("has_pending_transaction", false).apply();
            }
        }
    }

    /**
     * Hiển thị thông tin giao dịch đã được trích xuất
     */
    private void displayExtractedTransaction(String amountStr, String type, String category, 
                                            String note, String bank, Date date, String originalMessage) {
        extractedTransaction = new TransactionAIExtractor.ExtractedTransaction();
        extractedTransaction.setAmount(new BigDecimal(amountStr));
        extractedTransaction.setType(type);
        extractedTransaction.setSuggestedCategory(category);
        extractedTransaction.setNote(note);
        extractedTransaction.setBank(bank);
        extractedTransaction.setTransactionDate(date);
        
        // Điền vào form
        etAmount.setText(formatAmount(extractedTransaction.getAmount()));
        etNote.setText(extractedTransaction.getNote());
        selectedType = extractedTransaction.getType();
        selectType(selectedType);
        
        if (bank != null && !bank.isEmpty()) {
            tvBank.setText(getString(R.string.from, bank));
            tvBank.setVisibility(View.VISIBLE);
        } else {
            tvBank.setVisibility(View.GONE);
        }
        
        selectedDate.setTime(extractedTransaction.getTransactionDate());
        tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(extractedTransaction.getTransactionDate()));
        
        etSmsMessage.setText(originalMessage);
        cardExtractedTransaction.setVisibility(View.VISIBLE);
        cardSmsInput.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
        
        // Tìm và chọn category tương ứng
        loadCategoriesForType(selectedType, category);
    }

    /**
     * Tải danh mục và tự động chọn danh mục gợi ý
     */
    private void loadCategoriesForType(String type, String suggestedCategoryName) {
        api.getCategoriesByType(type).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    allCategories = res.body();
                    setupCompactRecycler(allCategories);
                    
                    // Tự động chọn category gợi ý
                    if (suggestedCategoryName != null && !suggestedCategoryName.isEmpty()) {
                        for (CategoryResponse cat : allCategories) {
                            if (cat.getName().equalsIgnoreCase(suggestedCategoryName) || 
                                cat.getName().contains(suggestedCategoryName)) {
                                selectedCategory = cat;
                                if (rvCategoryCompact.getAdapter() instanceof CategoryCompactAdapter) {
                                    ((CategoryCompactAdapter) rvCategoryCompact.getAdapter()).setSelectedCategory(selectedCategory);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_colon, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Format số tiền
     */
    private String formatAmount(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        return formatter.format(amount) + " VND";
    }

    /**
     * Setup camera launchers
     */
    private void setupCameraLaunchers() {
        try {
            cameraLauncher = cameraHelper.createCameraLauncher(new CameraHelper.ImageCaptureCallback() {
                @Override
                public void onImageCaptured(Bitmap bitmap) {
                    try {
                        if (bitmap != null && !bitmap.isRecycled() && isAdded() && getContext() != null) {
                            // Tạo copy ngay lập tức để tránh bitmap gốc bị hệ thống recycle
                            Bitmap bitmapCopy = null;
                            try {
                                bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
                            } catch (Exception e) {
                                android.util.Log.e("AITransaction", "Error copying bitmap from camera", e);
                                bitmapCopy = bitmap; // Fallback
                            }
                            
                            if (bitmapCopy != null) {
                                // Process image based on current scan mode
                                // processImage sẽ tự set bitmap vào ImageView
                                processImage(bitmapCopy, "qr".equals(currentScanMode));
                            } else {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                if (getContext() != null && isAdded()) {
                                    Toast.makeText(getContext(), getString(R.string.cannot_process_image), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (getContext() != null && isAdded()) {
                                Toast.makeText(getContext(), getString(R.string.cannot_read_image), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AITransaction", "Error in camera callback", e);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), getString(R.string.error_processing_image_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            imagePickerLauncher = cameraHelper.createImagePickerLauncher(new CameraHelper.ImageCaptureCallback() {
                @Override
                public void onImageCaptured(Bitmap bitmap) {
                    try {
                        if (bitmap != null && !bitmap.isRecycled() && isAdded() && getContext() != null) {
                            // Tạo copy ngay lập tức để tránh bitmap gốc bị hệ thống recycle
                            Bitmap bitmapCopy = null;
                            try {
                                bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
                            } catch (Exception e) {
                                android.util.Log.e("AITransaction", "Error copying bitmap from gallery", e);
                                bitmapCopy = bitmap; // Fallback
                            }
                            
                            if (bitmapCopy != null) {
                                // Process image based on current scan mode
                                // processImage sẽ tự set bitmap vào ImageView
                                processImage(bitmapCopy, "qr".equals(currentScanMode));
                            } else {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                if (getContext() != null && isAdded()) {
                                    Toast.makeText(getContext(), getString(R.string.cannot_process_image), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (getContext() != null && isAdded()) {
                                Toast.makeText(getContext(), getString(R.string.cannot_read_image), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AITransaction", "Error in image picker callback", e);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), getString(R.string.error_processing_image_detail, e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AITransaction", "Error setting up camera launchers", e);
        }
    }

    /**
     * Setup nút quét QR code
     */
    private void setupQRScanner() {
        btnScanQR.setOnClickListener(v -> {
            if (!cameraHelper.hasCameraPermission()) {
                cameraHelper.requestCameraPermission();
                return;
            }

            // Show dialog để chọn: chụp ảnh hoặc chọn từ gallery
            showImageSourceDialog(true);
        });
    }

    /**
     * Setup nút quét hóa đơn
     */
    private void setupReceiptScanner() {
        btnScanReceipt.setOnClickListener(v -> {
            if (!cameraHelper.hasCameraPermission()) {
                cameraHelper.requestCameraPermission();
                return;
            }

            // Show dialog để chọn: chụp ảnh hoặc chọn từ gallery
            showImageSourceDialog(false);
        });
    }

    /**
     * Hiển thị dialog để chọn nguồn ảnh (camera hoặc gallery)
     */
    private void showImageSourceDialog(boolean isQRScan) {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(isQRScan ? "Quét mã QR" : "Quét hóa đơn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        currentScanMode = isQRScan ? "qr" : "receipt";
                        Intent cameraIntent = cameraHelper.createTakePictureIntent();
                        if (cameraIntent != null) {
                            cameraLauncher.launch(cameraIntent);
                        }
                    } else {
                        // Gallery
                        currentScanMode = isQRScan ? "qr" : "receipt";
                        imagePickerLauncher.launch("image/*");
                    }
                })
                .show();
    }

    /**
     * Xử lý ảnh sau khi chụp/chọn
     */
    private void processImage(Bitmap bitmap, boolean isQRScan) {
        if (bitmap == null) {
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), getString(R.string.cannot_read_image), Toast.LENGTH_SHORT).show();
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }

        // Kiểm tra fragment đang attached
        if (!isAdded() || getContext() == null) {
            return;
        }

        // Giảm kích thước bitmap nếu quá lớn để tránh OOM
        // resizeBitmapIfNeeded đã tạo copy riêng, không ảnh hưởng đến bitmap gốc
        Bitmap processedBitmap = resizeBitmapIfNeeded(bitmap);
        
        if (processedBitmap == null) {
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), getString(R.string.cannot_process_image), Toast.LENGTH_SHORT).show();
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }
        
        // Tạo copy riêng cho ImageView để tránh conflict với OCR/QR scanner
        Bitmap displayBitmap = null;
        try {
            displayBitmap = processedBitmap.copy(processedBitmap.getConfig(), false);
        } catch (OutOfMemoryError e) {
            android.util.Log.e("AITransaction", "OutOfMemoryError copying bitmap for display", e);
            // Nếu không copy được, dùng processedBitmap trực tiếp nhưng không recycle
            displayBitmap = processedBitmap;
        } catch (Exception e) {
            android.util.Log.e("AITransaction", "Error copying bitmap for display", e);
            displayBitmap = processedBitmap;
        }
        
        // Set bitmap vào ImageView trên main thread
        if (displayBitmap != null && !displayBitmap.isRecycled() && imgPreview != null && isAdded() && getContext() != null) {
            if (getActivity() != null) {
                Bitmap finalDisplayBitmap = displayBitmap;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded() || getContext() == null || imgPreview == null) return;
                    try {
                        // Kiểm tra lại bitmap trước khi set
                        if (finalDisplayBitmap != null && !finalDisplayBitmap.isRecycled()) {
                            imgPreview.setImageBitmap(finalDisplayBitmap);
                            imgPreview.setVisibility(View.VISIBLE);
                        }
                    } catch (IllegalArgumentException e) {
                        android.util.Log.e("AITransaction", "Bitmap recycled when setting to ImageView", e);
                        // Bitmap đã bị recycle, thử tạo lại từ processedBitmap
                        try {
                            if (processedBitmap != null && !processedBitmap.isRecycled()) {
                                Bitmap newDisplay = processedBitmap.copy(processedBitmap.getConfig(), false);
                                if (newDisplay != null) {
                                    imgPreview.setImageBitmap(newDisplay);
                                    imgPreview.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception e2) {
                            android.util.Log.e("AITransaction", "Error creating new bitmap for ImageView", e2);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AITransaction", "Error setting bitmap to ImageView", e);
                    }
                });
            }
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (isQRScan) {
            // Quét QR code
            try {
                qrScanner.scanQRFromBitmap(processedBitmap, new QRCodeScannerHelper.QRScanCallback() {
                    @Override
                    public void onSuccess(String qrContent) {
                        if (!isAdded() || getContext() == null) return;
                        
                        try {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), getString(R.string.qr_scanned_analyzing), Toast.LENGTH_SHORT).show();
                                });
                            }
                            
                            // Phân tích QR content và trích xuất giao dịch
                            TransactionAIExtractor.ExtractedTransaction transaction = null;
                            try {
                                transaction = qrScanner.extractTransactionFromQR(qrContent);
                            } catch (Exception e) {
                                android.util.Log.e("AITransaction", "Error extracting from QR", e);
                            }
                            
                            if (transaction != null && transaction.getAmount() != null) {
                                final TransactionAIExtractor.ExtractedTransaction finalTransaction = transaction;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (!isAdded() || getContext() == null) return;
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        displayTransactionFromExtraction(finalTransaction, qrContent);
                                        autoSaveTransactionIfReady(finalTransaction);
                                    });
                                }
                            } else {
                                // Thử dùng TransactionAIExtractor
                                try {
                                    transaction = TransactionAIExtractor.extract(qrContent);
                                } catch (Exception e) {
                                    android.util.Log.e("AITransaction", "Error extracting transaction", e);
                                }
                                
                                if (transaction != null && transaction.getAmount() != null) {
                                    final TransactionAIExtractor.ExtractedTransaction finalTransaction2 = transaction;
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            if (!isAdded() || getContext() == null) return;
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            displayTransactionFromExtraction(finalTransaction2, qrContent);
                                            autoSaveTransactionIfReady(finalTransaction2);
                                        });
                                    }
                                } else {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            if (!isAdded() || getContext() == null) return;
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            Toast.makeText(getContext(), 
                                                getString(R.string.cannot_analyze_qr, 
                                                (qrContent.length() > 50 ? qrContent.substring(0, 50) + "..." : qrContent)), 
                                                Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AITransaction", "Error in QR callback", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getContext() == null) return;
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    Toast.makeText(getContext(), getString(R.string.error_processing_qr, e.getMessage()), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (!isAdded() || getContext() == null) return;
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AITransaction", "Error scanning QR", e);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_scanning_qr, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // OCR hóa đơn
            try {
                ocrHelper.recognizeText(processedBitmap, new ReceiptOCRHelper.OCRCallback() {
                    @Override
                    public void onSuccess(String extractedText) {
                        if (!isAdded() || getContext() == null) return;
                        
                        try {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), getString(R.string.receipt_read_analyzing), Toast.LENGTH_SHORT).show();
                                });
                            }
                            
                            // Phân tích và trích xuất giao dịch với thông tin chi tiết từ bill
                            TransactionAIExtractor.ExtractedTransaction transaction = null;
                            try {
                                transaction = ocrHelper.extractTransactionFromReceipt(extractedText);
                            } catch (Exception e) {
                                android.util.Log.e("AITransaction", "Error extracting from receipt", e);
                            }
                            
                            if (transaction != null && transaction.getAmount() != null) {
                                final TransactionAIExtractor.ExtractedTransaction finalTransaction = transaction;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (!isAdded() || getContext() == null) return;
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        displayTransactionFromExtraction(finalTransaction, extractedText);
                                        autoSaveTransactionIfReady(finalTransaction);
                                    });
                                }
                            } else {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (!isAdded() || getContext() == null) return;
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        Toast.makeText(getContext(), 
                                            getString(R.string.cannot_extract_transaction_from_receipt), 
                                            Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AITransaction", "Error in OCR callback", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getContext() == null) return;
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    Toast.makeText(getContext(), getString(R.string.error_processing_receipt, e.getMessage()), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (!isAdded() || getContext() == null) return;
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AITransaction", "Error in OCR", e);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.error_reading_receipt, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        // KHÔNG recycle bitmap ở đây vì:
        // 1. processedBitmap có thể được dùng bởi OCR/QR scanner
        // 2. displayBitmap đang được dùng bởi ImageView
        // Bitmap sẽ được garbage collected tự động
    }

    /**
     * Giảm kích thước bitmap nếu quá lớn để tránh OutOfMemoryError
     * Trả về bitmap MỚI (copy hoặc resized) để tránh conflict với bitmap gốc
     */
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        // Kiểm tra xem bitmap đã bị recycle chưa
        if (bitmap.isRecycled()) {
            android.util.Log.w("AITransaction", "Bitmap already recycled, cannot resize");
            return null;
        }
        
        int maxWidth = 1024;
        int maxHeight = 1024;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            // Tạo copy để tránh recycle conflict với bitmap gốc
            try {
                return bitmap.copy(bitmap.getConfig(), false);
            } catch (OutOfMemoryError e) {
                android.util.Log.e("AITransaction", "OutOfMemoryError copying bitmap", e);
                // Không thể copy, trả về null để xử lý lỗi
                return null;
            } catch (Exception e) {
                android.util.Log.e("AITransaction", "Error copying bitmap", e);
                return null;
            }
        }
        
        // Cần resize
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        try {
            // Tạo bitmap mới với kích thước nhỏ hơn
            // createScaledBitmap tự động tạo bitmap mới, không ảnh hưởng bitmap gốc
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            // KHÔNG recycle bitmap gốc - để CameraHelper hoặc system quản lý
            return resized;
        } catch (OutOfMemoryError e) {
            android.util.Log.e("AITransaction", "OutOfMemoryError when resizing bitmap", e);
            // Nếu không thể resize, thử copy bitmap gốc (nhỏ hơn một chút)
            try {
                return bitmap.copy(bitmap.getConfig(), false);
            } catch (Exception e2) {
                android.util.Log.e("AITransaction", "Error copying bitmap as fallback", e2);
                return null; // Không thể xử lý
            }
        }
    }

    /**
     * Hiển thị giao dịch từ extraction
     */
    private void displayTransactionFromExtraction(TransactionAIExtractor.ExtractedTransaction transaction, 
                                                  String sourceText) {
        if (transaction == null || !isAdded() || getContext() == null) {
            return;
        }
        
        try {
            extractedTransaction = transaction;
            
            if (etAmount != null && transaction.getAmount() != null) {
                etAmount.setText(formatAmount(transaction.getAmount()));
            }
            
            if (etNote != null) {
                etNote.setText(transaction.getNote() != null ? transaction.getNote() : "");
            }
            
            selectedType = transaction.getType() != null ? transaction.getType() : "EXPENSE";
            selectType(selectedType);
            
            if (tvBank != null) {
                if (transaction.getBank() != null && !transaction.getBank().isEmpty()) {
                    tvBank.setText(getString(R.string.from, transaction.getBank()));
                    tvBank.setVisibility(View.VISIBLE);
                } else {
                    tvBank.setVisibility(View.GONE);
                }
            }
            
            if (selectedDate != null && transaction.getTransactionDate() != null) {
                selectedDate.setTime(transaction.getTransactionDate());
                if (tvDate != null) {
                    tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(transaction.getTransactionDate()));
                }
            }
            
            if (cardExtractedTransaction != null) {
                cardExtractedTransaction.setVisibility(View.VISIBLE);
            }
            if (cardSmsInput != null) {
                cardSmsInput.setVisibility(View.GONE);
            }
            if (btnSave != null) {
                btnSave.setVisibility(View.VISIBLE);
            }
            
            loadCategoriesForType(selectedType, transaction.getSuggestedCategory());
        } catch (Exception e) {
            android.util.Log.e("AITransaction", "Error displaying transaction", e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), getString(R.string.error_displaying_transaction, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Tự động lưu giao dịch nếu có đầy đủ thông tin (wallet và category đã được chọn)
     */
    private void autoSaveTransactionIfReady(TransactionAIExtractor.ExtractedTransaction transaction) {
        // Kiểm tra xem có ví mặc định không
        WalletResponse selectedWallet = (WalletResponse) spWallet.getSelectedItem();
        
        // Nếu chưa chọn ví hoặc category, hiển thị dialog xác nhận
        if (selectedWallet == null || selectedWallet.getId() == 0L || selectedCategory == null) {
            // Hiển thị dialog để user chọn ví và xác nhận
            showAutoSaveConfirmationDialog(transaction);
        } else {
            // Đã có đầy đủ thông tin, có thể tự động lưu
            // Nhưng để an toàn, vẫn hiển thị dialog xác nhận
            showAutoSaveConfirmationDialog(transaction);
        }
    }

    /**
     * Hiển thị dialog xác nhận tự động lưu
     */
    private void showAutoSaveConfirmationDialog(TransactionAIExtractor.ExtractedTransaction transaction) {
        if (transaction == null || !isAdded() || getContext() == null) {
            return;
        }
        
        try {
            String message = "Giao dịch đã được phân tích:\n\n" +
                            "💰 Số tiền: " + (transaction.getAmount() != null ? formatAmount(transaction.getAmount()) : "N/A") + "\n" +
                            "📝 Ghi chú: " + (transaction.getNote() != null && !transaction.getNote().isEmpty() ? transaction.getNote() : "Không có") + "\n" +
                            "📂 Danh mục: " + (transaction.getSuggestedCategory() != null ? transaction.getSuggestedCategory() : "Chưa xác định") + "\n\n" +
                            "Bạn có muốn lưu giao dịch này không?";
            
            new android.app.AlertDialog.Builder(requireContext())
                .setTitle("✨ Xác nhận giao dịch")
                .setMessage(message)
                .setPositiveButton("💾 Lưu ngay", (dialog, which) -> {
                    // Tự động chọn wallet đầu tiên nếu chưa chọn
                    WalletResponse selectedWallet = (WalletResponse) spWallet.getSelectedItem();
                    if (selectedWallet == null || selectedWallet.getId() == 0L) {
                        // Chọn ví đầu tiên có sẵn
                        ArrayAdapter<WalletResponse> adapter = (ArrayAdapter<WalletResponse>) spWallet.getAdapter();
                        if (adapter != null && adapter.getCount() > 1) {
                            // Bỏ qua item đầu tiên (placeholder)
                            for (int i = 1; i < adapter.getCount(); i++) {
                                WalletResponse wallet = adapter.getItem(i);
                                if (wallet != null && wallet.getId() > 0L) {
                                    spWallet.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Đợi một chút để category được load xong
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        // Kiểm tra lại category
                        if (selectedCategory == null) {
                            // Chọn category đầu tiên
                            if (!allCategories.isEmpty()) {
                                selectedCategory = allCategories.get(0);
                                if (rvCategoryCompact.getAdapter() instanceof CategoryCompactAdapter) {
                                    ((CategoryCompactAdapter) rvCategoryCompact.getAdapter()).setSelectedCategory(selectedCategory);
                                }
                            }
                        }
                        
                        // Lưu giao dịch
                        if (selectedCategory != null) {
                            btnSave.performClick();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.please_select_category_before_save), Toast.LENGTH_SHORT).show();
                        }
                    }, 500);
                })
                .setNegativeButton(getString(R.string.edit), (dialog, which) -> {
                    // Keep form for user to edit
                    dialog.dismiss();
                })
                .setNeutralButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
        } catch (Exception e) {
            android.util.Log.e("AITransaction", "Error showing confirmation dialog", e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), getString(R.string.error_showing_dialog, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Setup nút phân tích SMS
     */
    private void setupAnalyzeButton() {
        btnAnalyze.setOnClickListener(v -> {
            String smsMessage = etSmsMessage.getText().toString().trim();
            if (TextUtils.isEmpty(smsMessage)) {
                Toast.makeText(getContext(), getString(R.string.please_enter_sms), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Phân tích tin nhắn
            TransactionAIExtractor.ExtractedTransaction transaction = 
                TransactionAIExtractor.extract(smsMessage);
            
            if (transaction == null) {
                Toast.makeText(getContext(), getString(R.string.cannot_analyze_sms), Toast.LENGTH_LONG).show();
                return;
            }
            
            extractedTransaction = transaction;
            
            // Hiển thị kết quả
            etAmount.setText(formatAmount(transaction.getAmount()));
            etNote.setText(transaction.getNote());
            selectedType = transaction.getType();
            selectType(selectedType);
            
            if (transaction.getBank() != null && !transaction.getBank().isEmpty()) {
                tvBank.setText("Từ: " + transaction.getBank());
                tvBank.setVisibility(View.VISIBLE);
            } else {
                tvBank.setVisibility(View.GONE);
            }
            
            selectedDate.setTime(transaction.getTransactionDate());
            tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(transaction.getTransactionDate()));
            
            cardExtractedTransaction.setVisibility(View.VISIBLE);
            cardSmsInput.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            
            // Tìm và chọn category
            loadCategoriesForType(selectedType, transaction.getSuggestedCategory());
            
            Toast.makeText(getContext(), getString(R.string.analysis_successful), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Setup switch bật/tắt AI tự động
     */
    private void setupAiSwitch() {
        boolean isEnabled = SmsReceiver.isAiEnabled(requireContext());
        switchAiEnabled.setChecked(isEnabled);
        updateAiStatus(isEnabled);
        
        switchAiEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Kiểm tra quyền SMS trước khi bật
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) 
                            != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) 
                            != PackageManager.PERMISSION_GRANTED) {
                        // Yêu cầu quyền
                        ActivityCompat.requestPermissions(requireActivity(),
                                new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                                PERMISSION_REQUEST_CODE);
                        switchAiEnabled.setChecked(false); // Tạm thời tắt cho đến khi có quyền
                        return;
                    }
                }
            }
            
            SmsReceiver.setAiEnabled(requireContext(), isChecked);
            updateAiStatus(isChecked);
            Toast.makeText(getContext(), 
                isChecked ? "AI tự động đã bật" : "AI tự động đã tắt", 
                Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Cập nhật trạng thái AI
     */
    private void updateAiStatus(boolean enabled) {
        if (enabled) {
            tvAiStatus.setText("AI tự động đang bật - Nhận tin nhắn từ ngân hàng/ví");
            tvAiStatus.setTextColor(0xFF4CAF50); // Green color
        } else {
            tvAiStatus.setText("AI tự động đã tắt - Nhấn để bật lại");
            tvAiStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    // Tabs: Expense / Income
    private void setupTabs() {
        tabExpense.setOnClickListener(v -> selectType("EXPENSE"));
        tabIncome.setOnClickListener(v -> selectType("INCOME"));
        selectType(selectedType);
    }

    private void selectType(String type) {
        selectedType = type;
        if ("EXPENSE".equals(type)) {
            tabExpense.setBackgroundResource(R.drawable.bg_tab_selected_pill);
            tabExpense.setTextColor(getResources().getColor(android.R.color.white));
            tabIncome.setBackgroundResource(android.R.color.transparent);
            tabIncome.setTextColor(getResources().getColor(R.color.home_text_regular));
        } else {
            tabExpense.setBackgroundResource(android.R.color.transparent);
            tabExpense.setTextColor(getResources().getColor(R.color.home_text_regular));
            tabIncome.setBackgroundResource(R.drawable.bg_tab_selected_pill);
            tabIncome.setTextColor(getResources().getColor(android.R.color.white));
        }
        loadCategories();
    }

    // Load danh mục từ API
    private void loadCategories() {
        api.getCategoriesByType(selectedType).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    allCategories = res.body();
                    setupCompactRecycler(allCategories);
                } else {
                    Toast.makeText(getContext(), getString(R.string.load_categories_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_colon, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    // RecyclerView hiển thị danh mục nhỏ gọn
    private void setupCompactRecycler(List<CategoryResponse> categories) {
        rvCategoryCompact.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        int limit = Math.min(4, categories.size());
        List<CategoryResponse> limited = categories.subList(0, limit);

        CategoryCompactAdapter adapter = new CategoryCompactAdapter(
                limited,
                category -> selectedCategory = category,
                this::showAllCategoriesBottomSheet
        );

        rvCategoryCompact.setAdapter(adapter);
    }

    // Popup hiển thị tất cả danh mục
    private void showAllCategoriesBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_category_all, null);
        dialog.setContentView(sheetView);

        GridView gridAll = sheetView.findViewById(R.id.gridAllCategories);
        CategoryGridAdapter adapter = new CategoryGridAdapter(requireContext(), allCategories);
        gridAll.setAdapter(adapter);

        if (selectedCategory != null) {
            for (int i = 0; i < allCategories.size(); i++) {
                if (allCategories.get(i).getId() == selectedCategory.getId()) {
                    adapter.setSelectedPosition(i);
                    break;
                }
            }
        }

        gridAll.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = allCategories.get(position);
            adapter.setSelectedPosition(position);

            if (rvCategoryCompact.getAdapter() instanceof CategoryCompactAdapter) {
                ((CategoryCompactAdapter) rvCategoryCompact.getAdapter()).setSelectedCategory(selectedCategory);
            }

            Toast.makeText(requireContext(),
                    "Selected: " + selectedCategory.getName(),
                    Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }

    // Load ví
    private void loadWallets() {
        api.getWallets().enqueue(new Callback<List<WalletResponse>>() {
            @Override
            public void onResponse(Call<List<WalletResponse>> call, Response<List<WalletResponse>> res) {
                List<WalletResponse> wallets = new ArrayList<>();

                if (res.isSuccessful() && res.body() != null) {
                    wallets.addAll(res.body());
                }

                WalletResponse placeholder = new WalletResponse();
                placeholder.setId(0L);
                placeholder.setName(" Select your wallet ");
                placeholder.setBalance(BigDecimal.ZERO);
                placeholder.setCurrency("VND");
                wallets.add(0, placeholder);

                WalletResponse addNew = new WalletResponse();
                addNew.setId(-1L);
                addNew.setName("➕ Add new wallet");
                addNew.setBalance(BigDecimal.ZERO);
                addNew.setCurrency("VND");
                wallets.add(addNew);

                ArrayAdapter<WalletResponse> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        wallets
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spWallet.setAdapter(adapter);

                spWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        WalletResponse selected = (WalletResponse) parent.getItemAtPosition(position);

                        if (selected.getId() == -1L) {
                            openAddWalletFragment();
                            spWallet.setSelection(0);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(Call<List<WalletResponse>> call, Throwable t) {
                List<WalletResponse> wallets = new ArrayList<>();

                WalletResponse placeholder = new WalletResponse();
                placeholder.setId(0L);
                placeholder.setName("-- Select wallet --");
                wallets.add(placeholder);

                WalletResponse addNew = new WalletResponse();
                addNew.setId(-1L);
                addNew.setName("➕ Add new wallet");
                wallets.add(addNew);

                ArrayAdapter<WalletResponse> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        wallets
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spWallet.setAdapter(adapter);
            }
        });
    }

    private void openAddWalletFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new AddWalletFragment())
                .addToBackStack(null)
                .commit();
    }

    // Format số tiền
    private void setupAmountFormatting() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                        String formatted = formatter.format(parsed) + " VND";
                        current = formatted;
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length() - 4);
                    } else {
                        current = "";
                        etAmount.setText("");
                    }
                    etAmount.addTextChangedListener(this);
                }
            }
        });
    }

    // Nút lưu
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim().replaceAll("[^\\d]", "");
            String note = etNote.getText().toString().trim();

            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(getContext(), getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategory == null) {
                Toast.makeText(getContext(), getString(R.string.please_select_category), Toast.LENGTH_SHORT).show();
                return;
            }

            WalletResponse wal = (WalletResponse) spWallet.getSelectedItem();
            if (wal == null || wal.getId() == 0L) {
                Toast.makeText(getContext(), getString(R.string.please_choose_wallet), Toast.LENGTH_SHORT).show();
                return;
            }

            if (wal.getId() == -1L) {
                openAddWalletFragment();
                return;
            }

            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            String formattedDate = apiFormat.format(selectedDate.getTime());

            TransactionRequest req = new TransactionRequest(
                    wal.getId(),
                    selectedCategory.getId(),
                    new BigDecimal(amountStr),
                    selectedType,
                    note
            );
            req.setTransactionDate(formattedDate);

            api.createTransaction(req).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        TransactionResponse transaction = res.body();
                        Toast.makeText(getContext(), getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show();

                        Fragment detail = TransactionDetailFragment.newInstance(transaction);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, detail)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.failed_to_save_transaction), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.error_colon, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(calendar.getTime());
                    tvDate.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Đã cấp quyền SMS. AI tự động đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                switchAiEnabled.setChecked(true);
                SmsReceiver.setAiEnabled(requireContext(), true);
                updateAiStatus(true);
            } else {
                Toast.makeText(getContext(), getString(R.string.sms_permission_required), Toast.LENGTH_LONG).show();
                switchAiEnabled.setChecked(false);
                SmsReceiver.setAiEnabled(requireContext(), false);
                updateAiStatus(false);
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.camera_permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (qrScanner != null) {
            qrScanner.close();
        }
        if (ocrHelper != null) {
            ocrHelper.close();
        }
    }
}

