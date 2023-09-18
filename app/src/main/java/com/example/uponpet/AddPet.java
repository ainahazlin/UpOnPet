package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.util.Pair; // Import the AndroidX Pair class

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.database.Query;
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

public class AddPet extends AppCompatActivity {


    private EditText name, checkin;
    private ProgressBar progress;
    private OnDeleteClientDataCallback onDeleteClientDataCallback; // Declare the callback variable at the class level

    private String selectedPetType;
    private ImageView imagepet;
    private List<String> selectedVaccines = new ArrayList<>();

    private Spinner spinnerpetbreed;
    Button registerpet;
    FirebaseDatabase database;
    DatabaseReference reference;
    int days;

    private Spinner spinnerpettype;
    String checkOutDate, checkInDate;

    private Spinner spinnerVaccine;
    private ArrayAdapter<String> vaccineAdapter;
    private TableLayout vaccineTable;
    private String selectedBreed;
    private ArrayAdapter<String> spinnerAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri capturedImageUri;
    String clientId;
    private MaterialDatePicker<Pair<Long, Long>> mMaterialDatePicker;


    int looptoregisterpet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        FirebaseApp.initializeApp(this);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        progress = findViewById(R.id.progressstore);
        name = findViewById(R.id.petnameclient);
        spinnerVaccine = findViewById(R.id.vaccineSpinner);
        vaccineTable = findViewById(R.id.vaccineTable);
        checkin = findViewById(R.id.editTextCheckIn);
        spinnerpettype = findViewById(R.id.spinnerpettype);
        spinnerpetbreed = findViewById(R.id.spinnerpetbreed);
        imagepet = findViewById(R.id.imagepet);
        registerpet = findViewById(R.id.buttonregisterpet);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerpettype.setAdapter(spinnerAdapter);
        spinnerAdapter.add("Pet Type"); // Add the title as the first item
        spinnerAdapter.add("Dog");
        spinnerAdapter.add("Cat");


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
                selectedPetType = spinnerpettype.getSelectedItem().toString();

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


        Intent intent = getIntent();
        looptoregisterpet = intent.getIntExtra("totalpettoregister", 0);
        clientId = intent.getStringExtra("clientkey");

        if (looptoregisterpet > 0) {
            displayAddPetActivity();
        }
        else {
            finish();
        }

