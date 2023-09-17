package com.example.uponpet;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

public class PermissionUtilAdmin {// Request code for permissions
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    // Permissions required for files and camera
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    // Check if the app has the required permissions
    public static boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Request the required permissions
    public static void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }
    }

    // Handle the permission request result
    public static boolean handlePermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}