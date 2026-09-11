package com.example.inzightapp.adapter.Category;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.example.inzightapp.R;
import com.example.inzightapp.model.response.CategoryResponse;

import java.util.List;

public class CategoryGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<CategoryResponse> categories;
    private int selectedPosition = -1;

    public CategoryGridAdapter(Context context, List<CategoryResponse> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public CategoryResponse getSelectedCategory() {
        if (selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    @Override public int getCount() { return categories.size(); }
    @Override public Object getItem(int position) { return categories.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);

        ImageView icon = convertView.findViewById(R.id.ivIcon);
        TextView name = convertView.findViewById(R.id.tvName);
        CategoryResponse cat = categories.get(position);

        name.setText(cat.getName());

        // Use hardcoded icons based on category name
        icon.setImageResource(
                com.example.inzightapp.utils.CategoryUtils.getIconForCategory(cat.getName())
        );

        if (position == selectedPosition)
            convertView.setBackgroundResource(R.drawable.bg_category_selected);
        else
            convertView.setBackgroundResource(R.drawable.bg_category_unselected);

        return convertView;
    }
}
