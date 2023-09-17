package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.uponpet.databinding.ActivityMainScreenClientBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainScreenClient extends AppCompatActivity {

    private FloatingActionButton fab;
    ProgressBar progress;
    private RecyclerView recyclerView;
    private ArrayList<HelperUpdatePet> dataList;
    private MyAdapter adapter;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PetUpdate");
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainScreenClientBinding binding = ActivityMainScreenClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progress = findViewById(R.id.progress);

        BottomNavigationView bottomNavigationView = binding.bottomnaviclient;
        bottomNavigationView.setSelectedItemId(R.id.homec);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homec) {
                return true;
            } else if (itemId == R.id.detailspethotel) {
                startActivity(new Intent(getApplicationContext(), DetailsPetHotelforClient.class));
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.profilec) {
                startActivity(new Intent(getApplicationContext(), ProfileClient.class));
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("PetHotelClient", Context.MODE_PRIVATE);
        String cphonenum = sharedPreferences.getString("clientcontactNumber", "");
        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        adapter = new MyAdapter(this, dataList);
        recyclerView.setAdapter(adapter);
        databaseReference.orderByChild("clientPhoneNumber").equalTo(cphonenum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear(); // Clear the list before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperUpdatePet dataClass = dataSnapshot.getValue(HelperUpdatePet.class);
                    if (dataClass != null) {
                        String imageUrl = dataClass.getImageUrl(); // Assuming you have a field for storing the image URL in HelperUpdatePet class
                        StorageReference imageRef = storage.getReference().child(imageUrl);
                       dataList.add(dataClass);
                    }
                }
                progress.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

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
