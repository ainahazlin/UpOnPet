package com.example.uponpet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileStaff extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";
    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    EditText upname, upemail, upcontact, upaddress, uppass;
    Button enterupdate;
    HelperClassStaffSignUp admin;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_staff);
        upemail = findViewById(R.id.updatepethotelemail);
        upname = findViewById(R.id.updatenamepethotel);
        upcontact = findViewById(R.id.updatecontactpethotel);
        upaddress = findViewById(R.id.updateaddresspethotel);

        SharedPreferences sharedPreferences = getSharedPreferences("PetHotel", MODE_PRIVATE);
        String oldemail = sharedPreferences.getString("phemail", "");

        // Get a reference to the "Admin" node in the database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Admin");

        loaddatapethotel(oldemail);

        enterupdate = findViewById(R.id.buttoneditprofile);
        enterupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = upname.getText().toString();
                String newAddress = upaddress.getText().toString();
                String newContact = upcontact.getText().toString();

                String newEmail = upemail.getText().toString();
                String formatcn = formatPhoneNumber(newContact);
                if (isValidMalaysiaPhoneNumber(formatcn) && isValidEmail(newEmail)) {
                    // Check if the oldemail exists in the "Admin" path
                    if (newName.equals(admin.getPethotelname()) && newAddress.equals(admin.getPethoteladdress())
                            && formatcn.equals(admin.getPethotelcontact()) && newEmail.equals(admin.getPethotelemail())) {
                        // No changes were made
                        Toast.makeText(EditProfileStaff.this, "No changes were made.", Toast.LENGTH_SHORT).show();
                    } else {
                        reference.orderByChild("pethotelemail").equalTo(oldemail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                                        // Get the key of the child node (admin)
                                        String adminKey = adminSnapshot.getKey();

                                        // Update the existing child node with the old email as the key
                                        reference = database.getReference("Admin").child(adminKey);
                                        reference.child("pethotelname").setValue(newName);
                                        reference.child("pethoteladdress").setValue(newAddress);
                                        reference.child("pethotelcontact").setValue(formatcn);
                                        reference.child("pethotelemail").setValue(newEmail);
                                        reference.child("pethotelpass").setValue(admin.getPethotelpass());

                                        // Update the shared preferences with the new data
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("phname", newName);
                                        editor.putString("phaddress", newAddress);
                                        editor.putString("phcontact", formatcn);
                                        editor.putString("phemail", newEmail);
                                        editor.putString("phpass", admin.getPethotelpass());
                                        editor.apply();

                                        // Update the Client node with the new email
                                        DatabaseReference clientReference = database.getReference("Client");
                                        clientReference.orderByChild("petHotelEmail").equalTo(oldemail).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot clientSnapshot) {
                                                if (clientSnapshot.exists()) {
                                                    for (DataSnapshot clientDataSnapshot : clientSnapshot.getChildren()) {
                                                        String clientKey = clientDataSnapshot.getKey();
                                                        clientReference.child(clientKey).child("petHotelEmail").setValue(newEmail);
                                                        loaddatapethotel(newEmail); // Pass the new email here


                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(EditProfileStaff.this, "Failed to update client pethotelmail", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        Toast.makeText(EditProfileStaff.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(EditProfileStaff.this, ProfilePetHotel.class);
//                                        startActivity(intent);
                                    }
                                } else {
                                    //Toast.makeText(EditProfileStaff.this, "Invalid User", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(EditProfileStaff.this, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }


    public void loaddatapethotel(String email) {
        // Query the database to get the data based on phemail
        reference.orderByChild("pethotelemail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                        // Get the admin data
                        admin = adminSnapshot.getValue(HelperClassStaffSignUp.class);

                        // Populate the EditText fields with the retrieved values
                        upname.setText(admin.getPethotelname());
                        upemail.setText(admin.getPethotelemail());
                        upcontact.setText(admin.getPethotelcontact());
                        upaddress.setText(admin.getPethoteladdress());

                        // Enable the enterupdate Button
                        enterupdate.setEnabled(true);
                    }
                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileStaff.this, "Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static boolean isValidEmail(String emailcheck) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(emailcheck);
        return matcher.matches();
    }

    public static String formatPhoneNumber(String phoneNumber) {
        // Remove any whitespace or dashes
        phoneNumber = phoneNumber.replaceAll("\\s", "").replaceAll("-", "");

        // Check if the phone number starts with "0" or "60"
        if (phoneNumber.startsWith("0")) {
            // Replace the "0" with "+60"
            phoneNumber = "+60" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("60")) {
            // Replace "60" with "+60"
            phoneNumber = "+60" + phoneNumber.substring(2);
        }

        return phoneNumber;
    }

    public static boolean isValidMalaysiaPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(MALAYSIA_PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ProfilePetHotel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }
}
