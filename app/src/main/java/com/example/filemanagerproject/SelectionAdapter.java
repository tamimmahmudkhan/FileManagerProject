package com.example.filemanagerproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Display files for selection
 */
public class SelectionAdapter extends RecyclerView.Adapter<SimpleFileView> {

    private static final String TAG = "SelectionAdapter";
    DocumentFile listFiles;
    DocumentFile prevDir;
    FileListAdapter.OnItemClickListener itemClickNotifier;

    SelectionAdapter(FileListAdapter.OnItemClickListener listener, DocumentFile list) {
        listFiles = list;
        itemClickNotifier = listener;
    }

    public DocumentFile getListFiles() {
        return listFiles;
    }

    @NonNull
    @Override
    public SimpleFileView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleFileView(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_file_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleFileView holder, int position) {
        DocumentFile thisFile = listFiles.listFiles()[position];
        Log.d(TAG, "onBindViewHolder: "+ thisFile.getName());
        holder.fileName.setText(thisFile.getName());
        holder.fileName.setCompoundDrawables(holder.linearLayout.getResources().getDrawable(R.drawable.baseline_folder_black_18dp), null, null, null);
        holder.linearLayout.setOnClickListener(v -> {
            prevDir = listFiles;
            listFiles = prevDir.listFiles()[position];
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < listFiles.listFiles().length; i++) {
            if (listFiles.listFiles()[i].isDirectory()) {
                count++;
            }
        }
        return count;
    }
}
