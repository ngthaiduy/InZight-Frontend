package com.example.inzightapp.utils;

import android.annotation.SuppressLint;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.inzightapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;

public class BottomNavHelper {
    
    public static void setupBottomNavWithIndicator(BottomNavigationView bottomNav) {
        bottomNav.post(() -> {
            updateIndicators(bottomNav);
        });
    }
    
    @SuppressLint("RestrictedApi")
    public static void updateIndicators(BottomNavigationView bottomNav) {
        if (bottomNav.getChildCount() == 0) {
            return;
        }
        
        ViewGroup bottomNavMenuView = (ViewGroup) bottomNav.getChildAt(0);
        if (bottomNavMenuView == null) {
            return;
        }
        
        // Lấy menu và selected item ID để kiểm tra trạng thái checked
        android.view.Menu menu = bottomNav.getMenu();
        int selectedItemId = bottomNav.getSelectedItemId();
        
        for (int i = 0; i < bottomNavMenuView.getChildCount(); i++) {
            View itemView = bottomNavMenuView.getChildAt(i);
            if (itemView instanceof BottomNavigationItemView) {
                BottomNavigationItemView item = (BottomNavigationItemView) itemView;
                
                // Lấy MenuItem tương ứng để kiểm tra trạng thái
                MenuItem menuItem = null;
                if (i < menu.size()) {
                    menuItem = menu.getItem(i);
                }
                
                // Tìm ViewGroup chứa icon và text
                ViewGroup itemContainer = findItemContainer(item);
                if (itemContainer != null) {
                    // Xóa indicator cũ nếu có
                    View oldIndicator = itemContainer.findViewWithTag("indicator");
                    if (oldIndicator != null) {
                        itemContainer.removeView(oldIndicator);
                    }
                    
                    // Thêm indicator mới nếu item được chọn
                    boolean isChecked = false;
                    if (menuItem != null) {
                        isChecked = (menuItem.getItemId() == selectedItemId);
                    }
                    
                    if (isChecked) {
                        View indicator = createIndicator(bottomNav.getContext());
                        indicator.setTag("indicator");
                        itemContainer.addView(indicator, 0); // Thêm ở đầu
                    }
                }
            }
        }
    }
    
    private static ViewGroup findItemContainer(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            // Tìm LinearLayout hoặc FrameLayout chứa icon và text
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) child;
                    // Kiểm tra xem có chứa ImageView và TextView không
                    boolean hasIcon = false;
                    boolean hasText = false;
                    for (int j = 0; j < layout.getChildCount(); j++) {
                        View subChild = layout.getChildAt(j);
                        if (subChild instanceof ImageView) {
                            hasIcon = true;
                        }
                        if (subChild instanceof TextView) {
                            hasText = true;
                        }
                    }
                    if (hasIcon && hasText) {
                        return layout;
                    }
                }
                ViewGroup result = findItemContainer(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    private static View createIndicator(android.content.Context context) {
        View indicator = new View(context);
        indicator.setBackgroundResource(R.drawable.bg_bottom_nav_indicator);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            dpToPx(context, 32),
            dpToPx(context, 3)
        );
        params.setMargins(0, 0, 0, dpToPx(context, 4));
        indicator.setLayoutParams(params);
        return indicator;
    }
    
    private static int dpToPx(android.content.Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

