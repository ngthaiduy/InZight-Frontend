package com.example.inzightapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.model.response.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<OrderResponse> list = new ArrayList<>();

    public void setOrders(List<OrderResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse item = list.get(position);

        holder.tvId.setText("#ORD-" + item.getId());
        holder.tvDate.setText(item.getOrderDate());
        holder.tvUser.setText(item.getUserEmail()); // Email người mua
        holder.tvAmount.setText("$" + item.getAmount());
        holder.tvStatus.setText(item.getStatus());

        // Logic màu sắc trạng thái
        if ("SUCCESS".equalsIgnoreCase(item.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá đậm
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9")); // Xanh lá nhạt
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#C62828")); // Đỏ đậm
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvDate, tvUser, tvAmount, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvUser = itemView.findViewById(R.id.tvOrderUser);
            tvAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}