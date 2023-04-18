package com.example.filemanagerproject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Select destination folder for copy.
 */
public class DestinationSelector extends AppCompatActivity implements FileListAdapter.OnItemClickListener{

    RecyclerView recyclerView;
    SelectionAdapter listAdapter;

    Button pasteButton;
    Button cancelButton;

    DocumentFile currentDir;
    DocumentFile prevDir;

    DocumentFile copyFrom;
    DocumentFile copyTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_selector);

        recyclerView = findViewById(R.id.destination_select_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorBackground));

        pasteButton = findViewById(R.id.button_paste);
        pasteButton.setOnClickListener(this::onPasteClicked);

        cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> this.finish());

        currentDir = MainActivity.file;
        copyFrom = MainActivity.copiedFile;

        listAdapter = new SelectionAdapter(this, currentDir);

        recyclerView.setAdapter(listAdapter);
    }

    private void onPasteClicked(View view) {
        DocumentFile selectDirectory = listAdapter.getListFiles();

        copyTo = selectDirectory.createFile(copyFrom.getType(), copyFrom.getName());

        copyFile(copyFrom, copyTo);
        finish();
    }

    private void copyFile(DocumentFile copyFrom, DocumentFile copyTo) {
        try {
            OutputStream destinationFile = getContentResolver().openOutputStream(copyTo.getUri());
            InputStream sourceFile = getContentResolver().openInputStream(copyFrom.getUri());

            int count = sourceFile.read();
            while (count != -1) {
                destinationFile.write(count);
                count = sourceFile.read();
            }

            sourceFile.close();
            destinationFile.close();

            finishActivity(MainActivity.REQUEST_COPY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyFileOrDirectory(DocumentFile srcDir, DocumentFile dstDir) {
        try {
            if (srcDir.isDirectory()) {
                dstDir = dstDir.createDirectory(srcDir.getName());
                DocumentFile files[] = srcDir.listFiles();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    DocumentFile src1 = files[i];
                    copyFileOrDirectory(src1, dstDir);
                }
            } else {
                copyFile(srcDir, dstDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Copy Failed", Toast.LENGTH_SHORT).show();
        }
    }

//    public void copyFile(File sourceFile, File destFile) throws IOException {
//        if (!destFile.getParentFile().exists())
//            destFile.getParentFile().mkdirs();
//
//        if (!destFile.exists()) {
//            destFile.createNewFile();
//        }
//
//        FileChannel source = null;
//        FileChannel destination = null;
//
//        try {
//            source = new FileInputStream(sourceFile).getChannel();
//            destination = new FileOutputStream(destFile).getChannel();
//            destination.transferFrom(source, 0, source.size());
//        } finally {
//            if (source != null) {
//                source.close();
//            }
//            if (destination != null) {
//                destination.close();
//            }
//        }
//    }


    @Override
    public void onClick(int position) {
        DocumentFile selection = currentDir.listFiles()[position];
        prevDir = currentDir;
        currentDir = selection;
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newUri(getContentResolver(), "copy", selection.getUri());
        manager.setPrimaryClip(clipData);
    }

    @Override
    public void onButtonClick(ImageButton button, DocumentFile file, int position) {

    }
}
