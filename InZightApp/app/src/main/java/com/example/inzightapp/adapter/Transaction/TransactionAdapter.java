package com.example.inzightapp.adapter.Transaction;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.fragments.TransactionHistoryDetailFragment;
import com.example.inzightapp.model.response.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionResponse> transactions = new ArrayList<>();
    private final Context context;

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setTransactions(List<TransactionResponse> list) {
        this.transactions = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        TransactionResponse item = transactions.get(position);

        holder.tvCategory.setText(item.getCategoryName());
        holder.tvWallet.setText(item.getWalletName());
        holder.tvAmount.setText(String.format("%,d₫", item.getAmount()));
        holder.tvNote.setText(item.getNote());
        holder.tvDate.setText(item.getTransactionDate());

        if ("EXPENSE".equals(item.getType())) {
            holder.tvAmount.setTextColor(context.getColor(R.color.red));
            holder.tvAmount.setText("-" + holder.tvAmount.getText());
        } else {
            holder.tvAmount.setTextColor(context.getColor(R.color.green));
            holder.tvAmount.setText("+" + holder.tvAmount.getText());
        }

        // 👉 Khi click vào item
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("transaction", item);

            TransactionHistoryDetailFragment fragment = new TransactionHistoryDetailFragment();
            fragment.setArguments(bundle);

            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvWallet, tvAmount, tvNote, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvWallet = itemView.findViewById(R.id.tvWallet);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
