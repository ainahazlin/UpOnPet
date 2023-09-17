package com.example.uponpet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ListView listView;
    private EditText searchEditText;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private List<String> originalDataList;
    private String selectedItem = "";
    private String mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        listView = findViewById(R.id.listView);
        searchEditText = findViewById(R.id.searchEditText);

        Intent intent = this.getIntent();
        mail = intent.getStringExtra("pethotelmail");

        // Initialize data lists
        dataList = new ArrayList<>();
        originalDataList = new ArrayList<>();

        // Set up adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this example
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter data based on search text
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used in this example
            }
        });

        // Set item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = dataList.get(position); // Get selected item from the adapter
                searchEditText.setText(selectedItem); // Set selected item in the EditText

                // Open another activity and pass the selected item
                Intent intent = new Intent(SearchActivity.this, UpdatePetStaff.class);
                intent.putExtra("selectedItem", selectedItem);
                startActivity(intent);
            }
        });


        // Retrieve data from the database and populate originalDataList
        retrieveDataFromDatabase(mail);
    }



    private void retrieveDataFromDatabase(String mail) {
        DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
        DatabaseReference petReference = FirebaseDatabase.getInstance().getReference("Pet");

        clientReference.orderByChild("petHotelEmail").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot clientSnapshot) {
                originalDataList.clear();

                // Iterate through each client
                for (DataSnapshot clientData : clientSnapshot.getChildren()) {
                    String clientId = clientData.getKey();
                    Boolean activeFlag = clientData.child("activeFlag").getValue(Boolean.class);
                    if (activeFlag) {
                        String clientPhoneNumber = clientData.child("clientcontactNumber").getValue(String.class);

                        // Fetch pets for the current client
                        petReference.orderByChild("clientID").equalTo(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot petSnapshot) {
                                // Iterate through each pet
                                for (DataSnapshot petData : petSnapshot.getChildren()) {
                                    String petName = petData.child("petName").getValue(String.class);

                                    if (petName != null) {
                                        originalDataList.add(petName + " - " + clientPhoneNumber);
                                    }
                                }

                                // Populate dataList with original data
                                dataList.clear();
                                dataList.addAll(originalDataList);

                                // Notify the adapter that the data set has changed
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }




    private void filterData(String searchText) {
        List<String> filteredData = new ArrayList<>();

        // Filter the original data list based on the search text
        for (String item : originalDataList) {
            if (item.toLowerCase().contains(searchText.toLowerCase())) {
                filteredData.add(item);
            }
        }

        // Update the adapter with the filtered data
        adapter.clear();
        if (searchText.isEmpty()) {
            adapter.addAll(originalDataList); // Add all items back when search input is empty
        } else {
            adapter.addAll(filteredData);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), UpdatePetStaff.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }
}
