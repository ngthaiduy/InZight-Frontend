package com.example.inzightapp.eventInput;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.inzightapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PostMenuBottomSheet extends BottomSheetDialogFragment {

    public interface PostMenuListener {
        void onHidePost();
        void onDeletePost();
    }

    private final boolean isOwner;
    private final PostMenuListener listener;

    public PostMenuBottomSheet(boolean isOwner, PostMenuListener listener) {
        this.isOwner = isOwner;
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_post_menu, null);
        dialog.setContentView(view);

        LinearLayout itemHide = view.findViewById(R.id.itemHide);
        LinearLayout itemDelete = view.findViewById(R.id.itemDelete);
        LinearLayout itemCancel = view.findViewById(R.id.itemCancel);

        // Nếu là bài viết của mình => hiện Delete
        if (isOwner) itemDelete.setVisibility(View.VISIBLE);

        itemHide.setOnClickListener(v -> {
            listener.onHidePost();
            dismiss();
        });

        itemDelete.setOnClickListener(v -> {
            listener.onDeletePost();
            dismiss();
        });

        itemCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }
}
