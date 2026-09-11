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

public class TransactionDetailFragment extends Fragment {

    private TransactionResponse transaction;
    private long transactionId;

    public TransactionDetailFragment() {}

    public static TransactionDetailFragment newInstance(TransactionResponse transaction) {
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("transaction", transaction);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        if (getArguments() != null) {
            transaction = (TransactionResponse) getArguments().getSerializable("transaction");
            transactionId = transaction.getId();
        }

        // View mapping
        ImageView ivIcon = v.findViewById(R.id.ivIcon);
        TextView tvType = v.findViewById(R.id.tvType);
        TextView tvAmount = v.findViewById(R.id.tvAmount);
        EditText etNote = v.findViewById(R.id.etNote);
        TextView tvWallet = v.findViewById(R.id.tvWallet);
        TextView tvDate = v.findViewById(R.id.tvDate);
        TextView tvCategory = v.findViewById(R.id.tvCategory);

        // Container buttons (LinearLayout)
        LinearLayout btnEditContainer = v.findViewById(R.id.btnEditContainer);
        LinearLayout btnDeleteContainer = v.findViewById(R.id.btnDeleteContainer);

        // Bottom buttons
        Button btnAddNew = v.findViewById(R.id.btnAddNew);
        Button btnDone = v.findViewById(R.id.btnDone);

        // Bind transaction data
        if (transaction != null) {
            tvType.setText(transaction.getType().equals("INCOME") ? "Income" : "Expense");

            DecimalFormat df = new DecimalFormat("#,###");
            String amountText = (transaction.getType().equals("INCOME") ? "+" : "-")
                    + df.format(transaction.getAmount()) + " VND";
            tvAmount.setText(amountText);

            etNote.setText(transaction.getNote());
            tvWallet.setText(transaction.getWalletName());
            tvCategory.setText(transaction.getCategoryName());

            // Format date
            String formattedDate;
            try {
                LocalDateTime dateTime = LocalDateTime.parse(transaction.getTransactionDate());
                formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()));
            } catch (Exception e) {
                formattedDate = "Unknown";
            }
            tvDate.setText("Date: " + formattedDate);

            // Icon based on transaction type
            Glide.with(requireContext())
                    .load(R.drawable.ic_transaction)
                    .into(ivIcon);
            tvAmount.setTextColor(getResources().getColor(R.color.blue_500));
        }

        // ✅ EDIT button
        btnEditContainer.setOnClickListener(view -> {
            TransactionEditFragment fragment = TransactionEditFragment.newInstance(transaction);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ✅ DELETE button
        btnDeleteContainer.setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteTransaction(transactionId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // ✅ ADD NEW → Go to AddTransactionFragment
        btnAddNew.setOnClickListener(view -> {
            AddTransactionFragment fragment = new AddTransactionFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ✅ DONE → Go Home
        btnDone.setOnClickListener(view -> {
            HomeFragment homeFragment = new HomeFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();
        });

        return v;
    }

    // ✅ API delete function
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
