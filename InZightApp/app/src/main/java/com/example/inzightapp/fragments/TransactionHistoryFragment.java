package com.example.inzightapp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Transaction.TransactionGroupAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.TransactionApiService;
import com.example.inzightapp.model.response.TransactionResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView rvTransactions;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnStartDate, btnEndDate, btnSortTime;
    private ImageButton btnApplyFilter;
    private TransactionGroupAdapter adapter;
    private Calendar startCal, endCal;
    private boolean sortNewestFirst = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        rvTransactions = view.findViewById(R.id.rvTransactions);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnSortTime = view.findViewById(R.id.btnSortTime);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionGroupAdapter(getContext());
        rvTransactions.setAdapter(adapter);

        // phạm vi mặc định
        startCal = Calendar.getInstance();
        endCal = Calendar.getInstance();
        startCal.set(2025, Calendar.JANUARY, 1);
        endCal.set(2025, Calendar.DECEMBER, 31);

        btnStartDate.setOnClickListener(v -> pickDate(btnStartDate, startCal));
        btnEndDate.setOnClickListener(v -> pickDate(btnEndDate, endCal));
        btnSortTime.setOnClickListener(v -> toggleSort());
        btnApplyFilter.setOnClickListener(v -> loadTransactions());

        loadTransactions();
        return view;
    }

    private void pickDate(Button button, Calendar cal) {
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            cal.set(year, month, day);
            button.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void toggleSort() {
        sortNewestFirst = !sortNewestFirst;
        btnSortTime.setText(sortNewestFirst ? "Newest ▼" : "Oldest ▲");
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        TransactionApiService api = ApiClient.getClient(requireContext()).create(TransactionApiService.class);
        api.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TransactionResponse> list = response.body();
                    if (list == null || list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    boolean hasDateFilter = !(btnStartDate.getText().equals("Start date") || btnEndDate.getText().equals("End date"));
                    if (hasDateFilter) {
                        list.removeIf(tx -> {
                            try {
                                Date txDate = parseFlexibleDate(tx.getTransactionDate());
                                return txDate.before(startCal.getTime()) || txDate.after(endCal.getTime());
                            } catch (Exception e) {
                                return false;
                            }
                        });
                    }

                    list.sort((a, b) -> {
                        int cmp = b.getTransactionDate().compareTo(a.getTransactionDate());
                        return sortNewestFirst ? cmp : -cmp;
                    });

                    if (list.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                    else adapter.setTransactions(list);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Server connection failed.");
            }
        });
    }

    private Date parseFlexibleDate(String input) throws ParseException {
        if (input == null || input.isEmpty()) return new Date();
        List<String> formats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        );
        for (String f : formats) {
            try {
                return new SimpleDateFormat(f, Locale.getDefault())
                        .parse(input.substring(0, Math.min(input.length(), f.length())));
            } catch (Exception ignored) {}
        }
        return new Date();
    }
}
