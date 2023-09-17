package com.example.uponpet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutDialogStaff extends DialogFragment {

    // Interface and listener for logout confirmation
    public interface LogoutListener {
        void onLogoutConfirmed();
    }

    private LogoutListener logoutListener;

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Call the logout listener when logout is confirmed
                        if (logoutListener != null) {
                            logoutListener.onLogoutConfirmed();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel the dialog and do nothing
                    }
                });
        return builder.create();
    }
}
