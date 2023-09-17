package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListofClients extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClientListAdapter adapter;
    TextView textforlistcheckout,textforlistcheckin;
    private boolean isActivityActive = true;

    private ArrayList<HelperClassRegisterClient> clientList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listof_clients);
        Intent intent = this.getIntent();
        String mail = intent.getStringExtra("pethotelmail");
        String typeofcl = intent.getStringExtra("typeofclient");
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        textforlistcheckin=findViewById(R.id.textforlistcheckin);
        textforlistcheckout=findViewById(R.id.textforlistcheckout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        clientList = new ArrayList<>();
        adapter = new ClientListAdapter(this, clientList, new ClientListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                HelperClassRegisterClient client = clientList.get(position);
                String clientId = client.getClientKey();
                Boolean activeFlag = client.isActiveFlag();
                openEditClientActivity(clientId, String.valueOf(activeFlag));
            }
        });

        recyclerView.setAdapter(adapter);

        if (typeofcl.equals("checkedin")) {
            textforlistcheckout.setVisibility(View.VISIBLE);
            loadCheckInList(mail);
        } else if (typeofcl.equals("checkedout")) {
            textforlistcheckin.setVisibility(View.VISIBLE);
            loadCheckOutList(mail);
        }
    }

    private void openEditClientActivity(String clientId, String activeflag) {
        Intent intent = new Intent(this, EditClient.class);
        intent.putExtra("clientId", clientId);
        intent.putExtra("typeofclient", activeflag);
        startActivity(intent);
    }
    private void loadCheckInList(String mail) {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        Query query = FirebaseDatabase.getInstance().getReference("Client")
                .orderByChild("petHotelEmail")
                .equalTo(mail);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clientList.clear();
                if (isActivityActive && dataSnapshot.exists()) {
                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                        HelperClassRegisterClient client = clientSnapshot.getValue(HelperClassRegisterClient.class);
                        String clientKey = clientSnapshot.getKey();
                        client.setClientKey(clientKey); // Set the client key in the HelperClassRegisterClient object

                        if (client.isActiveFlag()) {
                            Query petRef = FirebaseDatabase.getInstance().getReference("Pet")
                                    .orderByChild("clientID")
                                    .equalTo(clientKey);

                            petRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<String> petNames = new ArrayList<>();
                                    List<String> petBreeds = new ArrayList<>();
                                    List<List<String>> vaccinesList = new ArrayList<>(); // Create a new List to store vaccine information

                                    for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                                        HelperClassPet pet = petSnapshot.getValue(HelperClassPet.class);
                                        if (pet.isActiveFlag()) {
                                            String petName = pet.getPetName();
                                            String petBreed = pet.getPetBreed();

                                            // Retrieve pet vaccines from the path petID/Vaccines
                                            DatabaseReference vaccinesReference = FirebaseDatabase.getInstance().getReference("Pet")
                                                    .child(pet.getPetID())
                                                    .child("Vaccines");

                                            vaccinesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    List<String> vaccines = new ArrayList<>();
                                                    for (DataSnapshot vaccineSnapshot : dataSnapshot.getChildren()) {
                                                        String vaccine = vaccineSnapshot.getValue(String.class);
                                                        vaccines.add(vaccine);
                                                    }

                                                    petNames.add(petName);
                                                    petBreeds.add(petBreed);
                                                    vaccinesList.add(vaccines); // Add the vaccines list to the outer list

                                                    if (petNames.size() == snapshot.getChildrenCount()) {
                                                        // All pets' information retrieved, update UI
                                                        client.setClientKey(client.getClientKey()); // Set the client key
                                                        client.setActiveFlag(client.isActiveFlag()); // Set the active flag
                                                        client.setPetNames(petNames);
                                                        client.setPetBreeds(petBreeds);
                                                        client.setPetVaccines(vaccinesList);
                                                        clientList.add(client); // Add the updated client to the list
                                                        adapter.notifyDataSetChanged();
                                                        recyclerView.setVisibility(View.VISIBLE);
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle the error if needed
                                                    progressBar.setVisibility(View.GONE);
                                                    Log.e("ValueEventListener", "Database Error: " + databaseError.getMessage());
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("ValueEventListener", "Database Error: " + error.getMessage());
                                }
                            });
                        }
                    }
                } else if (isActivityActive) {
                    //Toast.makeText(ListofClients.this, "No data found", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ListofClients.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // Hide progress bar in case of error
            }
        });
    }


    private void loadCheckOutList(String mail) {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        Query query = FirebaseDatabase.getInstance().getReference("Client")
                .orderByChild("petHotelEmail")
                .equalTo(mail);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clientList.clear();
                if (isActivityActive && dataSnapshot.exists()) {
                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                        HelperClassRegisterClient client = clientSnapshot.getValue(HelperClassRegisterClient.class);
                        String clientKey = clientSnapshot.getKey();
                        client.setClientKey(clientKey); // Set the client key in the HelperClassRegisterClient object

                        if (!client.isActiveFlag()) {
                            Query petRef = FirebaseDatabase.getInstance().getReference("Pet")
                                    .orderByChild("clientID")
                                    .equalTo(clientKey);

                            petRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<String> petNames = new ArrayList<>();
                                    List<String> petBreeds = new ArrayList<>();
                                    List<List<String>> vaccinesList = new ArrayList<>(); // Create a new List to store vaccine information

                                    for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                                        HelperClassPet pet = petSnapshot.getValue(HelperClassPet.class);
                                        if (!pet.isActiveFlag()) {
                                            String petName = pet.getPetName();
                                            String petBreed = pet.getPetBreed();

                                            // Retrieve pet vaccines from the path petID/Vaccines
                                            DatabaseReference vaccinesReference = FirebaseDatabase.getInstance().getReference("Pet")
                                                    .child(pet.getPetID())
                                                    .child("Vaccines");

                                            vaccinesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    List<String> vaccines = new ArrayList<>();
                                                    for (DataSnapshot vaccineSnapshot : dataSnapshot.getChildren()) {
                                                        String vaccine = vaccineSnapshot.getValue(String.class);
                                                        vaccines.add(vaccine);
                                                    }

                                                    petNames.add(petName);
                                                    petBreeds.add(petBreed);
                                                    vaccinesList.add(vaccines); // Add the vaccines list to the outer list

                                                    if (petNames.size() == snapshot.getChildrenCount()) {
                                                        // All pets' information retrieved, update UI
                                                        client.setClientKey(client.getClientKey()); // Set the client key
                                                        client.setActiveFlag(client.isActiveFlag()); // Set the active flag
                                                        client.setPetNames(petNames);
                                                        client.setPetBreeds(petBreeds);
                                                        client.setPetVaccines(vaccinesList);
                                                        clientList.add(client); // Add the updated client to the list
                                                        adapter.notifyDataSetChanged();
                                                        recyclerView.setVisibility(View.VISIBLE);
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle the error if needed
                                                    progressBar.setVisibility(View.GONE);
                                                    Log.e("ValueEventListener", "Database Error: " + databaseError.getMessage());
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("ValueEventListener", "Database Error: " + error.getMessage());
                                }
                            });
                        }
                    }
                } else if (isActivityActive) {
                    //Toast.makeText(ListofClients.this, "No data found", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ListofClients.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // Hide progress bar in case of error
            }
        });
    }
}
