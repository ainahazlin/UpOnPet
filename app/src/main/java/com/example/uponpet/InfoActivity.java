package com.example.uponpet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InfoAdapter adapter;
    private List<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create the list of items
        itemList = new ArrayList<>();
        itemList.add("Analytic of Pet Hotel show the type of pet based on past and current record of pet checked-in");
        itemList.add("Did you know, your client get notification whenever there is update of their pet with image, pet's name and description");
        itemList.add("You can check-out your client by clicking their details and click check-out icon\nThis action will not delete the client data from your pet hotel data");
        itemList.add("You can edit client's pet whether to add or remove or edit their check-in or check-out date by clicking on client's data in Client List");
        itemList.add("You can use the name of pet hotel the same as name in Google Maps\nClient can see your details directly by Google Maps");
        itemList.add("If you removed pet it will be removed permanently under your pet hotel\nYou can just check-out client whenever their pet checked-out");

        // Create the adapter and set it to the RecyclerView
        adapter = new InfoAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ProfilePetHotel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }
}
