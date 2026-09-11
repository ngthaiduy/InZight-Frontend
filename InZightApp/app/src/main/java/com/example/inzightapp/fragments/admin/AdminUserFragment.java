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
import com.example.inzightapp.adapter.AdminUserAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.model.response.PageResponse;
import com.example.inzightapp.model.response.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private UserApiService userApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate cái layout quản lý user cũ của bạn
        View view = inflater.inflate(R.layout.activity_admin_user_management, container, false);

        // Ẩn Header/Title cũ trong layout XML đi vì Activity chính sẽ lo phần đó
        // Hoặc bạn có thể giữ nguyên nếu muốn mỗi tab có header riêng

        userApiService = ApiClient.getClient(getContext()).create(UserApiService.class);
        recyclerView = view.findViewById(R.id.rvUserList);

        setupRecyclerView();
        loadUsers();

        // Xử lý nút FAB Add User
        view.findViewById(R.id.fabAddUser).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Add User Dialog", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(UserResponse user) {
                Toast.makeText(getContext(), "Edit: " + user.getUsername(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(UserResponse user) {
                Toast.makeText(getContext(), "Delete: " + user.getUsername(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        userApiService.getAllUsers(0, 20).enqueue(new Callback<PageResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<UserResponse>> call, Response<PageResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserResponse> list = response.body().getContent();
                    adapter.setUsers(list);
                }
            }

            @Override
            public void onFailure(Call<PageResponse<UserResponse>> call, Throwable t) {
                // Handle error
            }
        });
    }
}