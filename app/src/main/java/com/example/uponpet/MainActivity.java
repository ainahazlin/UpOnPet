package com.example.uponpet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
import com.example.uponpet.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";
    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    EditText phname, phaddress, phcontact, phemail, phpass;
    Button submit, tologin, toclient;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        phname = findViewById(R.id.namepethotel);
        phaddress = findViewById(R.id.addresspethotel);
        phcontact = findViewById(R.id.contactpethotel);
        phemail = findViewById(R.id.pethotelemail);
        phpass = findViewById(R.id.passwordpethotel);
        phpass.addTextChangedListener(passwordTextWatcher);
        submit = findViewById(R.id.buttonSignUpStaff);



        //to go to login page for pet hotel
        tologin = findViewById(R.id.buttontoPageLogInStaff);
        tologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogInActivityStaff.class);
                startActivity(intent);
            }
        });

    }



    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable p) {
            String pwd = p.toString();
            if (pwd.isEmpty()) {
                phpass.setError(null);
            } else {
                boolean isValid = validatePassword(pwd);
                if (isValid) {
                    phpass.setError(null);
                    submit.setEnabled(true);
                } else {
                    phpass.setError("Please use a password with a mix of uppercase, lowercase, numbers, and special characters, with a minimum of 8 characters");
                }
            }
        }
    };
    private boolean validatePassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\[\\]{};':\"\\\\|,.<>/?-]).{8,}$";
        return password.matches(pattern);
    }

    public void register(View view) {
        String name = phname.getText().toString();
        String address = phaddress.getText().toString();
        String contactnumber = phcontact.getText().toString();
        String email = phemail.getText().toString();
        String password = phpass.getText().toString();
        String formatpn=formatPhoneNumber(contactnumber);
        if (name.isEmpty() || address.isEmpty() || formatpn.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
        } else if (isValidMalaysiaPhoneNumber(formatpn) && isValidEmail(email)) {
            //To sign up new pet hotel
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Admin");
            HelperClassStaffSignUp helperClassStaffSignUp = new HelperClassStaffSignUp(email, name, address, formatpn, password, true);

            // Query to check if email exists
            reference.orderByChild("pethotelemail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        phemail.setError("Email already exists");
                        phemail.requestFocus();
                    } else {
                        // Email does not exist in the database
                        // Generate a random key for the new data entry
                        String newKey = reference.push().getKey();

                        // Set the new data using the generated key
                        reference.child(newKey).setValue(helperClassStaffSignUp);

                        Toast.makeText(MainActivity.this, "You are successfully registered!", Toast.LENGTH_SHORT).show();
                        clearFields();

                        Intent intent = new Intent(MainActivity.this, LogInActivityStaff.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // An error occurred while retrieving the data
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        phname.getText().clear();
        phaddress.getText().clear();
        phcontact.getText().clear();
        phemail.getText().clear();
        phpass.getText().clear();
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
    public static boolean isValidEmail(String emailcheck) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(emailcheck);
        return matcher.matches();
    }

    public static boolean isValidMalaysiaPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(MALAYSIA_PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }

}