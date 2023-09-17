package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordPage extends AppCompatActivity {

    EditText staffphonenumtoverify, otpreceived;
    ProgressBar bar;
    FirebaseAuth mAuth;

    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    String verificationID, phoneNumber;

    Button buttontoverify, submitotp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_page);
        mAuth = FirebaseAuth.getInstance();

        staffphonenumtoverify = findViewById(R.id.phonenum);
        buttontoverify = findViewById(R.id.verifyacc);
        bar = findViewById(R.id.progressbar);
        submitotp = findViewById(R.id.submitotp);
        otpreceived = findViewById(R.id.otpreceived);

        submitotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(otpreceived.getText().toString()) && otpreceived.length() == 6) {
                    verifyotp(otpreceived.getText().toString());
                } else {
                    Toast.makeText(ForgotPasswordPage.this, "Please enter the 6 digit code received by sms", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttontoverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = formatPhoneNumber(staffphonenumtoverify.getText().toString().trim());
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(ForgotPasswordPage.this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                } else if (!isValidMalaysiaPhoneNumber(phoneNumber)) {
                    Toast.makeText(ForgotPasswordPage.this, "Please enter a valid Malaysia phone number", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");
                    Query query = adminRef.orderByChild("pethotelcontact").equalTo(phoneNumber);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Iterate over the snapshot to find the matching phone number and activeFlag
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    boolean activeFlag = snapshot.child("activeFlag").getValue(Boolean.class);
                                    if (activeFlag) {
                                        // Phone number exists and activeFlag is true
                                        bar.setVisibility(View.VISIBLE);
                                        sendVerificationCode(phoneNumber);
                                        return; // Exit the loop if activeFlag is true
                                    }
                                }
                                // Phone number exists but activeFlag is false or not found
                                Toast.makeText(ForgotPasswordPage.this, "Phone number is inactive", Toast.LENGTH_SHORT).show();
                            } else {
                                // Phone number not found in the database
                                Toast.makeText(ForgotPasswordPage.this, "Phone number not registered. Please check your phone number.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors
                        }
                    });

                }
            }
        });


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

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    final String code = credential.getSmsCode();
                    if (code != null) {
                        verifyotp(code);
                    }
                }


                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(ForgotPasswordPage.this, "Verification Failed. Please try again later", Toast.LENGTH_SHORT).show();
                    bar.setVisibility(View.INVISIBLE);
                    staffphonenumtoverify.setText("");
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationID = s;
                    Toast.makeText(ForgotPasswordPage.this, "Code should be receive by SMS", Toast.LENGTH_SHORT).show();
                    bar.setVisibility(View.GONE);
                    otpreceived.setVisibility(View.VISIBLE);
                    submitotp.setVisibility(View.VISIBLE);
                    buttontoverify.setVisibility(View.GONE);
                    staffphonenumtoverify.setVisibility(View.GONE);

                }
            };

    private void sendVerificationCode(String phoneNumber) {
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

                            Intent intent;
                            intent = new Intent(ForgotPasswordPage.this, ResetPasswordPage.class);
                            intent.putExtra("phonestaff", phoneNumber);
                            startActivity(intent);
                            finish();

                        } else {
                            // Invalid code entered
                            Toast.makeText(ForgotPasswordPage.this, "Invalid code entered. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
