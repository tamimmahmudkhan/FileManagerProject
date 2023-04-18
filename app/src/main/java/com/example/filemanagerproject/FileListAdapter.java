package com.example.filemanagerproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter to display files.
 */
public class FileListAdapter extends RecyclerView.Adapter<SimpleFileView> {

    private DocumentFile[] fileList; // Files to be displayed.
    private OnItemClickListener itemClickNotifier; //Notifies parent activity when item is clicked
    private static final String TAG = "FileListAdapter";

    FileListAdapter(OnItemClickListener listener, DocumentFile[] files) {
        itemClickNotifier = listener;
        fileList = files;
    }

    interface OnItemClickListener {
        void onClick(int position);

        void onButtonClick(ImageButton button, DocumentFile file, int position);
    }

    public void setFileList(DocumentFile[] fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    void updateFileList(DocumentFile[] files) {
        fileList = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SimpleFileView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");
        // Inflating individual list item.
        SimpleFileView simpleFileView = new SimpleFileView(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_file_item, null));
//        simpleFileView.setTextColor(android.R.color.holo_blue_bright, parent.getContext());
        return simpleFileView;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleFileView holder, int position) {
        Log.d(TAG, "onBindViewHolder: "+ fileList[position].getName());
        holder.fileName.setText(fileList[position].getName());
        holder.fileName.setVisibility(View.VISIBLE);
        if (fileList[position].isDirectory()) {
            holder.fileName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_folder_black_18dp,0, 0, 0);
        } else {
            holder.fileName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_insert_drive_file_black_18dp,0, 0, 0);
        }
        holder.linearLayout.setOnClickListener(v -> itemClickNotifier.onClick(position));
        holder.optionsButton.setOnClickListener(v -> itemClickNotifier.onButtonClick(holder.optionsButton, fileList[position], position));
    }

    @Override
    public int getItemCount() {
        return fileList.length - 1;
    }

    void onDeleteNotifier() {
        notifyDataSetChanged();
    }
}
