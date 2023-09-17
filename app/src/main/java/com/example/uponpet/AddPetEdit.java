package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddPetEdit extends AppCompatActivity {
    private EditText name, checkin;
    private String checkInDate, checkOutDate;
    int days;
    Button registerpet;
    private MaterialDatePicker<Pair<Long, Long>> mMaterialDatePicker;

    private ImageView imagepet;
    private Spinner spinnerVaccine;

    private Spinner spinnerpetbreed;
    private String selectedBreed;

    FirebaseDatabase database;
    DatabaseReference reference;

    private ProgressBar progress;
    long durationInDays;

    private Spinner spinnerpettype;
    String selectedItem;
    private List<String> selectedVaccines = new ArrayList<>();


    private ArrayAdapter<String> vaccineAdapter;
    private TableLayout vaccineTable;
    private ArrayAdapter<String> spinnerBreedAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri capturedImageUri;

    private ArrayAdapter<String> spinnerAdapter;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet_edit);
        Intent intent = getIntent();
        clientId = intent.getStringExtra("clientId");

        database = FirebaseDatabase.getInstance();
        FirebaseApp.initializeApp(this);

        reference = database.getReference();

        name = findViewById(R.id.petnameclient);
        checkin = findViewById(R.id.editTextCheckIn);
        spinnerpettype = findViewById(R.id.spinnerpettype);
        spinnerpetbreed = findViewById(R.id.spinnerpetbreed);
        imagepet = findViewById(R.id.imagepet);
        progress = findViewById(R.id.progressstore);

        registerpet = findViewById(R.id.buttonaddpet);

        registerpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storepet();
            }
        });

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerpettype.setAdapter(spinnerAdapter);
        spinnerAdapter.add("Pet Type"); // Add the title as the first item
        spinnerAdapter.add("Dog");
        spinnerAdapter.add("Cat");

        spinnerpettype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = spinnerAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });
        ArrayAdapter<String> spinnerBreedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerBreedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerpetbreed.setAdapter(spinnerBreedAdapter);

// Add breed options for both Dog and Cat initially
        String[] dogBreeds = {"Select Breed", "Labrador Retriever", "German Shepherd", "Golden Retriever", "French Bulldog", "Beagle", "Poodle", "Bulldog", "Pomeranian"};
        String[] catBreeds = {"Select Breed", "Persian Cat", "Maine Coon", "Siamese Cat", "Ragdoll", "Bengal Cat", "Sphynx Cat", "British Shorthair", "Scottish Fold"};

// Initially, set the breed options to "Dog" breeds
        spinnerBreedAdapter.addAll(dogBreeds);

