package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyticPetHotelStaff extends AppCompatActivity {

    private DatabaseReference databaseReference;
    TextView nametitle;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic_pet_hotel_staff);
        SharedPreferences sharedPreferences = getSharedPreferences("PetHotel", Context.MODE_PRIVATE);
        String pethotelemail = sharedPreferences.getString("phemail", "");
        progress = findViewById(R.id.progress);
        String name = sharedPreferences.getString("phname", "");
        nametitle = findViewById(R.id.textPetHotelName);
        nametitle.setText(name);

        Pie pie = AnyChart.pie();
        AnyChartView anyChartView = (AnyChartView) findViewById(R.id.any_chart_view);
        anyChartView.setChart(pie);

        // Retrieve data from the real-time database
        databaseReference = FirebaseDatabase.getInstance().getReference("Client");
        databaseReference.orderByChild("petHotelEmail").equalTo(pethotelemail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int[] catCount = {0};
                final int[] dogCount = {0};

                if (dataSnapshot.exists()) {
                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                        String clientId = clientSnapshot.getKey();

                        // Retrieve pet data for the current client
                        DatabaseReference petReference = FirebaseDatabase.getInstance().getReference("Pet");
                        petReference.orderByChild("clientID").equalTo(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot petSnapshot) {
                                if (petSnapshot.exists()) {
                                    for (DataSnapshot pet : petSnapshot.getChildren()) {
                                        String petType = pet.child("petType").getValue(String.class);
                                        if (petType != null) {
                                            if (petType.equals("Cat")) {
                                                catCount[0]++;
                                            } else if (petType.equals("Dog")) {
                                                dogCount[0]++;
                                            }
                                        }
                                    }
                                }

                                // Update the pie chart with the counts
//                                List<DataEntry> data = new ArrayList<>();
//                                data.add(new ValueDataEntry("Cat", catCount[0]));
//                                data.add(new ValueDataEntry("Dog", dogCount[0]));
//
//                                pie.data(data);
//                                progress.setVisibility(View.GONE);
                                // Update the pie chart with the counts
                                List<DataEntry> data = new ArrayList<>();
                                data.add(new ValueDataEntry("Cat", catCount[0]));
                                data.add(new ValueDataEntry("Dog", dogCount[0]));

// Create a custom palette with the desired colors
                                String[] colors = new String[]{"#a9957c", "#e5d7bc"};
                                String[] palette = new String[data.size()];
                                Arrays.fill(palette, colors[0]);
                                for (int i = 0; i < data.size(); i++) {
                                    palette[i] = colors[i % colors.length];
                                }

// Set the custom palette for the pie chart
                                pie.palette(palette);

                                pie.data(data);
                                progress.setVisibility(View.GONE);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle the error case if the pet data retrieval is canceled
                                Toast.makeText(AnalyticPetHotelStaff.this, "Failed to retrieve pet data from the database", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // Handle the case when no data is found for the petHotelEmail
                    // For example, display a message or show a default chart
                    progress.setVisibility(View.GONE);
                    Toast.makeText(AnalyticPetHotelStaff.this, "No data found for the pet hotel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error case if the client data retrieval is canceled
                Toast.makeText(AnalyticPetHotelStaff.this, "Failed to retrieve client data from the database", Toast.LENGTH_SHORT).show();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavistaff);
        bottomNavigationView.setSelectedItemId(R.id.analytics);
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
}