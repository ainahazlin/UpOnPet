package com.example.uponpet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start the MyFirebaseMessagingService when the device finishes booting
            Intent serviceIntent = new Intent(context, MyFirebaseMessagingService.class);
            context.startService(serviceIntent);
        }
    }
}
