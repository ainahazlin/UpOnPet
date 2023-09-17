package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainScreenStaff extends AppCompatActivity {

    private static final String MALAYSIA_PHONE_PATTERN = "^\\+?60\\d{9,10}$";

    FirebaseDatabase database;
    DatabaseReference reference;
    private ArrayAdapter<String> spinnerAdapter;
    private EditText editTextCheckIn;
    private EditText editTextCheckOut;
    private Calendar checkInCalendar;
    private Calendar checkOutCalendar;
    private DatePickerDialog.OnDateSetListener checkInDateSetListener;
    private DatePickerDialog.OnDateSetListener checkOutDateSetListener;
    private EditText cphonenum, cname, caddress, itemcheckedin, totalpet;
    private Spinner paymentstatus;
    Button submitClient;
    SharedPreferences sharedPreferences;
    String emailofph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_staff);

        sharedPreferences = getSharedPreferences("PetHotel", MODE_PRIVATE);
        emailofph = sharedPreferences.getString("phemail", "");
        // Check if data exists in SharedPreferences
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavistaff);
        bottomNavigationView.setSelectedItemId(R.id.homes);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homes) {
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
                    startActivity(new Intent(getApplicationContext(), ProfilePetHotel.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();


        cphonenum = findViewById(R.id.contactclient);
        cname = findViewById(R.id.nameclient);
        caddress = findViewById(R.id.adressclient);
        totalpet = findViewById(R.id.howmanypet);
        submitClient = findViewById(R.id.buttonregisterclient);
        itemcheckedin = findViewById(R.id.itemcheckedin);
        // Initialize the payment status spinner
        paymentstatus = findViewById(R.id.paymentstatus);
        String[] paymentOptions = {"Payment Status", "Deposit", "Unpaid", "Paid"};
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentstatus.setAdapter(spinnerAdapter);

        submitClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterClient(v);
            }
        });


    }


    public static boolean isValidMalaysiaPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(MALAYSIA_PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public void RegisterClient(View view) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Get the client details
        String contactnumberclient = cphonenum.getText().toString();
        String formatcnc = formatPhoneNumber(contactnumberclient);
        String nameclient = cname.getText().toString();
        String addressclient = caddress.getText().toString();
        String itembrought = itemcheckedin.getText().toString();
        String paymentStatus = paymentstatus.getSelectedItem().toString();
        String totalPetsToRegisterString = totalpet.getText().toString();


        if (TextUtils.isEmpty(cphonenum.getText().toString()) || TextUtils.isEmpty(cname.getText().toString()) || TextUtils.isEmpty(caddress.getText().toString())|| TextUtils.isEmpty(itemcheckedin.getText().toString())|| paymentStatus.equals("Payment Status")|| TextUtils.isEmpty(totalPetsToRegisterString)) {
            Toast.makeText(MainScreenStaff.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
        } else if (isValidMalaysiaPhoneNumber(formatcnc)) {
            // Generate a random key for the client
            int totalPetsToRegister = Integer.parseInt(totalPetsToRegisterString);
            String clientKey = reference.child("Client").push().getKey();

            // Save the client information using the generated key
            DatabaseReference clientReference = reference.child("Client").child(clientKey);
            HelperClassRegisterClient helperClassRegisterClient = new HelperClassRegisterClient(formatcnc, nameclient, addressclient, emailofph,itembrought, paymentStatus, totalPetsToRegister, clientKey, true);
            clientReference.setValue(helperClassRegisterClient);


            Toast.makeText(MainScreenStaff.this, "Client is successfully registered!", Toast.LENGTH_SHORT).show();

            repeattoaddpet(totalPetsToRegister, clientKey);

            // Clear the input fields
            cphonenum.getText().clear();
            cname.getText().clear();
            caddress.getText().clear();
            totalpet.getText().clear();
            paymentstatus.setSelection(0);
            itemcheckedin.getText().clear();
        } else {
            Toast.makeText(MainScreenStaff.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
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

    public void repeattoaddpet(int totalpettoadd, String clientkey) {
        Intent intent = new Intent(MainScreenStaff.this, AddPet.class);
        intent.putExtra("clientkey", clientkey); // Add data to the Intent using a key-value pair
        intent.putExtra("totalpettoregister", totalpettoadd);
        startActivity(intent);
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