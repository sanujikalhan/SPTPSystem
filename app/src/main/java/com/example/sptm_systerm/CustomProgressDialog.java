package com.example.sptm_systerm;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sptm_systerm.R;

public class CustomProgressDialog {
    private Dialog dialog;

    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCancelable(false); // Prevent dismissing on back press
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView progressGif = dialog.findViewById(R.id.progress_gif);
        TextView progressText = dialog.findViewById(R.id.progress_text);

        // Load GIF using Glide
        Glide.with(context).load(R.drawable.loading).into(progressGif);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        TextView progressText = dialog.findViewById(R.id.progress_text);
        progressText.setText(message);
    }
}
