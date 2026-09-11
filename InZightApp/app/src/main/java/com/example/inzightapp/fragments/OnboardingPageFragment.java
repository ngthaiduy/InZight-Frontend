package com.example.inzightapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;

public class OnboardingPageFragment extends Fragment {
    
    private static final String ARG_POSITION = "position";
    private int position;
    
    public static OnboardingPageFragment newInstance(int position) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, 0);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_page, container, false);
        
        ImageView imgOnboarding = view.findViewById(R.id.imgOnboarding);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        
        // Set content based on position
        switch (position) {
            case 0:
                imgOnboarding.setImageResource(R.mipmap.ic_logo_round);
                tvTitle.setText("FINTERTAINMENT");
                tvDescription.setText("Money Manage,  Achieve Goal,  Control Finance, Social Media Sharing");
                break;
            case 1:
                imgOnboarding.setImageResource(R.mipmap.ic_onboarding1);
                tvTitle.setText("Spend & Learn");
                tvDescription.setText("Earn financial knowledge\nby playing exciting games!");
                break;
            case 2:
                imgOnboarding.setImageResource(R.mipmap.ic_onboarding2);
                tvTitle.setText("Track Expenses");
                tvDescription.setText("Manage your finances\nwith ease and efficiency");
                break;
            case 3:
                imgOnboarding.setImageResource(R.mipmap.ic_onboarding3);
                tvTitle.setText("Connect & Share");
                tvDescription.setText("Engage with friends\nand share financial insights");
                break;
        }
        
        return view;
    }
}

