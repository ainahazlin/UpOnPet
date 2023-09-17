package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetPasswordPage extends AppCompatActivity {

    EditText otp, newpass;
    ProgressBar bar;
    FirebaseAuth mAuth;

    Button buttonsubmitotp, buttonnewpass;

    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    String usercode, staffphonenumtoverify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_page);
        mAuth = FirebaseAuth.getInstance();

        newpass = findViewById(R.id.newpassword);
        newpass.addTextChangedListener(passwordTextWatcher);
        buttonnewpass = findViewById(R.id.resetpass);
        bar = findViewById(R.id.progressbar);

        Intent intent = getIntent();
        staffphonenumtoverify = intent.getStringExtra("phonestaff");
        buttonnewpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(newpass.getText().toString())) {
                    Toast.makeText(ResetPasswordPage.this, "Set New Password Accordingly", Toast.LENGTH_SHORT).show();
                } else {
                    setNewPassword();

                }
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
                newpass.setError(null);
            } else {
                boolean isValid = validatePassword(pwd);
                if (isValid) {
                    newpass.setError(null);
                    buttonnewpass.setEnabled(true);
                } else {
                    newpass.setError("Please use a password with a mix of uppercase, lowercase, numbers, and special characters, with a minimum of 8 characters");
                }
            }
        }
    };

    private boolean validatePassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\[\\]{};':\"\\\\|,.<>/?-]).{8,}$";
        return password.matches(pattern);
    }

    private void setNewPassword() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebaseRef = database.getReference();
        String newPass = newpass.getText().toString().trim();

        firebaseRef.child("Admin")
                .orderByChild("pethotelcontact")
                .equalTo(staffphonenumtoverify)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                            String adminKey = adminSnapshot.getKey(); // Get the key of the admin

                            DatabaseReference adminRef = firebaseRef.child("Admin").child(adminKey);
                            adminRef.child("pethotelpass").setValue(newPass)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            otp.setText("");
                                            newpass.setText("");
                                            Toast.makeText(ResetPasswordPage.this, "Password updated successfully\nYou can log in using new password", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ResetPasswordPage.this, LogInActivityStaff.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ResetPasswordPage.this, "Please try again, there is a problem occurred during reset password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle onCancelled event if needed
                    }
                });

    }

}
