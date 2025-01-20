package com.example.undercover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DialerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the intent data matches the dial code
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && data.toString().equals("tel:*1234#")) {
            // Launch the hidden MainActivity
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
        }

        // Close the activity immediately after triggering
        finish();
    }
}