// Add an OnItemSelectedListener to the first spinner
        spinnerpettype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected pet type
                String selectedPetType = spinnerpettype.getSelectedItem().toString();

                // Clear the current breed options
                spinnerBreedAdapter.clear();

                // Add breed options based on the selected pet type
                if (selectedPetType.equals("Dog")) {
                    spinnerBreedAdapter.addAll(dogBreeds);
                } else if (selectedPetType.equals("Cat")) {
                    spinnerBreedAdapter.addAll(catBreeds);
                }

                // Notify the adapter that the data has changed
                spinnerBreedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        spinnerpetbreed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBreed = spinnerBreedAdapter.getItem(position);
                // You can store the selected breed in a variable or use it as needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Dates"); // You can set other properties here if needed
        mMaterialDatePicker = builder.build(); // Build the MaterialDatePicker instance


        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaterialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });

        mMaterialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                checkInDate = sdf.format(new Date(selection.first));
                checkOutDate = sdf.format(new Date(selection.second));

                checkin.setText(checkInDate + " - " + checkOutDate);

                long durationInMillis = selection.second - selection.first;
                days = (int) ((durationInMillis / (24 * 60 * 60 * 1000)) + 1);
            }
        });

        imagepet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        spinnerVaccine = findViewById(R.id.vaccineSpinner);
        vaccineTable = findViewById(R.id.vaccineTable);

        vaccineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVaccine.setAdapter(vaccineAdapter);

        // Add the vaccine names to the adapter
        vaccineAdapter.add("Select Vaccine");
        vaccineAdapter.add("FVRCP Vaccine");
        vaccineAdapter.add("Feline Leukemia Vaccine");
        vaccineAdapter.add("Rabies Vaccine");
        vaccineAdapter.add("DHPP Vaccine (or DHLPP)");
        vaccineAdapter.add("Bordetella Vaccine");
        vaccineAdapter.add("Canine Influenza Vaccine");
        vaccineAdapter.add("No Vaccine");


        Button addVaccineButton = findViewById(R.id.addVaccineButton);
        addVaccineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected vaccine from the spinner
                String selectedVaccine = spinnerVaccine.getSelectedItem().toString();

                // Check if the selected vaccine is not "Select Vaccine"
                if (!selectedVaccine.equals("Select Vaccine")) {
                    if (selectedVaccines.isEmpty()) {
                        // If the list is empty, add the selected vaccine
                        registerpet.setEnabled(true);
                        selectedVaccines.add(selectedVaccine);
                        updateVaccinesTable();
                    } else if (selectedVaccines.size() == 1 && selectedVaccines.contains("No Vaccine")) {
                        // If the list contains only "No Vaccine," prevent adding any other vaccine
                        Toast.makeText(AddPetEdit.this, "You cannot add another vaccine when 'No Vaccine' is selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccine.equals("No Vaccine")) {
                        // If there are other vaccines in the list, prevent adding "No Vaccine"
                        Toast.makeText(AddPetEdit.this, "You cannot add 'No Vaccine' when other vaccines are selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccines.contains(selectedVaccine)) {
                        // If the selected vaccine is already in the list, show a message
                        Toast.makeText(AddPetEdit.this, "Vaccine already selected.", Toast.LENGTH_SHORT).show();
                    } else {
                        registerpet.setEnabled(true);
                        // Add the selected vaccine to the list
                        selectedVaccines.add(selectedVaccine);
                        updateVaccinesTable();
                    }
                } else {
                    Toast.makeText(AddPetEdit.this, "Please select a valid vaccine.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void updateVaccinesTable() {
        // Clear the existing rows from the table
        vaccineTable.removeAllViews();

        // Add the selected vaccines to the table
        for (final String vaccine : selectedVaccines) {
            TableRow row = new TableRow(this);

            // TextView for displaying vaccine name
            TextView vaccineTextView = new TextView(this);
            vaccineTextView.setText(vaccine);
            row.addView(vaccineTextView);

            // Button for removing the vaccine
            Button removeButton = new Button(this);
            removeButton.setText("Remove");
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove the selected vaccine from the list
                    selectedVaccines.remove(vaccine);
                    // Update the table
                    updateVaccinesTable();
                }
            });
            row.addView(removeButton);

            vaccineTable.addView(row);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }

            if (photoFile != null) {
                capturedImageUri = FileProvider.getUriForFile(this,
                        "com.example.uponpet.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imagepet.setImageURI(capturedImageUri);
        }
    }



    private void storepet() {
        String petID = reference.child("Pet").push().getKey();
        String petName = name.getText().toString().trim();
        String petBreed = selectedBreed;
        String petType = selectedItem;


        if (petName.isEmpty() || petBreed.isEmpty() || checkInDate == null || checkInDate.isEmpty() || checkOutDate == null || checkOutDate.isEmpty() || capturedImageUri == null || selectedVaccines.isEmpty() || selectedVaccines.contains("Select Vaccine")) {
            Toast.makeText(AddPetEdit.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
            return; // Don't proceed if any required field is empty or invalid
        }

        progress.setVisibility(View.VISIBLE);

        String imageFileName = petID + ".jpg";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("pet_images").child(imageFileName);
        storageReference.putFile(capturedImageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Save the pet under the client's child node
                DatabaseReference petReference = reference.child("Pet").child(petID);
                HelperClassPet pet = new HelperClassPet(petID, clientId, petName, petType, petBreed, checkInDate, checkOutDate, days, imageUrl, true);
                petReference.setValue(pet);

                DatabaseReference vaccinesReference = petReference.child("Vaccines");
                vaccinesReference.setValue(selectedVaccines)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Pet data is successfully stored
                                Toast.makeText(AddPetEdit.this, "Pet data is successfully stored!", Toast.LENGTH_SHORT).show();
                                resetForm(); // Reset the form for the next pet registration
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddPetEdit.this, "Failed to store vaccines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            DatabaseReference clientReference = reference.child("Client").child(clientId);
            clientReference.child("totalPets").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Increment the totalPets count
                    if (snapshot.exists()) {
                        long totalPets = snapshot.getValue(Long.class);
                        totalPets++;
                        clientReference.child("totalPets").setValue(totalPets);
                    } else {
                        // If the node doesn't exist, create it with a value of 1
                        clientReference.child("totalPets").setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle any potential errors
                    Toast.makeText(AddPetEdit.this, "Failed to update totalPets count", Toast.LENGTH_SHORT).show();
                }
            });

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPetEdit.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        progress.setVisibility(View.GONE);
        spinnerpettype.setSelection(0);
        spinnerpetbreed.setSelection(0);
        name.getText().clear();
        checkin.getText().clear();
        spinnerVaccine.setSelection(0);
        imagepet.setImageResource(R.drawable.baseline_add_24);
        selectedVaccines.clear();
        vaccineTable.removeAllViews();

    }


    @Override
    public void onBackPressed() {
        // Open the desired activity
        Intent intent = new Intent(AddPetEdit.this, EditPet.class);
        intent.putExtra("clientId", clientId);
        startActivity(intent);
    }

}