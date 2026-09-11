package com.example.inzightapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Category.CategoryCompactAdapter;
import com.example.inzightapp.adapter.Category.CategoryGridAdapter;
import com.example.inzightapp.ai.ReceiptOCRHelper;
import com.example.inzightapp.ai.TransactionAIExtractor;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.api.finance.CategoryApiService;
import com.example.inzightapp.api.finance.WalletApiService;
import com.example.inzightapp.model.request.WalletRequest;
import com.example.inzightapp.model.request.TransactionRequest;
import com.example.inzightapp.model.response.*;
import com.example.inzightapp.utils.CameraHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.*;

public class AddTransactionFragment extends Fragment {

    // Manual entry views
    private EditText etAmount, etNote;
    private Spinner spWallet;
    private RecyclerView rvCategoryCompact;
    private TextView tabExpense, tabIncome, tvDate;
    
    // Image entry views
    private LinearLayout layoutManualEntry, layoutImageEntry;
    private TextView tabManual, tabImage;
    private View indicatorManual, indicatorImage;
    private View btnScanReceipt;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private EditText etAmountImage, etNoteImage;
    private Spinner spWalletImage;
    private RecyclerView rvCategoryCompactImage;
    private TextView tabExpenseImage, tabIncomeImage, tvDateImage;
    private LinearLayout layoutTypeTabsImage;
    
    private Button btnSave;
    private String selectedType = "EXPENSE";
    private String currentTab = "manual"; // "manual" or "image"
    private TransactionApiService api;
    private WalletApiService walletApiService;
    private List<CategoryResponse> allCategories = new ArrayList<>();
    private CategoryResponse selectedCategory;
    private CategoryResponse selectedCategoryImage;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedDateImage = Calendar.getInstance();
    
    // AI helpers
    private ReceiptOCRHelper ocrHelper;
    private CameraHelper cameraHelper;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String currentScanMode = "receipt"; // Only receipt scanning

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        // Manual entry views
        etAmount = v.findViewById(R.id.etAmount);
        etNote = v.findViewById(R.id.etNote);
        spWallet = v.findViewById(R.id.spWallet);
        rvCategoryCompact = v.findViewById(R.id.rvCategoryCompact);
        tabExpense = v.findViewById(R.id.tabExpense);
        tabIncome = v.findViewById(R.id.tabIncome);
        tvDate = v.findViewById(R.id.tvDate);
        ImageView btnPickDate = v.findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(view -> showDatePickerDialog());
        
        // Image entry views
        layoutManualEntry = v.findViewById(R.id.layoutManualEntry);
        layoutImageEntry = v.findViewById(R.id.layoutImageEntry);
        tabManual = v.findViewById(R.id.tabManual);
        tabImage = v.findViewById(R.id.tabImage);
        indicatorManual = v.findViewById(R.id.indicatorManual);
        indicatorImage = v.findViewById(R.id.indicatorImage);
        btnScanReceipt = v.findViewById(R.id.btnScanReceipt);
        imgPreview = v.findViewById(R.id.imgPreview);
        progressBar = v.findViewById(R.id.progressBar);
        etAmountImage = v.findViewById(R.id.etAmountImage);
        etNoteImage = v.findViewById(R.id.etNoteImage);
        spWalletImage = v.findViewById(R.id.spWalletImage);
        rvCategoryCompactImage = v.findViewById(R.id.rvCategoryCompactImage);
        tabExpenseImage = v.findViewById(R.id.tabExpenseImage);
        tabIncomeImage = v.findViewById(R.id.tabIncomeImage);
        tvDateImage = v.findViewById(R.id.tvDateImage);
        layoutTypeTabsImage = v.findViewById(R.id.layoutTypeTabsImage);
        ImageView btnPickDateImage = v.findViewById(R.id.btnPickDateImage);
        btnPickDateImage.setOnClickListener(view -> showDatePickerDialogImage());

        btnSave = v.findViewById(R.id.btnSaveTransaction);

        api = ApiClient.getClient(requireContext()).create(TransactionApiService.class);
        walletApiService = ApiClient.getClient(requireContext()).create(WalletApiService.class);

