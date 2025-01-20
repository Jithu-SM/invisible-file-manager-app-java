package com.example.undercover;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<File> files;
    private final Context context;
    private final FileAdapterListener listener;

    public interface FileAdapterListener {
        void onFileDeleted(File file);
        void onFileRenamed(File file);
        void onFileEdited(File file);
        void onFileRestored(File file);
    }

    public FileAdapter(Context context, File directory, FileAdapterListener listener) {
        this.files = new ArrayList<>();
        this.context = context;
        this.listener = listener;
        loadFiles(directory);  // Load initial files
    }


    // Load files from the directory into the list
    public void loadFiles(File directory) {
        files.clear();  // Clear current list
        File[] fileArray = directory.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                files.add(file);
            }
        }
        notifyDataSetChanged();  // Notify RecyclerView of data changes
    }

    public void refreshFiles(File directory) {
        loadFiles(directory);  // Reload files from the directory
    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        holder.fileName.setText(file.getName());

        // Open file on click
        holder.itemView.setOnClickListener(view -> openFile(file));

        // Show options menu on dots button click
        holder.fileOptionsMenu.setOnClickListener(view -> showPopupMenu(view, file));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    // Method to open a file using Intent
    private void openFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(context, "com.example.undercover.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getMimeType(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Open with"));
    }

    // Show the options menu (Edit, Delete, Rename, Restore)
    private void showPopupMenu(View view, File file) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.file_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.edit_file) {
                listener.onFileEdited(file);
                return true;
            } else if (id == R.id.delete_file) {
                listener.onFileDeleted(file);
                return true;
            } else if (id == R.id.rename_file) {
                listener.onFileRenamed(file);
                return true;
            } else if (id == R.id.restore_file) {
                listener.onFileRestored(file);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    // Get MIME type based on file extension
    private String getMimeType(File file) {
        String mimeType = "*/*";
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (extension.equals("txt")) {
            mimeType = "text/plain";
        } else if (extension.equals("jpg") || extension.equals("jpeg")) {
            mimeType = "image/jpeg";
        } else if (extension.equals("png")) {
            mimeType = "image/png";
        } else if (extension.equals("pdf")) {
            mimeType = "application/pdf";
        }
        return mimeType;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView fileOptionsMenu;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileOptionsMenu = itemView.findViewById(R.id.fileOptionsMenu);
        }
    }
}
