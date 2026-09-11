package com.example.inzightapp.adapter.Home;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class WrapContentLinearLayoutManager extends LinearLayoutManager {

    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler,
                          @NonNull RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        int height = 0;
        for (int i = 0; i < getItemCount(); i++) {
            try {
                View child = recycler.getViewForPosition(i);
                measureChild(child, widthSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                height += child.getMeasuredHeight();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
    }
}
