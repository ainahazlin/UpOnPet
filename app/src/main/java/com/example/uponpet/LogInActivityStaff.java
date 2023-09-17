package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogInActivityStaff extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";

    EditText adminemail, adminpass;
    Button submitlogin, gotoSignUpPageStaff, fpassword;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_staff);
        adminemail = findViewById(R.id.pethotelemail_login);
        adminpass = findViewById(R.id.passwordpethotel_login);
        submitlogin = findViewById(R.id.buttonLogInStaff);
        gotoSignUpPageStaff = findViewById(R.id.bttntoSignUpPage);

        if (!PermissionUtilAdmin.hasPermissions(this)) {
            PermissionUtilAdmin.requestPermissions(this);
        }
        fpassword = findViewById(R.id.buttonforgotpassword);
        fpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivityStaff.this, ForgotPasswordPage.class);
                startActivity(intent);
            }
        });
        sharedPreferences = getSharedPreferences("PetHotel", MODE_PRIVATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtilAdmin.handlePermissionsResult(requestCode, grantResults)) {
            return;
        } else {
            Toast.makeText(this, "Permission for access file and camera denied! You may set manually on app info", Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoSignUpPage(View view) {
        Intent intent = new Intent(LogInActivityStaff.this, MainActivity.class);
        startActivity(intent);

    }

    public void login(View view) {
        String email = adminemail.getText().toString().trim();
        String password = adminpass.getText().toString().trim();

        if (email.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Please fill in email and password", Toast.LENGTH_SHORT).show();
        } else if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Admin");
            Query checkUserDatabase = reference.orderByChild("pethotelemail").equalTo(email);
            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String passwordFromDB = childSnapshot.child("pethotelpass").getValue(String.class);
                            boolean isActive = childSnapshot.child("activeFlag").getValue(Boolean.class);
                            String contactn = childSnapshot.child("pethotelcontact").getValue(String.class);
                            if (passwordFromDB.equals(password)) {
                                if (isActive) {
                                    adminemail.setError(null);
                                    adminpass.setError(null);
                                    adminemail.getText().clear();
                                    adminpass.getText().clear();
                                    Toast.makeText(LogInActivityStaff.this, "Welcome!!", Toast.LENGTH_SHORT).show();

                                    // Store email in SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("phemail", email);
                                    editor.apply();

                                    // Pass data using intent
                                    String namefromdb = childSnapshot.child("pethotelname").getValue(String.class);
                                    String addressfromdb = childSnapshot.child("pethoteladdress").getValue(String.class);

                                    Intent intent = new Intent(LogInActivityStaff.this, ProfilePetHotel.class);
                                    intent.putExtra("phname", namefromdb);
                                    intent.putExtra("phemail", email);
                                    intent.putExtra("phcontact", contactn);
                                    intent.putExtra("phpassword", password);
                                    intent.putExtra("phaddress", addressfromdb);
                                    startActivity(intent);
                                } else {
                                    adminemail.setError("User is inactive");
                                    adminemail.requestFocus();
                                }
                                return; // Exit the method if a matching email is found
                            }
                        }
                        // Invalid password
                        adminemail.setError(null);
                        adminpass.getText().clear();
                        adminpass.setError("Invalid Credentials");
                        adminpass.requestFocus();
                    } else {
                        // User does not exist
                        adminemail.setError("User does not exist");
                        adminemail.requestFocus();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }


}