        imagepet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });



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
                        Toast.makeText(AddPet.this, "You cannot add another vaccine when 'No Vaccine' is selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccine.equals("No Vaccine")) {
                        // If there are other vaccines in the list, prevent adding "No Vaccine"
                        Toast.makeText(AddPet.this, "You cannot add 'No Vaccine' when other vaccines are selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccines.contains(selectedVaccine)) {
                        // If the selected vaccine is already in the list, show a message
                        Toast.makeText(AddPet.this, "Vaccine already selected.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add the selected vaccine to the list
                        registerpet.setEnabled(true);
                        selectedVaccines.add(selectedVaccine);
                        updateVaccinesTable();
                    }
                } else {
                    Toast.makeText(AddPet.this, "Please select a valid vaccine.", Toast.LENGTH_SHORT).show();
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

    private void displayAddPetActivity() {
        name.setVisibility(View.VISIBLE);
        checkin.setVisibility(View.VISIBLE);
        spinnerVaccine.setVisibility(View.VISIBLE);
        imagepet.setVisibility(View.VISIBLE);
        vaccineTable.setVisibility(View.VISIBLE);
        spinnerpettype.setVisibility(View.VISIBLE);
        spinnerpetbreed.setVisibility(View.VISIBLE);

        registerpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if all required fields are filled
                String petName = name.getText().toString().trim();
                String selectedPetType = spinnerpettype.getSelectedItem().toString();
                String selectedBreed = spinnerpetbreed.getSelectedItem().toString();
                String selectedVaccine = spinnerVaccine.getSelectedItem().toString();

                if (petName.isEmpty() || selectedPetType.equals("Pet Type") || selectedBreed.equals("Select Breed") || selectedVaccine.equals("Select Vaccine") || checkInDate == null || checkInDate.isEmpty() || checkOutDate == null || checkOutDate.isEmpty() || capturedImageUri == null) {
                    // Show an error message if any required field is empty or invalid
                    Toast.makeText(AddPet.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled correctly, proceed to store the pet data
                storepet();
                looptoregisterpet--;

                if (looptoregisterpet > 0) {
                    // Clear the input fields for the next pet registration
                    progress.setVisibility(View.GONE);
                    spinnerpettype.setSelection(0);
                    spinnerpetbreed.setSelection(0);
                    name.getText().clear();
                    checkin.getText().clear();
                    spinnerVaccine.setSelection(0);
                    imagepet.setImageResource(R.drawable.baseline_add_24);
                    selectedVaccines.clear();
                    vaccineTable.removeAllViews();
                    displayAddPetActivity();
                    Toast.makeText(AddPet.this, "Register for the next pet", Toast.LENGTH_SHORT).show();
                } else {
                    progress.setVisibility(View.GONE);
                    // Show confirmation dialog to stop adding pets
                    Intent intent = new Intent(AddPet.this, MainScreenStaff.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void countAndSetTotalPetsForClient() {
        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("Pet");

        // Create a query to filter pets by clientID
        Query query = petsRef.orderByChild("clientID").equalTo(clientId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int petCount = (int) dataSnapshot.getChildrenCount();

                if (petCount < 1) {
                    deleteClientData(new OnDeleteClientDataCallback() {
                        @Override
                        public void onDeleteComplete() {
                            Toast.makeText(AddPet.this, "There was a problem during client and pet registration. Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Update the totalPets field in the Client path
                    DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Client").child(clientId);
                    clientRef.child("totalPets").setValue(petCount);

                    // Notify the callback that the operation is complete
                    if (onDeleteClientDataCallback != null) {
                        onDeleteClientDataCallback.onDeleteComplete();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors or onCancelled events here
                Log.e("FirebaseError", "Error counting pets: " + databaseError.getMessage());
            }
        });
    }

    // Define an interface for the callback
    private interface OnDeleteClientDataCallback {
        void onDeleteComplete();
    }



    private interface PetCheckCallback {
        void onResult(boolean hasPets);
    }

    private void hasPetsForCurrentClient(final String clientId, final PetCheckCallback callback) {
        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("Pet");

        // Create a query to check if a pet with the given clientId exists
        Query query = petsRef.orderByChild("clientID").equalTo(clientId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasPets = dataSnapshot.exists();
                callback.onResult(hasPets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the query is canceled
                callback.onResult(false); // Handle the error case as well
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPet.this);
        builder.setTitle("Confirmation")
                .setMessage("Are you sure you want to stop adding pets?\nYou can add pets later in the Edit Pet in Checked-In Clients")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update the totalPets value for the client in the database
                        updateTotalPetsInClient();
                        Toast.makeText(AddPet.this, "All client's pets registered successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog and continue adding pets
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        // Check if there are pets associated with the current client
        hasPetsForCurrentClient(clientId, new PetCheckCallback() {
            @Override
            public void onResult(boolean hasPets) {
                if (hasPets) {
                    // Pets exist for the client, show a confirmation dialog
                    showConfirmationDialog();
                } else {
                    // No pets exist for the client, show a different dialog or take action
                    showNoPetsDialog();
                }
            }
        });
    }

    // Define a method to show a dialog when there are no pets for the client
    private void showNoPetsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPet.this);
        builder.setTitle("Confirmation")
                .setMessage("There are no pets associated with this client. Do you want to delete the client data?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteClientData(new OnDeleteClientDataCallback() {
                            @Override
                            public void onDeleteComplete() {
                                finish(); // Optionally, you can finish the current activity
                            }
                        });
                    }
                })


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog and stay on the current screen
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteClientData(final OnDeleteClientDataCallback callback) {
        DatabaseReference clientsRef = FirebaseDatabase.getInstance().getReference("Client");

        // Create a DatabaseReference to the specific client data using the clientId
        DatabaseReference clientDataRef = clientsRef.child(clientId);

        // Use removeValue() to delete the client data
        clientDataRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data successfully deleted
                        // You can perform any additional actions or show a message here
                        Toast.makeText(AddPet.this, "Client Data Deleted", Toast.LENGTH_SHORT).show();
                        // Notify the callback that the operation is complete
                        if (callback != null) {
                            callback.onDeleteComplete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to delete data
                        // Handle the error or show an error message
                        Toast.makeText(AddPet.this, "Failed to delete client data", Toast.LENGTH_SHORT).show();
                        // Notify the callback that the operation is complete
                        if (callback != null) {
                            callback.onDeleteComplete();
                        }
                    }
                });
    }

    private void updateTotalPetsInClient() {
        DatabaseReference petsReference = reference.child("Pet");
        Query petsQuery = petsReference.orderByChild("clientID").equalTo(clientId);

        petsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int petCount = (int) dataSnapshot.getChildrenCount();

                DatabaseReference clientReference = reference.child("Client").child(clientId);
                clientReference.child("totalPets").setValue(petCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddPet.this, "There's error during storing data you may try again", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void storepet() {
        String petID = reference.child("Pet").push().getKey();
        String petName = name.getText().toString().trim();
        String petBreed = selectedBreed;

        if (petName.isEmpty() || selectedPetType.isEmpty() || petBreed.isEmpty() || checkInDate == null || checkInDate.isEmpty() || checkOutDate == null || checkOutDate.isEmpty() || capturedImageUri == null || selectedVaccines.isEmpty() || selectedVaccines.contains("Select Vaccine")) {
            Toast.makeText(AddPet.this, "Please fill in all the fields correctly", Toast.LENGTH_SHORT).show();
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
                HelperClassPet pet = new HelperClassPet(petID, clientId, petName, selectedPetType, petBreed, checkInDate, checkOutDate, days, imageUrl, true);
                petReference.setValue(pet);

                DatabaseReference vaccinesReference = petReference.child("Vaccines");
                vaccinesReference.setValue(selectedVaccines)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Pet data is successfully stored
                                Toast.makeText(AddPet.this, "Pet data is successfully stored!", Toast.LENGTH_SHORT).show();
                                resetForm(); // Reset the form for the next pet registration
                                countAndSetTotalPetsForClient();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddPet.this, "Failed to store vaccines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPet.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        displayAddPetActivity();
        //Toast.makeText(AddPet.this, "Register for the next pet", Toast.LENGTH_SHORT).show();
    }



}