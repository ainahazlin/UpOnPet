package com.example.uponpet;

import android.content.Intent;
import android.os.Bundle;

import com.example.uponpet.ImageDetailsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            // Handle the notification here
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Get the click action value from the data payload
            String clickAction = remoteMessage.getData().get("click_action");

            // Start the desired activity based on the click action
            if ("OPEN_ACTIVITY".equals(clickAction)) {
                Intent intent = new Intent(this, ImageDetailsActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("body", body);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Pass additional data if needed
                Bundle extras = new Bundle();
                extras.putString("petId", remoteMessage.getData().get("petId"));
                extras.putString("phoneNumber", remoteMessage.getData().get("phoneNumber"));
                extras.putString("petName", remoteMessage.getData().get("petName"));
                extras.putString("description", remoteMessage.getData().get("description"));
                extras.putString("dateTime", remoteMessage.getData().get("dateTime"));
                extras.putString("imageUri", remoteMessage.getData().get("imageUri"));
                intent.putExtras(extras);

                startActivity(intent);
            }
        }
    }
}
