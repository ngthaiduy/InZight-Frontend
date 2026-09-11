package com.example.inzightapp.view;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Social.CommentAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.CommentApiService;
import com.example.inzightapp.model.request.CommentRequest;
import com.example.inzightapp.model.response.CommentResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private EditText edtComment;
    private ImageView btnSend;
    private CommentAdapter adapter;
    private Long postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        rvComments = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        ImageView btnBack = findViewById(R.id.btnBack);

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this, null);
        rvComments.setAdapter(adapter);

        postId = getIntent().getLongExtra("postId", -1);
        if (postId != -1) loadComments();

        btnSend.setOnClickListener(v -> sendComment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadComments() {
        CommentApiService service = ApiClient.getClient(this).create(CommentApiService.class);
        service.getCommentsByPost(postId).enqueue(new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(CommentActivity.this, "Không thể tải bình luận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendComment() {
        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentApiService api = ApiClient.getClient(this).create(CommentApiService.class);
        CommentRequest request = new CommentRequest(content, postId);

        api.addComment(request).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CommentActivity.this, "Đã bình luận", Toast.LENGTH_SHORT).show();
                    edtComment.setText("");
                    loadComments();
                } else {
                    Toast.makeText(CommentActivity.this, "Không thể gửi bình luận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}