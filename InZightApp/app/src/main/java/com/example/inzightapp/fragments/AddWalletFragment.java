package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Wallet.WalletAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.WalletApiService;
import com.example.inzightapp.model.request.WalletRequest;
import com.example.inzightapp.model.response.WalletResponse;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddWalletFragment extends Fragment {

    private EditText etName;
    private Button btnSave;
    private RecyclerView rvWallets;
    private WalletAdapter adapter;
    private WalletApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_wallet, container, false);

        etName = v.findViewById(R.id.etWalletName);
        btnSave = v.findViewById(R.id.btnSaveWallet);
        rvWallets = v.findViewById(R.id.rvWallets);
        api = ApiClient.getClient(requireContext()).create(WalletApiService.class);

        v.findViewById(R.id.btnBack).setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WalletAdapter(requireContext(), List.of(), this::loadWallets);
        rvWallets.setAdapter(adapter);

        btnSave.setOnClickListener(view -> saveWallet());

        loadWallets();
        return v;
    }

    private void loadWallets() {
        api.getWallets().enqueue(new Callback<List<WalletResponse>>() {
            @Override
            public void onResponse(Call<List<WalletResponse>> call, Response<List<WalletResponse>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    adapter.setWallets(res.body());
                }
            }

            @Override
            public void onFailure(Call<List<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.failed_to_load_wallets), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveWallet() {
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), getString(R.string.enter_wallet_name), Toast.LENGTH_SHORT).show();
            return;
        }

        WalletRequest request = new WalletRequest(name, BigDecimal.ZERO, "VND");
        api.createWallet(request).enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.wallet_created), Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    loadWallets();
                } else {
                    Toast.makeText(getContext(), getString(R.string.failed_to_create_wallet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_colon, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
