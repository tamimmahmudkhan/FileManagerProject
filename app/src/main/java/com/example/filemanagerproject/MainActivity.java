package com.example.filemanagerproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This class holds a simple RecyclerView that displays the contents of the root directory and provides access to all subdirectories as well as basic file operations.
 */
public class MainActivity extends AppCompatActivity implements FileListAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";
    static final String SUBFOLDER_LAUNCH_KEY = "subFolderKey-01";
    static final String ROOT_URI = "uri_root_persist";

    RecyclerView fileList;
    private final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 5;
    static final int REQUEST_COPY = 93;
    private FileListAdapter fileListAdapter;
    static DocumentFile file; //Root directory
    static DocumentFile copiedFile;
    static DocumentFile subFolder;
    private Uri rootUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //List of file items will be viewed here.
        fileList = findViewById(R.id.file_list);
        fileList.setLayoutManager(new GridLayoutManager(this, 2));

        //Check whether app has read/write permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE
            );
        } else {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!defaultSharedPreferences.contains(ROOT_URI)) {
                //open file if we have permission already
                openDirectory(Uri.fromFile(Environment.getExternalStorageDirectory()));
            }else {
                rootUri = Uri.parse(defaultSharedPreferences.getString(ROOT_URI, "NULL"));
                file = DocumentFile.fromTreeUri(this, rootUri);
                fileListAdapter = new FileListAdapter(this, file.listFiles());
            }
        }
        fileList.setAdapter(fileListAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            openDirectory(Uri.fromFile(Environment.getExternalStorageDirectory())); //open system file picker
        }
    }

    public void openDirectory(Uri uriToLoad) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Provide read and write access to files and sub-directories in the user-selected
        // directory.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        //Ask permission from android to access internal storage.
        startActivityForResult(intent, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {

                getContentResolver().takePersistableUriPermission(resultData.getData(), resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

                //DocumentFile representing internal storage root.
                rootUri = resultData.getData();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit()
                        .putString(ROOT_URI, rootUri.toString())
                        .apply();

                file = DocumentFile.fromTreeUri(this, rootUri);

                fileListAdapter = new FileListAdapter(this, file.listFiles());
                fileList.setAdapter(fileListAdapter);
                fileListAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == REQUEST_COPY) {
            fileListAdapter.setFileList(file.listFiles());
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }


    @Override
    public void onClick(int position) {
        DocumentFile selectedFile = file.listFiles()[position];
        if (selectedFile.isDirectory()) {
            Intent intent = new Intent(this, SubFolderActivity.class);
            intent.putExtra(SUBFOLDER_LAUNCH_KEY, position);
            subFolder = selectedFile;
            startActivity(intent);
        } else {
            Toast.makeText(this, selectedFile.getName() + " item was clicked!", Toast.LENGTH_SHORT).show();
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
                    copiedFile = file.listFiles()[position];
                    startActivityForResult(intent, REQUEST_COPY);
                    return true;
                case R.id.delete_menu_item:
                    if (selectedFile.canWrite() && selectedFile.delete()) {
                        file = DocumentFile.fromTreeUri(this, rootUri);
                        fileListAdapter.setFileList(file.listFiles());
                        fileList.removeViewAt(position - 1);
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show();
                    }
                    return true;case R.id.rename_menu_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Rename To:");
                    TextView view = new TextView(this);
                    builder.setView(view);
                    builder.setPositiveButton("Confirm", (dialog, which) -> selectedFile.renameTo(view.getEditableText().toString()));
                    builder.show();
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
        }else if (selectedFile.getType().contains("video")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedFile.getUri(), "video/*");
            startActivity(intent);
        }
    }
}
