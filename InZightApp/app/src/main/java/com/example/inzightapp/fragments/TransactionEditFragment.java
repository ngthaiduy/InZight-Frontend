package com.example.inzightapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Category.CategoryCompactAdapter;
import com.example.inzightapp.adapter.Category.CategoryGridAdapter;
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

public class TransactionEditFragment extends Fragment {

    private EditText etAmount, etNote;
    private Spinner spWallet;
    private RecyclerView rvCategoryCompact;
    private TextView tabExpense, tabIncome, tvDate;
    private Button btnUpdate;
    private String selectedType = "EXPENSE";
    private TransactionApiService api;
    private TransactionResponse transaction;
    private List<CategoryResponse> allCategories = new ArrayList<>();
    private CategoryResponse selectedCategory;

    private Calendar selectedDate = Calendar.getInstance();

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction_edit, container, false);

        etAmount = v.findViewById(R.id.etAmount);
        etNote = v.findViewById(R.id.etNote);
        spWallet = v.findViewById(R.id.spWallet);
        rvCategoryCompact = v.findViewById(R.id.rvCategoryCompact);
        tabExpense = v.findViewById(R.id.tabExpense);
        tabIncome = v.findViewById(R.id.tabIncome);
        tvDate = v.findViewById(R.id.tvDate);
        ImageView btnPickDate = v.findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(view -> showDatePickerDialog());

        btnUpdate = v.findViewById(R.id.btnUpdateTransaction);

        api = ApiClient.getClient(requireContext()).create(TransactionApiService.class);

        if (getArguments() != null) {
            transaction = (TransactionResponse) getArguments().getSerializable("transaction");
        }

        setupTabs();
        setupAmountFormatting();
        loadWallets();
        loadCategories();
        setupUpdateButton();

        // Fill dữ liệu có sẵn
        if (transaction != null) {
            etAmount.setText(NumberFormat.getInstance(Locale.US).format(transaction.getAmount()) + " VND");
            etNote.setText(transaction.getNote());
            selectedType = transaction.getType();

            // Gán lại ngày từ transaction (nếu có)
            if (transaction.getTransactionDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(transaction.getTransactionDate());
                    if (date != null) {
                        selectedDate.setTime(date);
                        tvDate.setText(sdf.format(date));
                    }
                } catch (Exception e) {
                    tvDate.setText("Today");
                }
            } else {
                tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));
            }
        }

        ImageView btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
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
            tabExpense.setBackgroundResource(R.drawable.bg_tab_left_selected);
            tabExpense.setTextColor(getResources().getColor(android.R.color.white));
            tabIncome.setBackgroundResource(R.drawable.bg_tab_right_unselected);
            tabIncome.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            tabExpense.setBackgroundResource(R.drawable.bg_tab_left_unselected);
            tabExpense.setTextColor(getResources().getColor(android.R.color.black));
            tabIncome.setBackgroundResource(R.drawable.bg_tab_right_selected);
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
                    Toast.makeText(getContext(), "Load categories failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        gridAll.setAdapter((ListAdapter) adapter);

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

                // 🟦 Thêm item đầu tiên "-- Select wallet --"
                WalletResponse placeholder = new WalletResponse();
                placeholder.setId(0L);
                placeholder.setName(" Select your wallet ");
                placeholder.setBalance(BigDecimal.ZERO);
                placeholder.setCurrency("VND");
                wallets.add(0, placeholder);

                // 🟩 Thêm item cuối "➕ Add new wallet"
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
                ) {
                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0; // Dòng đầu không cho chọn
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        if (position == 0) tv.setTextColor(getResources().getColor(R.color.gray));
                        else tv.setTextColor(getResources().getColor(android.R.color.black));
                        return view;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spWallet.setAdapter(adapter);

                // 🔄 Gán đúng ví hiện tại khi edit
                if (transaction != null && transaction.getWalletName() != null) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        WalletResponse w = adapter.getItem(i);
                        if (w != null && transaction.getWalletName().equals(w.getName())) {
                            spWallet.setSelection(i);
                            break;
                        }
                    }
                }

                spWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        WalletResponse selected = (WalletResponse) parent.getItemAtPosition(position);

                        if (selected.getId() == -1L) { // Add new wallet
                            openAddWalletFragment();
                            spWallet.setSelection(0); // reset về "-- Select wallet --"
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
                placeholder.setName(" Select your wallet ");
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
                        if (selected.getId() == -1L) openAddWalletFragment();
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

    @Override
    public void onResume() {
        super.onResume();
        loadWallets();

        // Đọc lại ví đã chọn nếu có
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

        // Xóa ID đã lưu để không auto chọn lại lần sau
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("selected_wallet_id").apply();
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

    // Nút cập nhật
    private void setupUpdateButton() {
        btnUpdate.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim().replaceAll("[^\\d]", "");
            String note = etNote.getText().toString().trim();

            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategory == null) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            WalletResponse wal = (WalletResponse) spWallet.getSelectedItem();
            if (wal == null || wal.getId() == 0L) {
                Toast.makeText(getContext(), "Please choose your wallet", Toast.LENGTH_SHORT).show();
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

            api.updateTransaction(transaction.getId(), req).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        Toast.makeText(getContext(), "Transaction updated!", Toast.LENGTH_SHORT).show();
                        Fragment detail = TransactionDetailFragment.newInstance(res.body());
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, detail)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public static TransactionEditFragment newInstance(TransactionResponse transaction) {
        TransactionEditFragment fragment = new TransactionEditFragment();
        Bundle args = new Bundle();
        args.putSerializable("transaction", transaction);
        fragment.setArguments(args);
        return fragment;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate.set(year, month, dayOfMonth); //  Lưu ngày đã chọn
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
}