        // Initialize AI helpers
        ocrHelper = new ReceiptOCRHelper();
        cameraHelper = new CameraHelper(this);
        setupCameraLaunchers();

        setupMainTabs(); // Tab: Manual Entry / Enter by Photo
        setupTabs(); // Tabs: Expense / Income (for manual entry)
        setupTabsImage(); // Tabs: Expense / Income (for image entry)
        setupAmountFormatting();
        setupAmountFormattingImage();
        setupScanButtons();
        loadWallets();
        loadCategories();
        setupSaveButton();

        // Hiển thị ngày hôm nay
        tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));
        tvDateImage.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));

        ImageView btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
    }
    
    /**
     * Setup tabs: Manual Entry / Enter by Photo
     */
    private void setupMainTabs() {
        tabManual.setOnClickListener(v -> switchTab("manual"));
        tabImage.setOnClickListener(v -> switchTab("image"));
        switchTab("manual"); // Default to manual
    }
    
    /**
     * Switch between manual and image entry tabs
     */
    @SuppressLint("ResourceAsColor")
    private void switchTab(String tab) {
        currentTab = tab;
        if ("manual".equals(tab)) {
            // Manual entry tab
            layoutManualEntry.setVisibility(View.VISIBLE);
            layoutImageEntry.setVisibility(View.GONE);
            tabManual.setTextColor(0xFFFF4081); // Pink color
            tabImage.setTextColor(0xFF757575); // Grey
            indicatorManual.setBackgroundColor(0xFFFF4081);
            indicatorImage.setBackgroundColor(android.R.color.transparent);
        } else {
            // Image entry tab
            layoutManualEntry.setVisibility(View.GONE);
            layoutImageEntry.setVisibility(View.VISIBLE);
            tabManual.setTextColor(0xFF757575);
            tabImage.setTextColor(0xFFFF4081);
            indicatorManual.setBackgroundColor(android.R.color.transparent);
            indicatorImage.setBackgroundColor(0xFFFF4081);
        }
    }
    
    /**
     * Setup tabs for image entry: Expense / Income
     */
    private void setupTabsImage() {
        tabExpenseImage.setOnClickListener(v -> selectTypeImage("EXPENSE"));
        tabIncomeImage.setOnClickListener(v -> selectTypeImage("INCOME"));
        selectTypeImage("EXPENSE");
    }
    
    private void selectTypeImage(String type) {
        selectedType = type;
        if ("EXPENSE".equals(type)) {
            tabExpenseImage.setBackgroundResource(R.drawable.bg_tab_selected_pill);
            tabExpenseImage.setTextColor(getResources().getColor(android.R.color.white));
            tabIncomeImage.setBackgroundResource(android.R.color.transparent);
            tabIncomeImage.setTextColor(getResources().getColor(R.color.home_text_regular));
        } else {
            tabExpenseImage.setBackgroundResource(android.R.color.transparent);
            tabExpenseImage.setTextColor(getResources().getColor(R.color.home_text_regular));
            tabIncomeImage.setBackgroundResource(R.drawable.bg_tab_selected_pill);
            tabIncomeImage.setTextColor(getResources().getColor(android.R.color.white));
        }
        loadCategoriesImage();
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
            tabIncome.setTextColor(getResources().getColor(R.color.home_text_regular)); // Use a grey color or black
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
                
                // Nếu user chưa có wallet nào, tự động tạo wallet mặc định
                if (wallets.isEmpty()) {
                    android.util.Log.d("AddTransaction", "No wallets found, creating default wallet...");
                    createDefaultWalletAndReload();
                    return;
                }
                
                // Nếu có wallet, tiếp tục hiển thị như bình thường

                //  Thêm item đầu tiên "-- Select wallet --"
                WalletResponse placeholder = new WalletResponse();
                placeholder.setId(0L);
                placeholder.setName(" Select your wallet ");
                placeholder.setBalance(BigDecimal.ZERO);
                placeholder.setCurrency("VND");
                wallets.add(0, placeholder);

                //  Thêm item cuối cùng "➕ Add new wallet"
                WalletResponse addNew = new WalletResponse();
                addNew.setId(-1L);
                addNew.setName("➕ Add new wallet");
                addNew.setBalance(BigDecimal.ZERO);
                addNew.setCurrency("VND");
                wallets.add(addNew);

                // 🟧 3. Gán adapter
                ArrayAdapter<WalletResponse> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        wallets
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spWallet.setAdapter(adapter);

                // 🟨 4. Gán sự kiện chọn
                spWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        WalletResponse selected = (WalletResponse) parent.getItemAtPosition(position);

                        if (selected.getId() == -1L) { //  Add new wallet
                            openAddWalletFragment();
                            spWallet.setSelection(0); // reset lại về "-- Select wallet --"
                        } else if (selected.getId() == 0L) {
                            // "-- Select wallet --" → bỏ qua
                            selectedCategory = null;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(Call<List<WalletResponse>> call, Throwable t) {
                //  ếu server lỗi → vẫn hiển thị Add new wallet
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
        });
    }
    private void openAddWalletFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new AddWalletFragment())
                .addToBackStack(null)
                .commit();
    }
    
    /**
     * Tạo wallet mặc định cho user mới nếu chưa có wallet nào
     */
    private void createDefaultWalletAndReload() {
        WalletRequest request = new WalletRequest("My Wallet", BigDecimal.ZERO, "VND");
        walletApiService.createWallet(request).enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("AddTransaction", "✅ Default wallet created successfully");
                    // Reload wallets sau khi tạo thành công
                    loadWallets();
                } else {
                    android.util.Log.e("AddTransaction", "Failed to create default wallet: " + response.code());
                    // Vẫn hiển thị UI với option Add new wallet
                    showWalletsWithAddOption();
                }
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                android.util.Log.e("AddTransaction", "Failed to create default wallet", t);
                // Vẫn hiển thị UI với option Add new wallet
                showWalletsWithAddOption();
            }
        });
    }
    
    /**
     * Hiển thị spinner với option Add new wallet (khi không có wallet hoặc tạo wallet fail)
     */
    private void showWalletsWithAddOption() {
        List<WalletResponse> wallets = new ArrayList<>();
        
        WalletResponse placeholder = new WalletResponse();
        placeholder.setId(0L);
        placeholder.setName(" Select your wallet ");
        placeholder.setBalance(BigDecimal.ZERO);
        placeholder.setCurrency("VND");
        wallets.add(placeholder);
        
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
    
    /**
     * Tạo wallet mặc định và reload cho image tab
     */
    private void createDefaultWalletAndReloadImage() {
        WalletRequest request = new WalletRequest("My Wallet", BigDecimal.ZERO, "VND");
        walletApiService.createWallet(request).enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("AddTransaction", "✅ Default wallet created successfully (image tab)");
                    // Reload wallets sau khi tạo thành công
                    loadWalletsImage();
                } else {
                    android.util.Log.e("AddTransaction", "Failed to create default wallet: " + response.code());
                    // Vẫn hiển thị UI với option Add new wallet
                    showWalletsWithAddOptionImage();
                }
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                android.util.Log.e("AddTransaction", "Failed to create default wallet", t);
                // Vẫn hiển thị UI với option Add new wallet
                showWalletsWithAddOptionImage();
            }
        });
    }
    
    /**
     * Hiển thị spinner với option Add new wallet cho image tab
     */
    private void showWalletsWithAddOptionImage() {
        List<WalletResponse> wallets = new ArrayList<>();
        
        WalletResponse placeholder = new WalletResponse();
        placeholder.setId(0L);
        placeholder.setName(" Select your wallet ");
        placeholder.setBalance(BigDecimal.ZERO);
        placeholder.setCurrency("VND");
        wallets.add(placeholder);
        
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
        spWalletImage.setAdapter(adapter);
        
        spWalletImage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WalletResponse selected = (WalletResponse) parent.getItemAtPosition(position);
                if (selected.getId() == -1L) {
                    openAddWalletFragment();
                    spWalletImage.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWallets(); // luôn load lại ví mới

        // Kiểm tra xem có ví nào được chọn không
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        long selectedWalletId = prefs.getLong("selected_wallet_id", -1);

        if (selectedWalletId != -1) {
            spWallet.postDelayed(() -> selectWalletById(selectedWalletId), 400);
        }
    }

    private void selectWalletById(long walletId) {
        ArrayAdapter<WalletResponse> adapter = (ArrayAdapter<WalletResponse>) spWallet.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            WalletResponse w = adapter.getItem(i);
            if (w != null && w.getId() == walletId) {
                spWallet.setSelection(i);
                break;
            }
        }

        // Sau khi chọn xong thì xoá ID để tránh auto-select lại lần sau
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("selected_wallet_id").apply();
    }



    // Format số tiền - Manual entry
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
    
    // Format số tiền - Image entry
    private void setupAmountFormattingImage() {
        etAmountImage.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmountImage.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                        String formatted = formatter.format(parsed) + " VND";
                        current = formatted;
                        etAmountImage.setText(formatted);
                        etAmountImage.setSelection(formatted.length() - 4);
                    } else {
                        current = "";
                        etAmountImage.setText("");
                    }
                    etAmountImage.addTextChangedListener(this);
                }
            }
        });
    }

    // Nút lưu - xử lý cả 2 tabs
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if ("manual".equals(currentTab)) {
                saveManualTransaction();
            } else {
                saveImageTransaction();
            }
        });
    }
    
    private void saveManualTransaction() {
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
    }
    
    private void saveImageTransaction() {
        String amountStr = etAmountImage.getText().toString().trim().replaceAll("[^\\d]", "");
        String note = etNoteImage.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryImage == null) {
            Toast.makeText(getContext(), getString(R.string.please_select_category), Toast.LENGTH_SHORT).show();
            return;
        }

        WalletResponse wal = (WalletResponse) spWalletImage.getSelectedItem();
        if (wal == null || wal.getId() == 0L) {
            Toast.makeText(getContext(), getString(R.string.please_choose_wallet), Toast.LENGTH_SHORT).show();
            return;
        }

        if (wal.getId() == -1L) {
            openAddWalletFragment();
            return;
        }

        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        String formattedDate = apiFormat.format(selectedDateImage.getTime());

        TransactionRequest req = new TransactionRequest(
                wal.getId(),
                selectedCategoryImage.getId(),
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
    
    private void showDatePickerDialogImage() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDateImage.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(calendar.getTime());
                    tvDateImage.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    
    /**
     * Load categories for image entry tab
     */
    private void loadCategoriesImage() {
        CategoryApiService categoryApi = ApiClient.getClient(requireContext()).create(CategoryApiService.class);
        categoryApi.getCategories(selectedType).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    setupCompactRecyclerImage(res.body());
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                // Ignore
            }
        });
    }
    
    private void setupCompactRecyclerImage(List<CategoryResponse> categories) {
        rvCategoryCompactImage.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        int limit = Math.min(4, categories.size());
        List<CategoryResponse> limited = categories.subList(0, limit);

        CategoryCompactAdapter adapter = new CategoryCompactAdapter(
                limited,
                category -> selectedCategoryImage = category,
                () -> showAllCategoriesBottomSheetImage(categories)
        );

        rvCategoryCompactImage.setAdapter(adapter);
    }
    
    private void showAllCategoriesBottomSheetImage(List<CategoryResponse> allCategoriesList) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_category_all, null);
        dialog.setContentView(sheetView);

        GridView gridAll = sheetView.findViewById(R.id.gridAllCategories);
        CategoryGridAdapter adapter = new CategoryGridAdapter(requireContext(), allCategoriesList);
        gridAll.setAdapter(adapter);

        if (selectedCategoryImage != null) {
            for (int i = 0; i < allCategoriesList.size(); i++) {
                if (allCategoriesList.get(i).getId() == selectedCategoryImage.getId()) {
                    adapter.setSelectedPosition(i);
                    break;
                }
            }
        }

        gridAll.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryImage = allCategoriesList.get(position);
            adapter.setSelectedPosition(position);

            if (rvCategoryCompactImage.getAdapter() instanceof CategoryCompactAdapter) {
                ((CategoryCompactAdapter) rvCategoryCompactImage.getAdapter()).setSelectedCategory(selectedCategoryImage);
            }

            dialog.dismiss();
        });

        dialog.show();
    }
    
    /**
     * Setup scan button for Receipt
     */
    private void setupScanButtons() {
        btnScanReceipt.setOnClickListener(v -> {
            if (!cameraHelper.hasCameraPermission()) {
                cameraHelper.requestCameraPermission();
                return;
            }
            showImageSourceDialog(false);
        });
    }
    
    /**
     * Show dialog to choose image source (camera or gallery)
     */
    private void showImageSourceDialog(boolean isQRScan) {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Scan Receipt")
                .setItems(options, (dialog, which) -> {
                    currentScanMode = "receipt";
                    if (which == 0) {
                        // Camera
                        Intent cameraIntent = cameraHelper.createTakePictureIntent();
                        if (cameraIntent != null) {
                            cameraLauncher.launch(cameraIntent);
                        }
                    } else {
                        // Gallery
                        imagePickerLauncher.launch("image/*");
                    }
                })
                .show();
    }
    
    /**
     * Setup camera launchers
     */
    private void setupCameraLaunchers() {
        try {
            cameraLauncher = cameraHelper.createCameraLauncher(new CameraHelper.ImageCaptureCallback() {
                @Override
                public void onImageCaptured(Bitmap bitmap) {
                    if (bitmap != null && !bitmap.isRecycled() && isAdded() && getContext() != null) {
                        Bitmap bitmapCopy = null;
                        try {
                            bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
                        } catch (Exception e) {
                            android.util.Log.e("AddTransaction", "Error copying bitmap", e);
                            bitmapCopy = bitmap;
                        }
                        
                        if (bitmapCopy != null) {
                            processImage(bitmapCopy);
                        } else {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), getString(R.string.cannot_process_image), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });

            imagePickerLauncher = cameraHelper.createImagePickerLauncher(new CameraHelper.ImageCaptureCallback() {
                @Override
                public void onImageCaptured(Bitmap bitmap) {
                    if (bitmap != null && !bitmap.isRecycled() && isAdded() && getContext() != null) {
                        Bitmap bitmapCopy = null;
                        try {
                            bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
                        } catch (Exception e) {
                            android.util.Log.e("AddTransaction", "Error copying bitmap", e);
                            bitmapCopy = bitmap;
                        }
                        
                        if (bitmapCopy != null) {
                            processImage(bitmapCopy);
                        } else {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), getString(R.string.cannot_process_image), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AddTransaction", "Error setting up camera", e);
        }
    }
    
    /**
     * Process image after capture/selection - Only for receipt scanning
     */
    private void processImage(Bitmap bitmap) {
        if (bitmap == null || !isAdded() || getContext() == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Resize if needed
        Bitmap processedBitmap = resizeBitmapIfNeeded(bitmap);
        if (processedBitmap == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Unable to process image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show preview
        imgPreview.setImageBitmap(processedBitmap);
        imgPreview.setVisibility(View.VISIBLE);
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        // OCR receipt
        ocrHelper.recognizeText(processedBitmap, new ReceiptOCRHelper.OCRCallback() {
            @Override
            public void onSuccess(String extractedText) {
                if (!isAdded() || getContext() == null) return;
                
                requireActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.receipt_read_analyzing), Toast.LENGTH_SHORT).show();
                    
                    TransactionAIExtractor.ExtractedTransaction transaction = 
                        ocrHelper.extractTransactionFromReceipt(extractedText);
                    
                    if (transaction != null && transaction.getAmount() != null) {
                        displayTransactionFromExtraction(transaction);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.cannot_extract_from_receipt), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded() || getContext() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Display extracted transaction in form fields
     */
    private void displayTransactionFromExtraction(TransactionAIExtractor.ExtractedTransaction transaction) {
        if (transaction == null || transaction.getAmount() == null) return;
        
        // Set type first (this will load categories)
        if ("INCOME".equals(transaction.getType())) {
            selectTypeImage("INCOME");
        } else {
            selectTypeImage("EXPENSE");
        }
        
        // Show form fields
        layoutTypeTabsImage.setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.tvAmountLabel).setVisibility(View.VISIBLE);
        etAmountImage.setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.tvNoteLabelImage).setVisibility(View.VISIBLE);
        etNoteImage.setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.tvCategoryLabelImage).setVisibility(View.VISIBLE);
        rvCategoryCompactImage.setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.tvDateLabelImage).setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.layoutDateImage).setVisibility(View.VISIBLE);
        layoutImageEntry.findViewById(R.id.tvWalletLabelImage).setVisibility(View.VISIBLE);
        spWalletImage.setVisibility(View.VISIBLE);
        
        // Fill amount - Set text directly (listener will handle formatting)
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formatted = formatter.format(transaction.getAmount().longValue()) + " VND";
        etAmountImage.setText(formatted);
        
        // Fill note
        if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
            etNoteImage.setText(transaction.getNote());
        }
        
        // Set date
        if (transaction.getTransactionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            tvDateImage.setText(sdf.format(transaction.getTransactionDate()));
            selectedDateImage.setTime(transaction.getTransactionDate());
        }
        
        // Load categories first, then find and select
        CategoryApiService categoryApi = ApiClient.getClient(requireContext()).create(CategoryApiService.class);
        categoryApi.getCategories(transaction.getType()).enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    List<CategoryResponse> categories = res.body();
                    setupCompactRecyclerImage(categories);
                    
                    // Find and select category
                    if (transaction.getSuggestedCategory() != null) {
                        findAndSelectCategory(transaction.getSuggestedCategory(), categories);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                // Ignore
            }
        });
        
        // Load wallets for image tab
        loadWalletsImage();
    }
    
    private void findAndSelectCategory(String categoryName, List<CategoryResponse> categories) {
        if (categoryName == null || categories == null) return;
        
        String lowerCategoryName = categoryName.toLowerCase();
        for (CategoryResponse cat : categories) {
            if (cat.getName().toLowerCase().equals(lowerCategoryName) || 
                cat.getName().toLowerCase().contains(lowerCategoryName) ||
                lowerCategoryName.contains(cat.getName().toLowerCase())) {
                selectedCategoryImage = cat;
                if (rvCategoryCompactImage.getAdapter() instanceof CategoryCompactAdapter) {
                    ((CategoryCompactAdapter) rvCategoryCompactImage.getAdapter()).setSelectedCategory(cat);
                }
                break;
            }
        }
    }
    
    private void loadWalletsImage() {
        api.getWallets().enqueue(new Callback<List<WalletResponse>>() {
            @Override
            public void onResponse(Call<List<WalletResponse>> call, Response<List<WalletResponse>> res) {
                List<WalletResponse> wallets = new ArrayList<>();
                if (res.isSuccessful() && res.body() != null) {
                    wallets.addAll(res.body());
                }
                
                // Nếu user chưa có wallet nào, tự động tạo wallet mặc định
                if (wallets.isEmpty()) {
                    android.util.Log.d("AddTransaction", "No wallets found in image tab, creating default wallet...");
                    createDefaultWalletAndReloadImage();
                    return;
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
                spWalletImage.setAdapter(adapter);
                
                spWalletImage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        WalletResponse selected = (WalletResponse) parent.getItemAtPosition(position);
                        if (selected.getId() == -1L) {
                            openAddWalletFragment();
                            spWalletImage.setSelection(0);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(Call<List<WalletResponse>> call, Throwable t) {
                // Ignore
            }
        });
    }
    
    /**
     * Resize bitmap if needed to avoid OOM
     */
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        int maxDimension = 1024;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        try {
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        } catch (Exception e) {
            android.util.Log.e("AddTransaction", "Error resizing bitmap", e);
            return bitmap;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ocrHelper != null) {
            ocrHelper.close();
        }
    }

}
