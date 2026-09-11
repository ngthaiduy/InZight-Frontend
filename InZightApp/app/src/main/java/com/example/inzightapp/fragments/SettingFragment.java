package com.example.inzightapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.view.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingFragment extends Fragment {

    private CircleImageView imgAvatar;
    private TextView tvUsername, tvEmail, tvThemeValue, tvAppVersion;
    private SwitchMaterial switchTwoFactor, switchExpenseNotifications, switchBudgetAlerts, switchWeeklyReports;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        android.util.Log.d("SettingFragment", "onCreateView called");
        
        View view = null;
        try {
            android.util.Log.d("SettingFragment", "Inflating layout...");
            view = inflater.inflate(R.layout.fragment_setting, container, false);
            android.util.Log.d("SettingFragment", "Layout inflated successfully");
            
            if (view == null) {
                android.util.Log.e("SettingFragment", "Failed to inflate layout - view is null");
                return createErrorView("Layout inflation failed");
            }

            // Initialize views
            android.util.Log.d("SettingFragment", "Initializing views...");
            initViews(view);
            
            android.util.Log.d("SettingFragment", "Loading user data...");
            loadUserData();
            
            // Setup logout button directly from view to ensure it's found
            setupLogoutButton(view);
            
            android.util.Log.d("SettingFragment", "onCreateView completed successfully");
            return view;
        } catch (Exception e) {
            android.util.Log.e("SettingFragment", "Error in onCreateView", e);
            e.printStackTrace();
            return createErrorView("Error: " + e.getMessage());
        }
    }
    
    private View createErrorView(String message) {
        Context context = getContext();
        if (context == null) {
            android.util.Log.e("SettingFragment", "Context is null, cannot create error view");
            return null;
        }
        
        android.widget.LinearLayout errorLayout = new android.widget.LinearLayout(context);
        errorLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        errorLayout.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        errorLayout.setBackgroundColor(0xFFF7F9FC);
        errorLayout.setGravity(android.view.Gravity.CENTER);
        errorLayout.setPadding(32, 32, 32, 32);
        
        android.widget.TextView errorText = new android.widget.TextView(context);
        errorText.setText(message != null ? message : "Settings screen error");
        errorText.setTextColor(0xFF333333);
        errorText.setTextSize(16);
        errorText.setGravity(android.view.Gravity.CENTER);
        errorLayout.addView(errorText);
        
        return errorLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("SettingFragment", "onViewCreated called");
        
        // Setup listeners after view is created and attached
        android.util.Log.d("SettingFragment", "Setting up listeners...");
        setupListeners(view);
    }

    private void initViews(View view) {
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvThemeValue = view.findViewById(R.id.tvThemeValue);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);
        
        switchTwoFactor = view.findViewById(R.id.switchTwoFactor);
        switchExpenseNotifications = view.findViewById(R.id.switchExpenseNotifications);
        switchBudgetAlerts = view.findViewById(R.id.switchBudgetAlerts);
        switchWeeklyReports = view.findViewById(R.id.switchWeeklyReports);
        
        btnLogout = view.findViewById(R.id.btnLogout);
        android.util.Log.d("SettingFragment", "btnLogout initialized: " + (btnLogout != null));
    }

    private void loadUserData() {
        Context context = getContext();
        if (context == null) return;
        
        // Load user data from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Miru");
        String email = prefs.getString("email", "abc@gmail.com");
        String avatarUrl = prefs.getString("avatarUrl", null);

        if (tvUsername != null) tvUsername.setText(username);
        if (tvEmail != null) tvEmail.setText(email);

        // Load avatar
        if (imgAvatar != null) {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this).load(avatarUrl).into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        // Load settings (temporarily disable listeners to avoid triggering on load)
        if (switchTwoFactor != null) {
            switchTwoFactor.setOnCheckedChangeListener(null);
            switchTwoFactor.setChecked(prefs.getBoolean("twoFactorEnabled", false));
        }
        if (switchExpenseNotifications != null) {
            switchExpenseNotifications.setOnCheckedChangeListener(null);
            switchExpenseNotifications.setChecked(prefs.getBoolean("expenseNotifications", true));
        }
        if (switchBudgetAlerts != null) {
            switchBudgetAlerts.setOnCheckedChangeListener(null);
            switchBudgetAlerts.setChecked(prefs.getBoolean("budgetAlerts", true));
        }
        if (switchWeeklyReports != null) {
            switchWeeklyReports.setOnCheckedChangeListener(null);
            switchWeeklyReports.setChecked(prefs.getBoolean("weeklyReports", false));
        }
        
        // App version
        if (tvAppVersion != null) {
            tvAppVersion.setText(getString(R.string.version));
        }
    }

    private void setupLogoutButton(View view) {
        MaterialButton logoutBtn = view.findViewById(R.id.btnLogout);
        if (logoutBtn != null) {
            android.util.Log.d("SettingFragment", "Found logout button, setting up listener");
            logoutBtn.setOnClickListener(v -> {
                android.util.Log.d("SettingFragment", "Logout button clicked!");
                performLogout();
            });
        } else {
            android.util.Log.e("SettingFragment", "Logout button not found in view!");
        }
    }

    private void performLogout() {
        android.app.Activity activity = getActivity();
        if (activity == null) {
            android.util.Log.e("SettingFragment", "Activity is null, cannot logout");
            return;
        }
        
        try {
            android.util.Log.d("SettingFragment", "Starting logout process");
            
            // Clear all saved credentials and preferences
            SharedPreferences prefs = activity.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().commit();
            android.util.Log.d("SettingFragment", "SharedPreferences cleared");
            
            // Navigate to LoginActivity with flags to clear task stack
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            android.util.Log.d("SettingFragment", "LoginActivity started");
            
            // Finish all activities in the task to ensure logout works
            activity.finishAffinity();
            android.util.Log.d("SettingFragment", "finishAffinity called");
        } catch (Exception e) {
            android.util.Log.e("SettingFragment", "Error during logout", e);
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, getString(R.string.logout_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupListeners(View rootView) {
        Context context = getContext();
        if (rootView == null || context == null) {
            android.util.Log.e("SettingFragment", "setupListeners: rootView or context is null");
            return;
        }
        
        android.util.Log.d("SettingFragment", "setupListeners: Setting up all listeners");
        
        // Profile edit
        View btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.edit_profile), Toast.LENGTH_SHORT).show();
            });
        }

        // Settings items
        View itemChangePassword = rootView.findViewById(R.id.itemChangePassword);
        if (itemChangePassword != null) {
            itemChangePassword.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.change_password), Toast.LENGTH_SHORT).show();
            });
        }

        View itemManageDevices = rootView.findViewById(R.id.itemManageDevices);
        if (itemManageDevices != null) {
            itemManageDevices.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.manage_devices), Toast.LENGTH_SHORT).show();
            });
        }

        View itemMonthlyLimit = rootView.findViewById(R.id.itemMonthlyLimit);
        if (itemMonthlyLimit != null) {
            itemMonthlyLimit.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.set_monthly_limit), Toast.LENGTH_SHORT).show();
            });
        }

        View itemSavingGoals = rootView.findViewById(R.id.itemSavingGoals);
        if (itemSavingGoals != null) {
            itemSavingGoals.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.manage_saving_goals), Toast.LENGTH_SHORT).show();
            });
        }

        View itemTheme = rootView.findViewById(R.id.itemTheme);
        if (itemTheme != null) {
            itemTheme.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.theme), Toast.LENGTH_SHORT).show();
            });
        }

        View itemAccentColor = rootView.findViewById(R.id.itemAccentColor);
        if (itemAccentColor != null) {
            itemAccentColor.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.accent_color), Toast.LENGTH_SHORT).show();
            });
        }

        View itemPrivacyPolicy = rootView.findViewById(R.id.itemPrivacyPolicy);
        if (itemPrivacyPolicy != null) {
            itemPrivacyPolicy.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.privacy_policy), Toast.LENGTH_SHORT).show();
            });
        }

        View itemContactSupport = rootView.findViewById(R.id.itemContactSupport);
        if (itemContactSupport != null) {
            itemContactSupport.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.contact_support), Toast.LENGTH_SHORT).show();
            });
        }

        View itemAppFeedback = rootView.findViewById(R.id.itemAppFeedback);
        if (itemAppFeedback != null) {
            itemAppFeedback.setOnClickListener(v -> {
                Toast.makeText(context, getString(R.string.app_feedback), Toast.LENGTH_SHORT).show();
            });
        }

        // Switches
        if (switchTwoFactor != null) {
            switchTwoFactor.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("twoFactorEnabled", isChecked);
            });
        }

        if (switchExpenseNotifications != null) {
            switchExpenseNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("expenseNotifications", isChecked);
            });
        }

        if (switchBudgetAlerts != null) {
            switchBudgetAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("budgetAlerts", isChecked);
            });
        }

        if (switchWeeklyReports != null) {
            switchWeeklyReports.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("weeklyReports", isChecked);
            });
        }

        // Logout button is already set up in setupLogoutButton() method
    }

    private void saveSetting(String key, boolean value) {
        Context context = getContext();
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }
}
