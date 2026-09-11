package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.google.android.material.card.MaterialCardView;

public class RetirementMenuFragment extends Fragment {

    private ImageView btnBack;
    private MaterialCardView cardWithoutPension;
    private MaterialCardView cardLifePlanner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retirement_menu, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide Bottom Navigation
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Show Bottom Navigation when leaving
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        cardWithoutPension = view.findViewById(R.id.cardWithoutPension);
        cardLifePlanner = view.findViewById(R.id.cardLifePlanner);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        cardWithoutPension.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RetirementWithoutPensionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardLifePlanner.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RetirementLifePlannerFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
