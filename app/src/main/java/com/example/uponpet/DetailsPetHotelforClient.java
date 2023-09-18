package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class DetailsPetHotelforClient extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    TextView nmpethotel, empethotel, cnpethotel, addpethotel;
    ProgressBar progressBar;
    private Geocoder geocoder;
    private List<Address> addresses;
    private LatLng petHotelLocation;
    String pethoteladd, pethotelname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_pet_hotelfor_client);
        geocoder = new Geocoder(this);

        nmpethotel = findViewById(R.id.namepethotel);
        empethotel = findViewById(R.id.emailpethotel);
        cnpethotel = findViewById(R.id.contactpethotel);
        addpethotel = findViewById(R.id.addresspethotel);
        progressBar = findViewById(R.id.progressbarclient);
        progressBar.setVisibility(View.VISIBLE);
        cnpethotel.setOnClickListener(v -> {
            String phoneNumber = cnpethotel.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                // Create an intent to initiate a phone call
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnaviclient);
        bottomNavigationView.setSelectedItemId(R.id.detailspethotel);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homec) {
                startActivity(new Intent(getApplicationContext(), MainScreenClient.class));
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.detailspethotel) {
                return true;
            } else if (itemId == R.id.profilec) {
                startActivity(new Intent(getApplicationContext(), ProfileClient.class));
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        retrievepethoteldata();
    }
//
//    public void retrievepethoteldata() {
//        SharedPreferences sharedPreferences = getSharedPreferences("PetHotelClient", Context.MODE_PRIVATE);
//        String cphonenum = sharedPreferences.getString("clientcontactNumber", "");
//
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Client");
//        Query checkUserDatabase = databaseRef.orderByChild("clientcontactNumber").equalTo(cphonenum);
//        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
//                        String pethotelmail = clientSnapshot.child("petHotelEmail").getValue(String.class);
//
//                        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Admin");
//                        Query checkpethotel = dbref.orderByChild("pethotelemail").equalTo(pethotelmail);
//                        checkpethotel.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists()) {
//                                    for (DataSnapshot pethotelSnapshot : snapshot.getChildren()) {
//                                        pethotelname = pethotelSnapshot.child("pethotelname").getValue(String.class);
//                                        String pethotelcn = pethotelSnapshot.child("pethotelcontact").getValue(String.class);
//                                        pethoteladd = pethotelSnapshot.child("pethoteladdress").getValue(String.class);
//
//                                        nmpethotel.setText(pethotelname);
//                                        empethotel.setText(pethotelmail);
//                                        cnpethotel.setText(pethotelcn);
//                                        addpethotel.setText(pethoteladd);
//                                        // Call onMapReady after retrieving the address
//                                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//                                        if (pethoteladd != null && !pethoteladd.isEmpty()) {
//                                            mapFragment.getMapAsync(DetailsPetHotelforClient.this);
//                                        }
//                                    }
//                                } else {
//                                    Toast.makeText(DetailsPetHotelforClient.this, "Pet hotel not found", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Toast.makeText(DetailsPetHotelforClient.this, "Failed to retrieve pet hotel data. Please make sure you have internet connection", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                } else {
//                    Toast.makeText(DetailsPetHotelforClient.this, "Client contact number not found", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(DetailsPetHotelforClient.this, "Error accessing database.Please try again later", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void retrievepethoteldata() {
        SharedPreferences sharedPreferences = getSharedPreferences("PetHotelClient", Context.MODE_PRIVATE);
        String clientPhoneNumber = sharedPreferences.getString("clientcontactNumber", "");

        DatabaseReference clientDatabaseRef = FirebaseDatabase.getInstance().getReference("Client");
        Query queryClient = clientDatabaseRef.orderByChild("clientcontactNumber").equalTo(clientPhoneNumber);
        queryClient.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                        String petHotelEmail = clientSnapshot.child("petHotelEmail").getValue(String.class);
                        boolean activeFlag = clientSnapshot.child("activeFlag").getValue(Boolean.class);

                        if (activeFlag) {
                            // Only proceed if the client has an active booking
                            retrievePetHotelDetails(petHotelEmail);
                        } else {
                        }
                    }
                } else {
                    Toast.makeText(DetailsPetHotelforClient.this, "Client contact number not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailsPetHotelforClient.this, "Error accessing database. Please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrievePetHotelDetails(String petHotelEmail) {
        DatabaseReference petHotelDatabaseRef = FirebaseDatabase.getInstance().getReference("Admin");
        Query queryPetHotel = petHotelDatabaseRef.orderByChild("pethotelemail").equalTo(petHotelEmail);
        queryPetHotel.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot petHotelSnapshot : snapshot.getChildren()) {
                        pethotelname = petHotelSnapshot.child("pethotelname").getValue(String.class);
                        String pethotelcn = petHotelSnapshot.child("pethotelcontact").getValue(String.class);
                        pethoteladd = petHotelSnapshot.child("pethoteladdress").getValue(String.class);

                        nmpethotel.setText(pethotelname);
                        empethotel.setText(petHotelEmail);
                        cnpethotel.setText(pethotelcn);
                        addpethotel.setText(pethoteladd);

                        // Call onMapReady after retrieving the address
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        if (pethoteladd != null && !pethoteladd.isEmpty()) {
                            mapFragment.getMapAsync(DetailsPetHotelforClient.this);
                        }
                    }
                } else {
                    Toast.makeText(DetailsPetHotelforClient.this, "Pet hotel not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsPetHotelforClient.this, "Failed to retrieve pet hotel data. Please make sure you have an internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), MainScreenClient.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (pethoteladd != null && !pethoteladd.isEmpty()) {  // Check if the address is not empty
            try {
                addresses = geocoder.getFromLocationName(pethoteladd, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address petHotelAddress = addresses.get(0);
                    double latitude = petHotelAddress.getLatitude();
                    double longitude = petHotelAddress.getLongitude();
                    petHotelLocation = new LatLng(latitude, longitude);

                    // Create a custom marker icon with a smaller size
                    Bitmap originalMarkerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.googlemarker);
                    float markerIconSize = getResources().getDimension(R.dimen.marker_icon_size);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalMarkerIcon, (int) markerIconSize, (int) markerIconSize, false);
                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(petHotelLocation)
                            .title(pethotelname)
                            .icon(markerIcon); // Set the resized marker icon

                    myMap.addMarker(markerOptions);
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petHotelLocation, 15f));
                    progressBar.setVisibility(View.GONE);
                    myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            String query = Uri.encode(pethotelname);
                            String uri = "geo:0,0?q=" + query;
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);

                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Unable to geocode the address.Please try again later", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error in geocoding.Please make sure you have internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Address is empty", Toast.LENGTH_SHORT).show();
        }
    }


}
