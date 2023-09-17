package com.example.uponpet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PetList extends AppCompatActivity {

    private RecyclerView rvPetList;
    private PetAdapter adapter;
    private DatabaseReference databaseRef;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        // Initialize RecyclerView and adapter
        rvPetList = findViewById(R.id.recyclerview);
        progressbar = findViewById(R.id.progressBar);
        adapter = new PetAdapter();

        // Set layout manager for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPetList.setLayoutManager(layoutManager);

        // Set adapter for the RecyclerView
        rvPetList.setAdapter(adapter);

        // Initialize Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Retrieve pet details based on client ID (assuming clientID is a string variable)
        SharedPreferences sharedPreferences = getSharedPreferences("PetHotelClient",MODE_PRIVATE);
        String clientID = sharedPreferences.getString("clientKey", "");
        Toast.makeText(this, "Please wait for the data to load", Toast.LENGTH_SHORT).show();

        Query query = FirebaseDatabase.getInstance().getReference("Pet")
                .orderByChild("clientID")
                .equalTo(clientID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HelperClassPet> petList = new ArrayList<>();

                // Iterate through the snapshot to retrieve pet details
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClassPet pet = dataSnapshot.getValue(HelperClassPet.class);
                    petList.add(pet);
                }

                // Pass the pet details to the adapter and notify the changes
                adapter.setPetList(petList);
                adapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors while retrieving data from the database
                Toast.makeText(PetList.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressbar.setVisibility(View.GONE);

            }
        });
    }
}
