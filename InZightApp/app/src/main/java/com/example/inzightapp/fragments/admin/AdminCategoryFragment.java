package com.example.inzightapp.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.AdminCategoryAdapter;
import com.example.inzightapp.model.response.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminCategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_category, container, false);

        recyclerView = view.findViewById(R.id.rvCategoryList);

        // Nút thêm mới
        view.findViewById(R.id.fabAddCategory).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Add Dialog", Toast.LENGTH_SHORT).show();
        });

        setupRecyclerView();
        loadMockData();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(category -> {
            Toast.makeText(getContext(), "Deleted: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMockData() {
        List<CategoryResponse> mockList = new ArrayList<>();

        CategoryResponse c1 = new CategoryResponse();
        c1.setId(1L); c1.setName("Food & Drink"); c1.setType("EXPENSE");
        c1.setIconUrl("https://cdn-icons-png.flaticon.com/512/3170/3170733.png");

        CategoryResponse c2 = new CategoryResponse();
        c2.setId(2L); c2.setName("Salary"); c2.setType("INCOME");
        c2.setIconUrl("https://cdn-icons-png.flaticon.com/512/2454/2454282.png");

        CategoryResponse c3 = new CategoryResponse();
        c3.setId(3L); c3.setName("Transportation"); c3.setType("EXPENSE");
        c3.setIconUrl("https://cdn-icons-png.flaticon.com/512/814/814587.png");

        mockList.add(c1);
        mockList.add(c2);
        mockList.add(c3);

        adapter.setCategories(mockList);
    }
}