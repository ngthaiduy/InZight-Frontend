package com.example.inzightapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inzightapp.R;
import com.example.inzightapp.adapter.Social.PostAdapter;
import com.example.inzightapp.api.ApiClient;
import com.example.inzightapp.api.social.PostApiService;
import com.example.inzightapp.databinding.FragmentSocialBinding;
import com.example.inzightapp.model.response.PostResponse;
import com.example.inzightapp.view.CreatePostActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialFragment extends Fragment {

    private FragmentSocialBinding binding;
    private PostAdapter postAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSocialBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupListeners();
        loadPosts();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.recyclerPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext());
        binding.recyclerPosts.setAdapter(postAdapter);
    }

    private void setupListeners() {
        binding.fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreatePostActivity.class);
            startActivityForResult(intent, 1001);
        });

        binding.btnNotification.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.open_notifications), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            loadPosts(); // Reload post list after creating a new post
        }
    }

    private void loadPosts() {
        binding.progressBar.setVisibility(View.VISIBLE); // Show loading
        binding.recyclerPosts.setVisibility(View.GONE); // Hide list

        PostApiService apiService = ApiClient.getClient(requireContext()).create(PostApiService.class);

        apiService.getAllPosts().enqueue(new Callback<List<PostResponse>>() {
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) {
                binding.progressBar.setVisibility(View.GONE); // Hide loading
                binding.recyclerPosts.setVisibility(View.VISIBLE); // Show list

                if (response.isSuccessful() && response.body() != null) {
                    List<PostResponse> posts = response.body();
                    
                    // Filter out Fin admin posts - chỉ hiển thị posts của người dùng thật
                    List<PostResponse> filteredPosts = new ArrayList<>();
                    for (PostResponse post : posts) {
                        // Loại bỏ posts của Fin admin (username là "Fin" hoặc "fin")
                        if (post.getUsername() != null && 
                            !post.getUsername().equalsIgnoreCase("Fin") &&
                            !post.getUsername().equalsIgnoreCase("fin")) {
                            filteredPosts.add(post);
                        }
                    }
                    
                    if (filteredPosts.isEmpty()) {
                        Toast.makeText(getContext(), getString(R.string.no_posts_available), Toast.LENGTH_SHORT).show();
                    }
                    postAdapter.setPostList(filteredPosts);
                } else {
                    Toast.makeText(getContext(), getString(R.string.unable_to_load_posts), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE); // Hide loading
                binding.recyclerPosts.setVisibility(View.VISIBLE); // Show list (even if empty/error)
                Toast.makeText(getContext(), getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
