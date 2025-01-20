package com.example.undercover;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditFileActivity extends AppCompatActivity {

    private EditText fileContent;
    private Button saveButton;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        fileContent = findViewById(R.id.fileContent);
        saveButton = findViewById(R.id.saveButton);

        String filePath = getIntent().getStringExtra("filePath");
        file = new File(filePath);

        loadFileContent();

        saveButton.setOnClickListener(view -> saveFileContent());
    }

    // Load the file content into the EditText
    private void loadFileContent() {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fileContent.setText(new String(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save the edited content back to the file
    private void saveFileContent() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContent.getText().toString().getBytes());
            finish(); // Close the activity after saving
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
