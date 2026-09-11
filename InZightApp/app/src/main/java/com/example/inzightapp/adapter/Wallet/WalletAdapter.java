package com.example.inzightapp.adapter.Wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.finance.WalletApiService;
import com.example.inzightapp.model.response.WalletResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private final Context context;
    private List<WalletResponse> wallets;
    private final Runnable reloadCallback;

    public WalletAdapter(Context context, List<WalletResponse> wallets, Runnable reloadCallback) {
        this.context = context;
        this.wallets = wallets;
        this.reloadCallback = reloadCallback;
    }

    public void setWallets(List<WalletResponse> list) {
        this.wallets = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalletAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wallet_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletAdapter.ViewHolder holder, int position) {
        WalletResponse item = wallets.get(position);

        holder.tvName.setText(item.getName());
        holder.tvBalance.setText(String.format("%,.0f %s", item.getBalance(), item.getCurrency()));

        holder.itemView.animate()
                .scaleX(0.96f).scaleY(0.96f)
                .setDuration(100)
                .withEndAction(() -> holder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();

        // 🟢 Khi bấm vào 1 ví -> lưu lại ID ví -> quay lại fragment trước
        holder.itemView.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putLong("selected_wallet_id", item.getId())
                    .apply();

            ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
        });

        //  Khi bấm nút delete
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm delete")
                    .setMessage("If you delete wallet '" + item.getName() +
                            "', all related transaction history will be lost. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteWallet(item))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteWallet(WalletResponse wallet) {
        WalletApiService api = ApiClient.getClient(context).create(WalletApiService.class);
        api.deleteWallet(wallet.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Wallet deleted", Toast.LENGTH_SHORT).show();
                    reloadCallback.run();
                } else {
                    Toast.makeText(context, "Failed to delete wallet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return wallets != null ? wallets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBalance;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvWalletName);
            tvBalance = itemView.findViewById(R.id.tvWalletBalance);
            btnDelete = itemView.findViewById(R.id.btnDeleteWallet);
        }
    }
}
