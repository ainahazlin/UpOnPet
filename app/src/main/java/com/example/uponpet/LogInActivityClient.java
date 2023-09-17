package com.example.uponpet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogInActivityClient extends AppCompatActivity {

    EditText clientlogin, otpreceived;
    Button loginclient, buttonverifyotp;
    FirebaseAuth mAuth;
    String verificationID;
    ProgressBar bar;
    Intent intent;
    String phoneNumber;
    SharedPreferences sharedPreferences;
    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_client);
        clientlogin = findViewById(R.id.contactnumberclient_login);
        loginclient = findViewById(R.id.buttonclientlogin);
        otpreceived = findViewById(R.id.enterotp);
        buttonverifyotp = findViewById(R.id.buttonsubmitotp);
        bar = findViewById(R.id.progressauth);
        mAuth = FirebaseAuth.getInstance();

        if (!PermissionUtilClient.hasNotificationPermission(this)) {
            requestNotificationPermission();
        }

        loginclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeClientLogin(v);
            }
        });
        buttonverifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(otpreceived.getText().toString())) {
                    Toast.makeText(LogInActivityClient.this, "Please enter the code", Toast.LENGTH_SHORT).show();
                } else {
                    verifyotp(otpreceived.getText().toString());
                }
            }
        });
        sharedPreferences = getSharedPreferences("PetHotelClient", MODE_PRIVATE);

    }

    // Request notification permission
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PermissionUtilClient.requestNotificationPermission(this);
        } else {
            // Open notification settings for the user to grant permission
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    // Handle the result of the notification permission request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PermissionUtilClient.NOTIFICATION_SETTINGS_REQUEST_CODE) {
            if (PermissionUtilClient.hasNotificationPermission(this)) {
                return;
            } else {
                // Permission not granted, show an explanation or disable relevant functionality
                Toast.makeText(this, "Notification permission denied! You may set manually on app info", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static boolean isValidMalaysiaPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(MALAYSIA_PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public void makeClientLogin(View view) {
        phoneNumber = clientlogin.getText().toString().trim();
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        if (phoneNumber.isEmpty()) {
            Toast.makeText(LogInActivityClient.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        } else if (isValidMalaysiaPhoneNumber(formattedPhoneNumber)) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Client");
            Query checkUserDatabase = databaseRef.orderByChild("clientcontactNumber").equalTo(formattedPhoneNumber);
            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                            boolean activeFlag = clientSnapshot.child("activeFlag").getValue(Boolean.class);
                            if (activeFlag) {
                                String clientKey = clientSnapshot.getKey(); // Get the key of the dataSnapshot
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("clientKey", clientKey);
                                editor.apply();
                                Toast.makeText(LogInActivityClient.this, "Please wait for the verification code", Toast.LENGTH_SHORT).show();
                                bar.setVisibility(View.VISIBLE);
                                Toast.makeText(LogInActivityClient.this, "Please choose any browser to allow authentication", Toast.LENGTH_SHORT).show();
                                sendverificationcode(formattedPhoneNumber);
                            } else {
                                // The account is not active
                                Toast.makeText(LogInActivityClient.this, "Account is not active\nDo ask the Pet Hotel for further action", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // The phone number does not exist in the "Client" child of the database
                        // Display an error message or handle the login failure
                        clientlogin.setText("");
                        Toast.makeText(LogInActivityClient.this, "Phone number is not registered or inactive", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that occur during the database operation
                    Toast.makeText(LogInActivityClient.this, "Please try again, the operation is cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            clientlogin.setText("");
            Toast.makeText(LogInActivityClient.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
        }
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

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            final String code = credential.getSmsCode();
            if (code != null) {
                verifyotp(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LogInActivityClient.this, "Verification Failed. Please try again later\nThe firebase block the authentication as it is detect unusual activity on the phone number", Toast.LENGTH_LONG).show();
            bar.setVisibility(View.INVISIBLE);
            clientlogin.setText("");
            otpreceived.setText("");
        }

        @Override
        public void onCodeSent(@NonNull String s,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            verificationID = s;
            Toast.makeText(LogInActivityClient.this, "Code for verification should be received by SMS", Toast.LENGTH_SHORT).show();
            otpreceived.setVisibility(View.VISIBLE);
            buttonverifyotp.setVisibility(View.VISIBLE);
            bar.setVisibility(View.INVISIBLE);

        }
    };

    private void sendverificationcode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyotp(String Code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, Code);
        signinbyCredentials(credential);
    }

    private void signinbyCredentials(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            otpreceived.setText("");
                            clientlogin.setText("");

                            intent = new Intent(LogInActivityClient.this, ProfileClient.class);
                            startActivity(intent);
                            finish();
                            // Retrieve FCM token and save it to the database
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isSuccessful()) {
                                        String token = task.getResult();
                                        // Save the FCM token to the database for the pet owner
                                        // Associate the token with the pet owner's information
                                        // You can use the pet owner's phone number as the key
                                        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
                                        tokenRef.child(formatPhoneNumber(phoneNumber)).setValue(token);
                                    }

                                }
                            });
                        } else {
                            // Invalid code entered
                            Toast.makeText(LogInActivityClient.this, "Invalid code entered. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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