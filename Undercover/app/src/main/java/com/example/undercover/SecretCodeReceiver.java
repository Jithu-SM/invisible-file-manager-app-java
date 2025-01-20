package com.example.undercover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class SecretCodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String host = uri.getHost();

            // Retrieve the stored secret code
            SharedPreferences prefs = context.getSharedPreferences("UndercoverPrefs", Context.MODE_PRIVATE);
            String storedCode = prefs.getString("dial_code", "1234");

            if (host.equals(storedCode)) {
                // Launch the app if the dialed code matches the stored secret code
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } else {
                Log.d("SecretCodeReceiver", "Invalid secret code");
            }
        }
    }
}
