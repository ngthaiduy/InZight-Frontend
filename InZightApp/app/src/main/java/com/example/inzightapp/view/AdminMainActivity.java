package com.example.inzightapp.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.inzightapp.R;
import com.example.inzightapp.fragments.admin.AdminCategoryFragment;
import com.example.inzightapp.fragments.admin.AdminOrderFragment;
import com.example.inzightapp.fragments.admin.AdminPostFragment;
import com.example.inzightapp.fragments.admin.AdminUserFragment;
import com.example.inzightapp.fragments.admin.AdminSettingFragment;
// Import các fragment placeholder khác ở đây
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigation);

        // Mặc định load User Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_container, new AdminUserFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_admin_users) {
                selectedFragment = new AdminUserFragment();
            } else if (id == R.id.nav_admin_categories){
                selectedFragment = new AdminCategoryFragment();
            } else if (id == R.id.nav_admin_posts){
                selectedFragment = new AdminPostFragment();
            } else if (id == R.id.nav_admin_orders){
                selectedFragment = new AdminOrderFragment();
            } else if (id == R.id.nav_admin_settings) {
                selectedFragment = new AdminSettingFragment();
            }


            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}