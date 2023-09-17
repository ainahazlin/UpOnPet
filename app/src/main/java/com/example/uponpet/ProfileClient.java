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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileClient extends AppCompatActivity {


    //    DatabaseReference databaseRef;
    SharedPreferences sharedPreferences;
    TextView namec, cntactc, addressc, greetingtextview;
    Button logoutButton, gotolistmypet;
    private ProgressBar loadingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_client);

        Intent intent = getIntent();
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        namec = findViewById(R.id.clientname);
        cntactc = findViewById(R.id.clientcontactnum);
        addressc = findViewById(R.id.clientaddress);
        gotolistmypet = findViewById(R.id.listofpet);
        logoutButton = findViewById(R.id.logout);
        greetingtextview = findViewById(R.id.greeting);
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

        greeting();
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog();
            }
        });
        gotolistmypet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileClient.this, PetList.class);
                startActivity(intent);
            }
        });
        sharedPreferences = getSharedPreferences("PetHotelClient", MODE_PRIVATE);

        if (sharedPreferences.contains("clientKey")) {
            showUserFromSharedPreferences();

        } else {
            displayUserDataFromSharedPreferences();

        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnaviclient);
        bottomNavigationView.setSelectedItemId(R.id.profilec);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homec) {
                    startActivity(new Intent(getApplicationContext(), MainScreenClient.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.detailspethotel) {
                    startActivity(new Intent(getApplicationContext(), DetailsPetHotelforClient.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.profilec) {

                    return true;
                }
                return false;
            }
        });
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
        greetingtextview.setText(message);
    }


    private void displayUserDataFromSharedPreferences() {
        // Retrieve the data from SharedPreferences
        String cname = sharedPreferences.getString("clientname", "");
        String ccontactnumber = sharedPreferences.getString("clientcontactNumber", "");
        String caddress = sharedPreferences.getString("clientaddress", "");
        String totalPetsString = sharedPreferences.getString("totalPets", "");

        // Update the TextViews with the retrieved data
        namec.setText(cname);
        cntactc.setText(ccontactnumber);
        addressc.setText(caddress);
    }

    private void showUserFromSharedPreferences() {
        loadingProgressBar.setVisibility(View.VISIBLE); // Show the progress bar

        String clientKey = sharedPreferences.getString("clientKey", "");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Client");
        Query query = reference.orderByChild("clientKey").equalTo(clientKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String ccontactnumber = childSnapshot.child("clientcontactNumber").getValue(String.class);
                        String cname = childSnapshot.child("clientname").getValue(String.class);
                        String pethotelemail = childSnapshot.child("petHotelEmail").getValue(String.class);
                        String caddress = childSnapshot.child("clientaddress").getValue(String.class);
                        Integer totalpet = childSnapshot.child("totalPets").getValue(Integer.class);
                        String totalPetsString = String.valueOf(totalpet);

                        namec.setText(cname);
                        cntactc.setText(ccontactnumber);
                        addressc.setText(caddress);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("clientname", cname);
                        editor.putString("clientcontactNumber", ccontactnumber);
                        editor.putString("clientaddress", caddress);
                        editor.putString("totalPets", String.valueOf(totalpet));
                        editor.putString("pethotelregistered", pethotelemail);
                        editor.putString("clientKey", clientKey);
                        editor.apply();
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


    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), MainScreenClient.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }

    private void showConfirmationDialog() {
        LogoutDialogStaff dialog = new LogoutDialogStaff();
        dialog.setLogoutListener(new LogoutDialogStaff.LogoutListener() {
            @Override
            public void onLogoutConfirmed() {
                // Clear SharedPreferences
                removeTokenFromDatabase(cntactc.getText().toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.remove("PetHotelClient");
                editor.apply();
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editorpref = sharedPreferences.edit();

                editorpref.remove("loggedInUser");
                editorpref.apply();
                // Perform logout operations here
                FirebaseAuth.getInstance().signOut();

                // Navigate to MainScreen.class
                Intent intent = new Intent(ProfileClient.this, MainScreen.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.show(getSupportFragmentManager(), "LogoutDialog");
    }

    private void removeTokenFromDatabase(String phonenumber) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("Tokens");

        // Find the token node to be removed
        Query tokenQuery = tokensRef.orderByChild(phonenumber);

        tokenQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                    tokenSnapshot.getRef().removeValue(); // Remove the token from the database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }

}