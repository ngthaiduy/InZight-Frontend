package com.example.inzightapp.adapter.Transaction;

import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.fragments.TransactionHistoryDetailFragment;
import com.example.inzightapp.model.response.TransactionResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionGroupAdapter extends RecyclerView.Adapter<TransactionGroupAdapter.ViewHolder> {

    private final Context context;
    private List<TransactionResponse> transactions;

    public TransactionGroupAdapter(Context context) {
        this.context = context;
    }

    public void setTransactions(List<TransactionResponse> list) {
        this.transactions = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionResponse item = transactions.get(position);

        holder.tvCategory.setText(item.getCategoryName());
        holder.tvWallet.setText(item.getWalletName());
        holder.tvDate.setText(formatDate(item.getTransactionDate()));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%,d₫", item.getAmount()));
        // Set category icon
        int iconRes = com.example.inzightapp.utils.CategoryUtils.getIconForCategory(item.getCategoryName());
        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.clearColorFilter(); // Remove any tint to show original icon colors

        if ("EXPENSE".equalsIgnoreCase(item.getType())) {
            holder.tvAmount.setTextColor(context.getColor(R.color.red));
            holder.tvAmount.setText("-" + holder.tvAmount.getText());
        } else {
            holder.tvAmount.setTextColor(context.getColor(R.color.green));
            holder.tvAmount.setText("+" + holder.tvAmount.getText());
        }

        holder.itemView.setOnClickListener(v -> {
            TransactionHistoryDetailFragment fragment = TransactionHistoryDetailFragment.newInstance(item);
            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    private String formatDate(String input) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(input);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return input;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvCategory, tvWallet, tvDate, tvAmount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvWallet = itemView.findViewById(R.id.tvWallet);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
