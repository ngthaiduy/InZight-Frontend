package com.example.inzightapp.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.AdminOrderAdapter;
import com.example.inzightapp.model.response.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private TextView tvTotalRevenue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_order, container, false);

        recyclerView = view.findViewById(R.id.rvOrderList);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);

        setupRecyclerView();
        loadMockData();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMockData() {
        List<OrderResponse> mockList = new ArrayList<>();
        // Giả sử OrderResponse có constructor đầy đủ tham số
        // Nếu không, bạn dùng Setter tương tự như PostResponse
        mockList.add(new OrderResponse(1001L, "2025-11-28", "john.doe@gmail.com", 99.00, "SUCCESS"));
        mockList.add(new OrderResponse(1002L, "2025-11-27", "jane.smith@email.com", 19.99, "SUCCESS"));
        mockList.add(new OrderResponse(1003L, "2025-11-26", "bob.fake@test.com", 50.00, "FAILED"));
        mockList.add(new OrderResponse(1004L, "2025-11-25", "alice.wonder@real.com", 200.00, "SUCCESS"));

        adapter.setOrders(mockList);
        calculateTotal(mockList);
    }

    private void calculateTotal(List<OrderResponse> list) {
        double total = 0;
        for (OrderResponse item : list) {
            if ("SUCCESS".equalsIgnoreCase(item.getStatus())) {
                total += item.getAmount();
            }
        }
        tvTotalRevenue.setText("$" + String.format("%.2f", total));
    }
}