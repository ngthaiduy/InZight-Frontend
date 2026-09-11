package com.example.inzightapp.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.model.response.TransactionResponse;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryDetailFragment extends Fragment {

    private TransactionResponse transaction;
    private long transactionId;

    public TransactionHistoryDetailFragment() {}

    public static TransactionHistoryDetailFragment newInstance(TransactionResponse transaction) {
        TransactionHistoryDetailFragment fragment = new TransactionHistoryDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("transaction", transaction);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction_history_detail, container, false);

        if (getArguments() != null) {
            transaction = (TransactionResponse) getArguments().getSerializable("transaction");
            transactionId = transaction.getId();
        }

        // Ánh xạ View
        ImageView ivIcon = v.findViewById(R.id.ivIcon);
        TextView tvType = v.findViewById(R.id.tvType);
        TextView tvAmount = v.findViewById(R.id.tvAmount);
        EditText etNote = v.findViewById(R.id.etNote);
        TextView tvWallet = v.findViewById(R.id.tvWallet);
        TextView tvDate = v.findViewById(R.id.tvDate);
        TextView tvCategory = v.findViewById(R.id.tvCategory);

        LinearLayout btnDeleteContainer = v.findViewById(R.id.btnDeleteContainer);
        LinearLayout btnEditContainer = v.findViewById(R.id.btnEditContainer);

        // Gán dữ liệu
        if (transaction != null) {
            // Loại giao dịch
            String typeText = transaction.getType().equalsIgnoreCase("INCOME") ? "Income" : "Expense";
            tvType.setText(typeText);

            // Định dạng tiền tệ
            DecimalFormat df = new DecimalFormat("#,###");
            String amountStr = df.format(transaction.getAmount()) + " VND";
            if ("INCOME".equalsIgnoreCase(transaction.getType())) {
                tvAmount.setTextColor(getResources().getColor(R.color.green));
                tvAmount.setText("+" + amountStr);
            } else {
                tvAmount.setTextColor(getResources().getColor(R.color.red));
                tvAmount.setText("-" + amountStr);
            }

            // Ghi chú
            etNote.setText(transaction.getNote() != null && !transaction.getNote().isEmpty()
                    ? transaction.getNote() : "No note");

            // Ví, danh mục
            tvWallet.setText(transaction.getWalletName() != null ? transaction.getWalletName() : "Unknown");
            tvCategory.setText(transaction.getCategoryName() != null ? transaction.getCategoryName() : "Unknown");

            // Định dạng ngày
            String formattedDate;
            try {
                LocalDateTime dateTime = LocalDateTime.parse(transaction.getTransactionDate());
                formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
            } catch (Exception e) {
                formattedDate = "Unknown date";
            }
            tvDate.setText(formattedDate);

            // Icon
            Glide.with(requireContext())
                    .load(R.drawable.ic_transaction)
                    .into(ivIcon);
        }

        // 🗑️ Nút Delete
        btnDeleteContainer.setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteTransaction(transactionId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ✏️ Nút Edit
        btnEditContainer.setOnClickListener(view -> {
            TransactionEditFragment fragment = TransactionEditFragment.newInstance(transaction);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    private void deleteTransaction(long id) {
        TransactionApiService api = ApiClient.getClient(requireContext()).create(TransactionApiService.class);
        api.deleteTransaction(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
