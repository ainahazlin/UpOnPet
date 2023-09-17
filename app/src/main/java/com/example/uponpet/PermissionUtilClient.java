package com.example.uponpet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class PermissionUtilClient {

    public static final int NOTIFICATION_SETTINGS_REQUEST_CODE = 200;

    // Check if the app has notification permission
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners") != null;
        } else {
            String enabledNotificationListeners = Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners");
            return enabledNotificationListeners != null &&
                    enabledNotificationListeners.contains(context.getPackageName());
        }
    }

    // Request notification permission
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            activity.startActivityForResult(intent, NOTIFICATION_SETTINGS_REQUEST_CODE);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivityForResult(intent, NOTIFICATION_SETTINGS_REQUEST_CODE);
        }
    }
}
