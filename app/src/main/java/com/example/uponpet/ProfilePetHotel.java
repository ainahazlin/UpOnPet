package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ProfilePetHotel extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Button logoutButton, editprofile, listclient, infoButton;

    TextView greet,name, email, cntact, address;
    private ProgressBar loadingProgressBar;
    private String mail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pet_hotel);
        sharedPreferences = getSharedPreferences("PetHotel", MODE_PRIVATE);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        greet = findViewById(R.id.text);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        cntact = findViewById(R.id.contact);
        address = findViewById(R.id.address);
        greeting();
         mail = sharedPreferences.getString("phemail", "");


        showUserFromSharedPreferences();

        Button showBottomSheetButton = findViewById(R.id.listofclient);
        showBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavistaff);
        bottomNavigationView.setSelectedItemId(R.id.profiles);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homes) {
                    startActivity(new Intent(getApplicationContext(), MainScreenStaff.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.update) {
                    startActivity(new Intent(getApplicationContext(), UpdatePetStaff.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.analytics) {
                    startActivity(new Intent(getApplicationContext(), AnalyticPetHotelStaff.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.profiles) {
                    return true;
                }
                return false;
            }
        });
    }
    private void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        Button infoapps = view.findViewById(R.id.infoButton);
        Button editDataButton = view.findViewById(R.id.editDataButton);
        Button listOfClientButton = view.findViewById(R.id.listOfClientButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button listofCheckedOutButton = view.findViewById(R.id.checkedoutclient);

        // Set click listeners for the buttons inside the bottom sheet dialog
        editDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePetHotel.this, EditProfileStaff.class);
                startActivity(intent);
            }
        });

        infoapps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePetHotel.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        listofCheckedOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePetHotel.this, ListofClients.class);
                intent.putExtra("pethotelmail", mail);
                intent.putExtra("typeofclient", "checkedout");
                startActivity(intent);
            }
        });

        listOfClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePetHotel.this, ListofClients.class);
                intent.putExtra("pethotelmail", mail);
                intent.putExtra("typeofclient", "checkedin");
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
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


    private void greeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Determine the appropriate greeting based on the time
        String greeting;
        if (hour < 12) {
            greeting = "Good morning, ";
        } else if (hour < 18) {
            greeting = "Good afternoon, ";
        } else {
            greeting = "Good evening, ";
        }

        String message = greeting;
        greet.setText(message);
    }
    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), MainScreenStaff.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }

    private void showUserFromSharedPreferences() {
        loadingProgressBar.setVisibility(View.VISIBLE); // Show the progress bar

        String phemail = sharedPreferences.getString("phemail", "");
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference("Admin");
        Query query = reference.orderByChild("pethotelemail").equalTo(phemail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String phname = childSnapshot.child("pethotelname").getValue(String.class);
                        String phcontact = childSnapshot.child("pethotelcontact").getValue(String.class);
                        String phaddress = childSnapshot.child("pethoteladdress").getValue(String.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("phemail", phemail);
                        editor.putString("phname", phname);
                        editor.putString("phcontact", phcontact);
                        editor.putString("phaddress", phaddress);
                        editor.apply();
                        name.setText(phname);
                        email.setText(phemail);
                        cntact.setText(phcontact);
                        address.setText(phaddress);
                        loadingProgressBar.setVisibility(View.GONE); // Hide the progress bar

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void showConfirmationDialog() {
        LogoutDialogStaff dialog = new LogoutDialogStaff();
        dialog.setLogoutListener(new LogoutDialogStaff.LogoutListener() {
            @Override
            public void onLogoutConfirmed() {
                // Clear SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.remove("PetHotel");
                editor.apply();
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editorpref = sharedPreferences.edit();
                editorpref.remove("loggedInUser");
                editorpref.apply();

                // Perform logout operations here
                FirebaseAuth.getInstance().signOut();

                // Navigate to MainScreen.class
                Intent intent = new Intent(ProfilePetHotel.this, MainScreen.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.show(getSupportFragmentManager(), "LogoutDialog");
    }


}