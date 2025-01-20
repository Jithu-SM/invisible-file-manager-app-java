package com.example.undercover;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements FileAdapter.FileAdapterListener {

    private RecyclerView fileList;
    private FileAdapter fileAdapter;
    private File directory;



    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for permissions
        checkPermissions();

        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(view -> {
            Log.d("MainActivity", "Upload button clicked");
            openFilePicker();
        });

        fileList = findViewById(R.id.fileList);
        directory = getFilesDir();  // Internal storage

        setupFileList();  // Display files
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                handleSelectedFile(fileUri);  // Pass the file URI for further handling
            }
        }
    }



    private void setupFileList() {
        fileList.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this, directory, this);
        fileList.setAdapter(fileAdapter);
    }

    @Override
    public void onFileDeleted(File file) {
        if (file.exists() && file.delete()) {
            fileAdapter.refreshFiles(directory);
        }
    }

    @Override
    public void onFileRenamed(File file) {
        showRenameDialog(file);
    }

    @Override
    public void onFileEdited(File file) {
        Intent intent = new Intent(this, EditFileActivity.class);
        intent.putExtra("filePath", file.getAbsolutePath());
        startActivity(intent);
    }

    @Override
    public void onFileRestored(File file) {
        restoreFileToDevice(file);
    }

    // Restore file to device storage (Downloads folder)
    private void restoreFileToDevice(File file) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File restoredFile = new File(downloadsDir, file.getName());

        try (FileOutputStream fos = new FileOutputStream(restoredFile)) {
            fos.write(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Show a dialog to rename the file
    private void showRenameDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        final EditText input = new EditText(this);
        input.setText(file.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString();
            File newFile = new File(file.getParent(), newName);
            if (file.renameTo(newFile)) {
                fileAdapter.refreshFiles(directory);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private static final int PICK_FILE_REQUEST = 1;

    // Open file picker to upload new files
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");  // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }
    private void handleSelectedFile(Uri fileUri) {
        // Copy the selected file to your app's internal storage
        File destFile = new File(getFilesDir(), getFileName(fileUri));
        try (InputStream in = getContentResolver().openInputStream(fileUri);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            // Refresh file list to show the new file
            fileAdapter.refreshFiles(getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get the file name from URI
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }




    // Save the selected file to internal storage
    private void saveFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            String fileName = getFileName(uri);

            // Create new file in internal storage
            File file = new File(directory, fileName);
            try (InputStream inputStream = contentResolver.openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Refresh the RecyclerView with the new file list
                fileAdapter.refreshFiles(directory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Show file options (Edit/Delete)
    private void showFileOptions(File file, View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.file_options_menu, popupMenu.getMenu());


        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.edit_file) {
                editFile(file);
                return true;
            } else if (id == R.id.delete_file) {
                deleteFile(file);
                return true;
            } else {
                return false;
            }
        });

    }

    // Edit the file (navigate to EditFileActivity)
    private void editFile(File file) {
        Intent intent = new Intent(this, EditFileActivity.class);
        intent.putExtra("filePath", file.getAbsolutePath());
        startActivity(intent);
    }

    // Delete the file and refresh the list
    private void deleteFile(File file) {
        if (file.exists() && file.delete()) {
            fileAdapter.refreshFiles(directory);  // Refresh file list
        }
    }
}
