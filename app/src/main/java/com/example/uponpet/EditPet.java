package com.example.uponpet;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EditPet extends AppCompatActivity {
    private String clientId;
    private MaterialDatePicker<Pair<Long, Long>> mMaterialDatePicker;
    private List<String> selectedVaccines = new ArrayList<>();


    private String selectedPetName;
    private String petID;
    private Spinner petname;
    DatabaseReference catRef;
    private String storedCheckInDate;
    private String storedCheckOutDate;

    ArrayAdapter<String> spinnerAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri capturedImageUri;
    ArrayList<String> petNames = new ArrayList<>();
    ArrayList<String> petIds = new ArrayList<>();
    private ArrayAdapter<String> spinnerBreedAdapter;

    private ArrayList<String> petIDsArray = new ArrayList<>();

    private DatePickerDialog.OnDateSetListener checkInDateSetListener;
    private DatePickerDialog.OnDateSetListener checkOutDateSetListener;

    String checkInDate, checkOutDate;
    int days;
    Button delpet, updatepet, addpet;
    private EditText updatecheckin;
    private ImageView imagepet;

    String selectedBreed;
    private AutoCompleteTextView updatebreed;

    private Spinner spinnerVaccine;
    private ArrayAdapter<String> vaccineAdapter;
    private TableLayout vaccineTable;


    private class CustomArrayAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> originalItems;
        private List<String> filteredItems;

        public CustomArrayAdapter(EditPet context, String[] items) {
            super(context, android.R.layout.simple_dropdown_item_1line, items);
            originalItems = new ArrayList<>(Arrays.asList(items));
            filteredItems = new ArrayList<>(originalItems);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    filteredItems.clear();

                    for (String item : originalItems) {
                        if (item.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }

                    results.values = filteredItems;
                    results.count = filteredItems.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);
        Intent intent = getIntent();
        clientId = intent.getStringExtra("clientId");

        addpet = findViewById(R.id.buttonaddpet);
        delpet = findViewById(R.id.buttonDelete);
        updatepet = findViewById(R.id.buttonupdatepet);
        petname = findViewById(R.id.spinnerpetname);
        updatecheckin = findViewById(R.id.updatecheckin);
        updatebreed = findViewById(R.id.updatebreed);
        imagepet = findViewById(R.id.imagepet);


        String[] breedChoices = {
                "Persian Cat", "Maine Coon", "Siamese Cat", "Ragdoll",
                "Bengal Cat", "Sphynx Cat", "British Shorthair", "Scottish Fold",
                "Labrador Retriever", "German Shepherd", "Golden Retriever",
                "French Bulldog", "Beagle", "Poodle", "Bulldog", "Pomeranian"
        };


        updatebreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dropdown list
                updatebreed.showDropDown();
            }
        });
        // ArrayAdapter setup and initialization for breedChoices
        ArrayAdapter<String> spinnerBreedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, breedChoices
        );

        CustomArrayAdapter breedAdapter = new CustomArrayAdapter(this, breedChoices);
        updatebreed.setAdapter(breedAdapter);
        updatebreed.setThreshold(1); // Set a threshold to start filtering after 1 character

        updatebreed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Perform filtering here
                breedAdapter.getFilter().filter(editable.toString());
            }
        });


        // Initialize the spinner with an empty list
        spinnerAdapter = new ArrayAdapter<>(EditPet.this, android.R.layout.simple_spinner_item, petNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petname.setAdapter(spinnerAdapter);
        setthespinner();

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Dates"); // You can set other properties here if needed
        mMaterialDatePicker = builder.build(); // Build the MaterialDatePicker instance


        updatecheckin.setOnClickListener(new View.OnClickListener() {
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

                updatecheckin.setText(checkInDate + " - " + checkOutDate);

                long durationInMillis = selection.second - selection.first;
                days = (int) ((durationInMillis / (24 * 60 * 60 * 1000)) + 1);

                // Store the values in instance variables
                storedCheckInDate = checkInDate;
                storedCheckOutDate = checkOutDate;
            }
        });


        addpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPet.this, AddPetEdit.class);
                intent.putExtra("clientId", clientId);
                startActivity(intent);
            }
        });

        // Define a reference to the "Pet" path in the Realtime Database
        catRef = FirebaseDatabase.getInstance().getReference("Pet");

        // Retrieve the pet names based on the clientId
        catRef.orderByChild("clientID").equalTo(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there is only one matching pet, retrieve its data
                    DataSnapshot petSnapshot = dataSnapshot.getChildren().iterator().next();
                    // Retrieve the check-in and check-out dates from the selected pet
                    // Retrieve the check-in and check-out dates from the selected pet
                    String datein = petSnapshot.child("dateIn").getValue(String.class);
                    String dateout = petSnapshot.child("dateOut").getValue(String.class);
                    String petbreed = petSnapshot.child("petBreed").getValue(String.class);
                    updatebreed.setText(petbreed);

                    updatecheckin.setText(convertDateFormat(datein) + "-" + convertDateFormat(dateout));


                    // Set the selectedPetName variable
                    selectedPetName = petSnapshot.child("petName").getValue(String.class);
                    // Set the petname spinner selection
                    int selectedPosition = spinnerAdapter.getPosition(selectedPetName);
                    petname.setSelection(selectedPosition);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error case if needed
            }
        });

        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petNames.clear();
                petIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String petKey = snapshot.getKey();
                    boolean activeFlag = snapshot.child("activeFlag").getValue(Boolean.class);
                    String petName = snapshot.child("petName").getValue(String.class);
                    String clientid = snapshot.child("clientID").getValue(String.class);

                    if (activeFlag && petName != null && clientid.equals(clientId)) {
                        petNames.add(petName);
                        petIds.add(petKey);
                    } else {
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error case if needed
            }
        });


        delpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPet.this);
                builder.setMessage("Do you want to delete " + selectedPetName + "?\nThis action will permanently remove pet under the client and your pet hotel")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deletePet(petID, clientId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.create().show();
            }
        });

        imagepet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        updatepet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePet(petID);
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
                        selectedVaccines.add(selectedVaccine);
                        updateVaccinesTable();
                    } else if (selectedVaccines.size() == 1 && selectedVaccines.contains("No Vaccine")) {
                        // If the list contains only "No Vaccine," prevent adding any other vaccine
                        Toast.makeText(EditPet.this, "You cannot add another vaccine when 'No Vaccine' is selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccine.equals("No Vaccine")) {
                        // If there are other vaccines in the list, prevent adding "No Vaccine"
                        Toast.makeText(EditPet.this, "You cannot add 'No Vaccine' when other vaccines are selected.", Toast.LENGTH_SHORT).show();
                    } else if (selectedVaccines.contains(selectedVaccine)) {
                        // If the selected vaccine is already in the list, show a message
                        Toast.makeText(EditPet.this, "Vaccine already selected.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add the selected vaccine to the list
                        selectedVaccines.add(selectedVaccine);
                        updateVaccinesTable();
                    }
                } else {
                    Toast.makeText(EditPet.this, "Please select a valid vaccine.", Toast.LENGTH_SHORT).show();
                }
            }
        });

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


    private void setthespinner() {
        petname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPetName = parent.getItemAtPosition(position).toString();
                petID = petIds.get(position);

                // Retrieve the selected pet based on the pet ID
                catRef.child(petID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Retrieve the check-in and check-out dates from the selected pet
                            String checkInDate = dataSnapshot.child("dateIn").getValue(String.class);
                            String checkOutDate = dataSnapshot.child("dateOut").getValue(String.class);
                            String petbreed = dataSnapshot.child("petBreed").getValue(String.class);
                            String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class); // Add this line

                            // Load the image into the imagepet ImageView using Picasso
                            Picasso.get().load(imageUrl).fit().into(imagepet);
                            // Set breed and dates EditTexts
                            updatebreed.setText(petbreed);
                            updatecheckin.setText(convertDateFormat(checkInDate) + " - " + convertDateFormat(checkOutDate));

                            selectedVaccines.clear(); // Clear the list first

                            DataSnapshot vaccinesSnapshot = dataSnapshot.child("Vaccines");
                            for (DataSnapshot vaccineSnapshot : vaccinesSnapshot.getChildren()) {
                                String vaccine = vaccineSnapshot.getValue(String.class);
                                selectedVaccines.add(vaccine);
                            }

                            updateVaccinesTable(); // Update the vaccineTable
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error case if needed
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });
    }

    private void deletePet(String petID, String clientID) {
        DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference("Pet").child(petID);

        // Delete the pet data
        deleteRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully deleted the pet data
                Toast.makeText(EditPet.this, "Pet has been deleted", Toast.LENGTH_SHORT).show();

                // Reduce the totalPets count of the client by 1
                DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Client").child(clientID);
                clientRef.child("totalPets").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer totalPets = mutableData.getValue(Integer.class);
                        if (totalPets == null) {
                            // If the totalPets value is null, set it to 0
                            totalPets = 0;
                        } else {
                            // Decrease the totalPets count by 1
                            totalPets--;
                        }
                        mutableData.setValue(totalPets);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                        if (committed) {
                            // Check if totalPets is now 0
                            if (dataSnapshot != null) {
                                Integer totalPets = dataSnapshot.child("totalPets").getValue(Integer.class);
                                if (totalPets != null && totalPets == 0) {
                                    // Prompt the user to delete the client
                                    showDeleteClientConfirmation(clientID);
                                } else {
                                    // TotalPets is not 0, so refresh the spinner
                                    setthespinner();
                                }
                            }
                        } else {
                            Toast.makeText(EditPet.this, "Client total pet is not reduced", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure case if needed
            }
        });
    }

    private void showDeleteClientConfirmation(String clientID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Client");
        builder.setMessage("The client has no more pets. Do you want to delete the client?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the client and navigate to AddPetEdit activity
                deleteClient();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User chose not to delete the client, so navigate to AddPetEdit activity
                goToAddPetEditActivity();
            }
        });
        builder.create().show();
    }

    private void deleteClient() {
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
                        Toast.makeText(EditPet.this, "Client Data Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to delete data
                        // Handle the error or show an error message
                        Toast.makeText(EditPet.this, "Failed to delete client data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToAddPetEditActivity() {
        Intent intent = new Intent(EditPet.this, AddPetEdit.class);
        intent.putExtra("clientId", clientId);
        startActivity(intent);
    }



    private void updatePet(String petID) {
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("Pet").child(petID);

        updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean breedChanged = isBreedChanged(dataSnapshot);
                boolean vaccinesChanged = areVaccinesChanged(dataSnapshot);
                boolean datesChanged = areDatesChanged(dataSnapshot);
                boolean imageChanged = isImageChanged(dataSnapshot);

                if (breedChanged || vaccinesChanged || datesChanged || imageChanged) {
                    if (breedChanged) {
                        updateRef.child("petBreed").setValue(updatebreed.getText().toString());
                    }

                    if (vaccinesChanged) {
                        updateRef.child("Vaccines").setValue(selectedVaccines);
                    }

                    if (imageChanged) {
                        uploadImageToStorage(petID);
                    }

                    if (datesChanged) {
                        updateRef.child("dateIn").setValue(storedCheckInDate);
                        updateRef.child("dateOut").setValue(storedCheckOutDate);
                        updateRef.child("duration").setValue(days);
                        //updatecheckin.setText(convertDateFormat(storedCheckInDate) + " - " + convertDateFormat(storedCheckOutDate));
                    }

                    setthespinner();
                    Toast.makeText(EditPet.this, "Pet information has been updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditPet.this, "No changes made", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error case if needed
            }
        });
    }

    private boolean isBreedChanged(DataSnapshot dataSnapshot) {
        String currentPetBreed = dataSnapshot.child("petBreed").getValue(String.class);
        String newBreed = updatebreed.getText().toString().trim();
        return !newBreed.isEmpty() && !Objects.equals(currentPetBreed, newBreed);
    }

    private boolean areVaccinesChanged(DataSnapshot dataSnapshot) {
        List<String> currentVaccines = new ArrayList<>();
        DataSnapshot vaccinesSnapshot = dataSnapshot.child("Vaccines");
        for (DataSnapshot vaccineSnapshot : vaccinesSnapshot.getChildren()) {
            String vaccine = vaccineSnapshot.getValue(String.class);
            currentVaccines.add(vaccine);
        }

        // Check if selectedVaccines is empty or contains only "No Vaccine"
        if (selectedVaccines.isEmpty() || (selectedVaccines.size() == 1 && selectedVaccines.get(0).equals("No Vaccine"))) {
            // Add "No Vaccine" as the vaccine
            selectedVaccines.clear();
            selectedVaccines.add("No Vaccine");
        }

        return !selectedVaccines.isEmpty() && !selectedVaccines.equals(currentVaccines);
    }


    private boolean areDatesChanged(DataSnapshot dataSnapshot) {
        String currentDateIn = dataSnapshot.child("dateIn").getValue(String.class);
        String currentDateOut = dataSnapshot.child("dateOut").getValue(String.class);

        return !storedCheckInDate.isEmpty() && !storedCheckOutDate.isEmpty() &&
                (!Objects.equals(storedCheckInDate, currentDateIn) ||
                        !Objects.equals(storedCheckOutDate, currentDateOut) ||
                        days != dataSnapshot.child("duration").getValue(Integer.class));
    }




    private boolean isImageChanged(DataSnapshot dataSnapshot) {
        String currentImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
        if (capturedImageUri == null || capturedImageUri.toString().isEmpty()) {
            return false;
        }
        return !capturedImageUri.toString().equals(currentImageUrl);
    }



    private void uploadImageToStorage(String petID) {
        // Create a reference to the storage location
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("pet_images").child(petID);
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("Pet").child(petID);

        // Upload the image to the storage location
        storageRef.putFile(capturedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the image download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                // Update the image URL in the Realtime Database
                                updateRef.child("imageUrl").setValue(downloadUrl.toString());
                                Toast.makeText(EditPet.this, "Pet image has been updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure case if needed
                    }
                });
    }

    private String convertDateFormat(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ProfilePetHotel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }
}
