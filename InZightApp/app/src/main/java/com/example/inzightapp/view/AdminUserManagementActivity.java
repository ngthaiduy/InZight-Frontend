package com.example.inzightapp.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.user.UserApiService;
import com.example.inzightapp.adapter.AdminUserAdapter;
import com.example.inzightapp.model.request.AdminCreateUserRequest;
import com.example.inzightapp.model.request.AdminUpdateUserRequest;
import com.example.inzightapp.model.response.PageResponse;
import com.example.inzightapp.model.response.UserResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class AdminUserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private UserApiService userApiService;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        userApiService = ApiClient.getClient(this).create(UserApiService.class);

        recyclerView = findViewById(R.id.rvUserList);
        fabAdd = findViewById(R.id.fabAddUser);

        setupRecyclerView();
        loadUsers();

        fabAdd.setOnClickListener(v -> showAddUserDialog());
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(UserResponse user) {
                showEditUserDialog(user);
            }

            @Override
            public void onDelete(UserResponse user) {
                confirmDelete(user);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // ... imports

    private void loadUsers() {
        // Gọi API lấy trang 0, size 20 (hoặc số khác tùy bạn)
        userApiService.getAllUsers(0, 20).enqueue(new Callback<PageResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<UserResponse>> call, Response<PageResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lấy lớp vỏ PageResponse
                    PageResponse<UserResponse> pageData = response.body();

                    // 2. Lấy danh sách List<UserResponse> từ bên trong "content"
                    List<UserResponse> listUsers = pageData.getContent();

                    // 3. Đưa vào Adapter hiển thị
                    if (listUsers != null && !listUsers.isEmpty()) {
                        adapter.setUsers(listUsers);
                    } else {
                        Toast.makeText(AdminUserManagementActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    }

                    // Debug: In ra log xem có bao nhiêu user
                    // Log.d("ADMIN_USER", "Loaded users: " + listUsers.size());

                } else {
                    Toast.makeText(AdminUserManagementActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<UserResponse>> call, Throwable t) {
                Toast.makeText(AdminUserManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- LOGIC XÓA USER ---
    private void confirmDelete(UserResponse user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userApiService.deleteUser(user.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminUserManagementActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                loadUsers(); // Load lại list
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // --- LOGIC THÊM/SỬA (Dùng Dialog đơn giản) ---
    // Bạn có thể tự tạo Dialog Custom đẹp hơn
    private void showAddUserDialog() {
        // Code hiển thị Dialog nhập username, pass, email...
        // Sau đó gọi userApiService.createUser(...)
    }

    private void showEditUserDialog(UserResponse user) {
        // Code hiển thị Dialog sửa thông tin
        // Sau đó gọi userApiService.updateUser(...)
    }
}