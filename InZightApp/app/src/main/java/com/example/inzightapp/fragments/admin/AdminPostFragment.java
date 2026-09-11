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
import com.example.inzightapp.adapter.AdminPostAdapter;
import com.example.inzightapp.model.response.PostResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminPostFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminPostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_post, container, false);

        // Ẩn filter bar vì model mới chưa có status
        View filterBox = view.findViewById(R.id.filterContainer);
        if (filterBox != null) filterBox.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.rvPostList);
        setupRecyclerView();
        loadMockData();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AdminPostAdapter(post -> {
            Toast.makeText(getContext(), "Deleted post ID: " + post.getId(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMockData() {
        List<PostResponse> list = new ArrayList<>();

        // Bài 1
        PostResponse p1 = new PostResponse();
        // LƯU Ý: Nếu PostResponse.java chưa có Setter, bạn phải tạo Setter (Generate -> Setter)
        try {
            // Dùng Reflection hoặc giả định bạn đã có Setter
            setField(p1, "id", 1L);
            setField(p1, "content", "Thị trường tài chính hôm nay biến động mạnh quá!");
            setField(p1, "fullName", "Nguyen Van A");
            setField(p1, "avatarUrl", "https://i.pravatar.cc/150?img=1");
            setField(p1, "imageUrl", "https://image.cnbcfm.com/api/v1/image/107086230-16575486952022-07-11t134212z_1814777477_rc2e5u9j9v5e_rtrmadp_0_usa-stocks.jpeg");
        } catch (Exception e) { e.printStackTrace(); }

        // Bài 2 (Không có ảnh)
        PostResponse p2 = new PostResponse();
        try {
            setField(p2, "id", 2L);
            setField(p2, "content", "Cần tư vấn cách tiết kiệm tiền hiệu quả cho sinh viên.");
            setField(p2, "fullName", "Tran Thi B");
            setField(p2, "avatarUrl", "https://i.pravatar.cc/150?img=5");
            setField(p2, "imageUrl", ""); // Không ảnh
        } catch (Exception e) { e.printStackTrace(); }

        list.add(p1);
        list.add(p2);

        adapter.setPosts(list);
    }

    // Helper function để tránh lỗi compile nếu bạn chưa tạo setter
    // Khi bạn đã tạo Setter trong PostResponse, hãy xóa hàm này và gọi p1.setId(...) trực tiếp
    private void setField(Object obj, String fieldName, Object value) {
        // Đây chỉ là giả lập, bạn hãy dùng p1.setContent("...") trong code thật
    }
}