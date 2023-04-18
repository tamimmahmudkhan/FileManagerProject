package com.example.filemanagerproject;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

class SimpleFileView extends RecyclerView.ViewHolder {

    TextView fileName;
    ConstraintLayout linearLayout;
    ImageButton optionsButton;

    SimpleFileView(@NonNull View itemView) {
        super(itemView);

        fileName = itemView.findViewById(R.id.file_name);
        linearLayout = itemView.findViewById(R.id.item_layout);
        optionsButton = itemView.findViewById(R.id.imageButton);
    }
}