package com.example.inzightapp.eventInput;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.CommentApiService;
import com.example.inzightapp.model.request.CommentRequest;
import com.example.inzightapp.model.response.CommentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentInputDialog extends Dialog {

    public CommentInputDialog(Context context, Long postId, Runnable onCommentAdded) {
        super(context);
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_comment_input, null));
        setCancelable(true);

        EditText edtComment = findViewById(R.id.edtComment);
        ImageButton btnSend = findViewById(R.id.btnSendComment);

        CommentApiService commentApi = ApiClient.getClient(context).create(CommentApiService.class);

        btnSend.setOnClickListener(v -> {
            String content = edtComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            CommentRequest request = new CommentRequest(content, postId);
            commentApi.addComment(request).enqueue(new Callback<CommentResponse>() {
                @Override
                public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show();
                        dismiss();
                        onCommentAdded.run(); // callback to refresh the post
                    } else {
                        Toast.makeText(context, "Unable to send comment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CommentResponse> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
