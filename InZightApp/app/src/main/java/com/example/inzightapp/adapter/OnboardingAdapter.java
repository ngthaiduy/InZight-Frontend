package com.example.inzightapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.inzightapp.R;
import com.example.inzightapp.fragments.OnboardingPageFragment;

public class OnboardingAdapter extends FragmentStateAdapter {
    
    private static final int PAGE_COUNT = 4;
    
    public OnboardingAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OnboardingPageFragment.newInstance(position);
    }
    
    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}

