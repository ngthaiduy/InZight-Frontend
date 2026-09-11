package com.example.inzightapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.PostApiService;
import com.example.inzightapp.model.request.PostRequest;
import com.example.inzightapp.model.response.PostResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etContent;
    private ImageView imgPreview;
    private ImageButton btnRemoveMedia;
    private FrameLayout cardMediaPreview;
    private TextView tvCharCount, tvUsername;
    private MenuItem btnPost;
    private ProgressBar progressLoading;
    private ShapeableImageView imgAvatar;
    private Uri selectedMedia;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        etContent = findViewById(R.id.etContent);
        imgPreview = findViewById(R.id.imgPreview);
        cardMediaPreview = findViewById(R.id.cardMediaPreview);
        btnRemoveMedia = findViewById(R.id.btnRemoveMedia);
        tvCharCount = findViewById(R.id.tvCharCount);
        progressLoading = findViewById(R.id.progressMedia);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsername = findViewById(R.id.tvUsername);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Load user info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("full_name", "User");
        String avatarUrl = prefs.getString("avatar_url", null);

        tvUsername.setText(fullName);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        // 🖼 Image picker
        findViewById(R.id.btnAddImage).setOnClickListener(v -> pickMedia.launch("image/*"));
        btnRemoveMedia.setOnClickListener(v -> removeMedia());

        // Character counter
        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(s.length() + "/1000");
                if (btnPost != null)
                    btnPost.setEnabled(s.length() > 0 || selectedMedia != null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Post button (Toolbar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        btnPost = menu.findItem(R.id.action_post);
        btnPost.setOnMenuItemClickListener(item -> {
            createPost();
            return true;
        });
        btnPost.setEnabled(false);
        return true;
    }

    // Image picker
    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    selectedMedia = uri;
                    cardMediaPreview.setVisibility(FrameLayout.VISIBLE);

                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(imgPreview);

                    if (btnPost != null) btnPost.setEnabled(true);
                }
            });

    private void removeMedia() {
        selectedMedia = null;
        cardMediaPreview.setVisibility(FrameLayout.GONE);
        if (btnPost != null)
            btnPost.setEnabled(etContent.getText().length() > 0);
    }

    // Send post to server
    private void createPost() {
        String content = etContent.getText().toString().trim();

        if (content.isEmpty() && selectedMedia == null) {
            Toast.makeText(this, getString(R.string.please_enter_text_or_image), Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);
        progressLoading.setVisibility(ProgressBar.VISIBLE);

        PostApiService api = ApiClient.getClient(this).create(PostApiService.class);

        // Tạo RequestBody cho content
        RequestBody contentBody = RequestBody.create(
                MediaType.parse("text/plain"),
                content != null ? content : ""
        );

        // Tạo MultipartBody.Part cho ảnh (có thể null)
        MultipartBody.Part imagePart = null;
        File imageFile = null;

        if (selectedMedia != null) {
            try {
                // Convert URI thành File
                imageFile = createFileFromUri(selectedMedia);
                if (imageFile != null && imageFile.exists()) {
                    // Tạo RequestBody cho file
                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse("image/*"),
                            imageFile
                    );
                    imagePart = MultipartBody.Part.createFormData(
                            "image",
                            imageFile.getName(),
                            requestFile
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error_processing_image), Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true);
                progressLoading.setVisibility(ProgressBar.GONE);
                return;
            }
        }

        // Gửi multipart request (imagePart có thể null)
        final File finalImageFile = imageFile;
        api.createPost(contentBody, imagePart).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                progressLoading.setVisibility(ProgressBar.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, getString(R.string.post_created_successfully), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CreatePostActivity.this, getString(R.string.failed_to_create_post), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                }
                // Xóa file tạm nếu có
                if (finalImageFile != null && finalImageFile.exists()) {
                    finalImageFile.delete();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                progressLoading.setVisibility(ProgressBar.GONE);
                Toast.makeText(CreatePostActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_LONG).show();
                btnPost.setEnabled(true);
                // Xóa file tạm nếu có
                if (finalImageFile != null && finalImageFile.exists()) {
                    finalImageFile.delete();
                }
            }
        });
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Tạo file tạm
            File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
