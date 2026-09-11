package com.example.inzightapp.adapter.Home;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

public class FullHeightRecyclerView extends RecyclerView {

    public FullHeightRecyclerView(Context context) {
        super(context);
    }

    public FullHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullHeightRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Khi đang ở chế độ xem trước layout (Preview mode trong Android Studio)
        // thì bỏ qua việc tính toán này để tránh lỗi preview (Use View.isInEditMode()).
        if (isInEditMode()) {
            // Preview trong Android Studio: giữ nguyên hành vi mặc định
            super.onMeasure(widthSpec, heightSpec);
            return;
        }

        // Khi chạy thật: đo toàn bộ chiều cao
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, expandSpec);
    }
}
