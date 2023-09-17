package com.example.uponpet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainScreen extends AppCompatActivity {

    private Button btnclient, btnstaff;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        btnclient = findViewById(R.id.buttonclient);
        btnstaff = findViewById(R.id.buttonpethotel);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String loggedInUser = sharedPreferences.getString("loggedInUser", "");

        if (!loggedInUser.isEmpty()) {
            if (loggedInUser.equals("PetHotelClient")) {
                if (isPetHotelClientDataEmpty()) {
                    // Display the current activity since data is empty
                    return;
                } else {
                    openClientActivity();
                    return;
                }
            } else if (loggedInUser.equals("PetHotel")) {
                if (isPetHotelDataEmpty()) {
                    // Display the current activity since data is empty
                    return;
                } else {
                    openStaffActivity();
                    return;
                }
            }
        } else {
            if (!isNetworkAvailable()) {
                showNoInternetDialog();
                return;
            }
        }

    }

    public void buttonforstaff(View view){
        saveLoggedInUser("PetHotel");
        Intent intent = new Intent(MainScreen.this, MainActivity.class);
        startActivity(intent);
    }

    public void buttonforclient(View view){
        saveLoggedInUser("PetHotelClient");
        Intent intent = new Intent(MainScreen.this, LogInActivityClient.class);
        startActivity(intent);
    }

    private void resetLoggedInUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("loggedInUser");
        editor.apply();
    }

    private boolean isPetHotelClientDataEmpty() {
        // Check if the necessary data for PetHotelClient is empty
        // For example, check if SharedPreferences or relevant fields are empty
        // Return true if data is empty, false otherwise

        SharedPreferences sharedPreferences = getSharedPreferences("PetHotelClient", MODE_PRIVATE);
        String clientKey = sharedPreferences.getString("clientKey", "");
        return clientKey.isEmpty();
    }

    private boolean isPetHotelDataEmpty() {
        // Check if the necessary data for PetHotel is empty
        // For example, check if SharedPreferences or relevant fields are empty
        // Return true if data is empty, false otherwise

        SharedPreferences sharedPreferences = getSharedPreferences("PetHotel", MODE_PRIVATE);
        String staffKey = sharedPreferences.getString("phemail", "");
        return staffKey.isEmpty();
    }


    private void saveLoggedInUser(String user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loggedInUser", user);
        editor.apply();
    }

    private void openClientActivity() {
        Intent intent = new Intent(MainScreen.this, ProfileClient.class);
        startActivity(intent);
        finish();
    }

    private void openStaffActivity() {
        Intent intent = new Intent(MainScreen.this, ProfilePetHotel.class);
        startActivity(intent);
        finish();
    }


    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your network settings.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the activity or handle the OK button click event
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Go back to the home screen of the phone
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}