package com.example.filemanagerproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Manages the display and basic file operations for all sub-folders in the
 */
public class SubFolderActivity extends AppCompatActivity implements FileListAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    FileListAdapter listAdapter;
    private DocumentFile subFolder;
    private DocumentFile prevDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = new RecyclerView(this);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        subFolder = MainActivity.subFolder;

        listAdapter = new FileListAdapter(this, subFolder.listFiles());

        recyclerView.setAdapter(listAdapter);

        setContentView(recyclerView);
    }

    @Override
    public void onClick(int position) {
        DocumentFile selectedFile = subFolder.listFiles()[position];
        if (selectedFile.isDirectory()) {
            prevDir = subFolder;
            subFolder = subFolder.listFiles()[position];
            listAdapter.setFileList(subFolder.listFiles());
        } else {
            openSelection(selectedFile);
        }
    }

    @Override
    public void onButtonClick(ImageButton button, DocumentFile selectedFile, int position) {
        PopupMenu popupMenu = new PopupMenu(this, button);
        popupMenu.inflate(R.menu.menu_context);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.copy_menu_item:
                    Intent intent = new Intent(this, DestinationSelector.class);
                    MainActivity.copiedFile = subFolder.listFiles()[position];
                    startActivity(intent);
                    return true;
                case R.id.rename_menu_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Rename To:");
                    TextView view = new TextView(this);
                    builder.setView(view);
                    builder.setPositiveButton("Confirm", (dialog, which) -> selectedFile.renameTo(view.getEditableText().toString()));
                    builder.show();
                    return true;
                case R.id.delete_menu_item:
                    if (selectedFile.canWrite() && selectedFile.delete()) {
                        listAdapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void openSelection(DocumentFile selectedFile) {
        if (selectedFile.getType().contains("image")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedFile.getUri(), "image/*");
            startActivity(intent);
        } else if (selectedFile.getType().contains("audio")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedFile.getUri(), "audio/*");
            startActivity(intent);
        } else if (selectedFile.getType().contains("video")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedFile.getUri(), "video/*");
            startActivity(intent);
        }
    }
}
