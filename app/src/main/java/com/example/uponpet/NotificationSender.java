package com.example.uponpet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class NotificationSender {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAla1OlrI:APA91bFKeiDEiHqydJ66V_Rg9NMdALfMIY6eaGQKyHC7w9KX0FffnYKy4XTZFByQ4v9HllXWW20-xDVlDgujcdZV1KW_V2M1Yqf77hJqNbbkdFvfM9x7JATdPkzTzeyDS7QftQ2zDspI"; // Replace with your FCM server key

    public void sendNotification(String to, String title, String body, HelperUpdatePet pet) {
        OkHttpClient client = new OkHttpClient();
        String jsonPayload = createJsonPayload(to, title, body, pet);
        RequestBody requestBody = RequestBody.create(jsonPayload, JSON);
        Request request = createRequest(requestBody);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle response
            }
        });
    }

    private String createJsonPayload(String to, String title, String body, HelperUpdatePet pet) {
        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("to", to);

            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body + "\n" +
                    "Pet: " + pet.getPetName());
            notification.put("click_action", "OPEN_ACTIVITY"); // Add click action
            notification.put("icon", "drawable/icon.png"); // Set your custom icon here

            // Set notification properties
            JSONObject androidNotification = new JSONObject();
            androidNotification.put("priority", "high");
            androidNotification.put("sound", "default");
            androidNotification.put("vibrate", true);
            androidNotification.put("visibility", 1); // Set visibility to 1 for floating notification
            androidNotification.put("wake_screen", true); // Set wake_screen to true for waking the lock screen
            notification.put("android", androidNotification);

            jsonPayload.put("notification", notification);

            JSONObject data = new JSONObject();
            data.put("petId", pet.getId());
            data.put("phoneNumber", pet.getClientPhoneNumber());
            data.put("petName", pet.getPetName());
            data.put("description", pet.getDescription());
            data.put("dateTime", pet.getSelectedDate());
            data.put("imageUri", pet.getImageUrl());
            data.put("mediatype", pet.getMediaType());
            jsonPayload.put("data", data);

            return jsonPayload.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    private Request createRequest (RequestBody body){
            return new Request.Builder()
                    .url(FCM_API)
                    .post(body)
                    .addHeader("Authorization", "key=" + SERVER_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
        }
    }
