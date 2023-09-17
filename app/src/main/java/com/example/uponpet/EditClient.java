package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class EditClient extends AppCompatActivity {

    private String clientId,type;

    private ArrayAdapter<String> spinnerAdapter;

    private Button delete, updateclient, updatepet, checkinback;
    private AutoCompleteTextView paymentstatus;

    private EditText upcname, upccontact, upcaddress, itembrought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_client);
        //showConfirmationDialog(client);
        Intent intent = getIntent();
        clientId = intent.getStringExtra("clientId");
        type = intent.getStringExtra("typeofclient");

        paymentstatus = findViewById(R.id.paymentstatus);
        itembrought = findViewById(R.id.itembrought);
        upcname = findViewById(R.id.updateclientname);
        upccontact = findViewById(R.id.updateclientcontacnumber);
        upcaddress = findViewById(R.id.updateclientaddress);
        delete = findViewById(R.id.buttonDelete);
        updateclient = findViewById(R.id.buttoneditclient);
        updatepet = findViewById(R.id.buttoneditpet);
        checkinback = findViewById(R.id.checkinback);


        if(type.equals("false")){
            checkinback.setVisibility(View.VISIBLE);

        }
        else if(type.equals("true"))
        {
            updatepet.setVisibility(View.VISIBLE);
            updateclient.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }

        updatepet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditClient.this, EditPet.class);
                intent.putExtra("clientId", clientId);
                startActivity(intent);
            }
        });


        checkinback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialogCheckIn();
            }
        });

        updateclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedClientName = upcname.getText().toString().trim();
                String updatedClientContact = formatPhoneNumber(upccontact.getText().toString().trim());
                String updatedClientAddress = upcaddress.getText().toString().trim();
                String updatedItemBrought = itembrought.getText().toString().trim();
                String updatedPaymentStatus = paymentstatus.getText().toString().trim();

                DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Client").child(clientId);
                clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentClientName = dataSnapshot.child("clientname").getValue(String.class);
                        String currentClientContact = dataSnapshot.child("clientcontactNumber").getValue(String.class);
                        String currentClientAddress = dataSnapshot.child("clientaddress").getValue(String.class);
                        String currentPaymentStatus = dataSnapshot.child("paymentstatus").getValue(String.class);
                        String currentItemBrought = dataSnapshot.child("itemcheckedIn").getValue(String.class);


                        // Check if any changes were made to the client details
                        if (updatedClientName.equals(currentClientName) && updatedClientContact.equals(currentClientContact) && updatedClientAddress.equals(currentClientAddress)&& updatedItemBrought.equals(currentItemBrought)&& updatedPaymentStatus.equals(currentPaymentStatus)) {
                            Toast.makeText(EditClient.this, "No changes made to the client", Toast.LENGTH_SHORT).show();
                        } else {
                            // Update the client details
                            clientRef.child("clientname").setValue(updatedClientName);
                            clientRef.child("clientcontactNumber").setValue(updatedClientContact);
                            clientRef.child("paymentstatus").setValue(updatedPaymentStatus);
                            clientRef.child("itemcheckedIn").setValue(updatedItemBrought);
                            clientRef.child("clientaddress").setValue(updatedClientAddress)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditClient.this, "Client updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditClient.this, "Failed to update client", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(EditClient.this, "Failed to retrieve client data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialogCheckOut();
            }
        });

        loadClientData();


    }

    public static String formatPhoneNumber(String phoneNumber) {
        // Remove any whitespace or dashes
        phoneNumber = phoneNumber.replaceAll("\\s", "").replaceAll("-", "");

        // Check if the phone number starts with "0" or "60"
        if (phoneNumber.startsWith("0")) {
            // Replace the "0" with "+60"
            phoneNumber = "+60" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("60")) {
            // Replace "60" with "+60"
            phoneNumber = "+60" + phoneNumber.substring(2);
        }

        return phoneNumber;
    }

    private void loadClientData() {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Client").child(clientId);
        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HelperClassRegisterClient client = dataSnapshot.getValue(HelperClassRegisterClient.class);
                    if (client != null) {
                        upcname.setText(client.getClientname());
                        upccontact.setText(client.getClientcontactNumber());
                        upcaddress.setText(client.getClientaddress());
                        itembrought.setText(client.getItemcheckedIn());
                        paymentstatus.setText(client.getPaymentstatus());


                        String[] paymentOptions = { "Deposit", "Unpaid", "Paid"};
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditClient.this, android.R.layout.simple_dropdown_item_1line, paymentOptions);

                        paymentstatus.setAdapter(adapter);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditClient.this, "Failed to load client data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialogCheckIn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check Out Client");
        builder.setMessage("Do you want to Reactive this client and their pets?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateActiveFlag(clientId, true); // Change the flag to true
                updatepet.setVisibility(View.VISIBLE);
                updateclient.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                checkinback.setVisibility(View.GONE);
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }


    private void showConfirmationDialogCheckOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check Out Client");
        builder.setMessage("Do you want to check out this client and their pets?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check the payment status for the client
                checkPaymentStatus(clientId);
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    private void checkPaymentStatus(String clientId) {
        // Assuming you have a Firebase Realtime Database reference to the payment status
        DatabaseReference paymentStatusRef = FirebaseDatabase.getInstance().getReference("Client").child(clientId).child("paymentstatus");

        paymentStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String paymentStatus = dataSnapshot.getValue(String.class);

                    if ("Paid".equals(paymentStatus)) {
                        // Payment is marked as "Paid," proceed with checkout
                        updateActiveFlag(clientId, false);
                        checkinback.setVisibility(View.VISIBLE);
                        updateclient.setVisibility(View.GONE);
                        updatepet.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);


                    } else {
                        // Payment is not marked as "Paid," prompt the user
                        showPaymentNotPaidDialog();
                    }
                } else {
                    Toast.makeText(EditClient.this, "Please Try Again", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the query is canceled
            }
        });
    }

    private void showPaymentNotPaidDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Not Paid");
        builder.setMessage("The client has not fully paid their payment.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



    private void updateActiveFlag(String clientId, boolean activeFlag) {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("Client").child(clientId);
        clientRef.child("activeFlag").setValue(activeFlag)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update activeFlag for associated pets
                        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("Pet");
                        petsRef.orderByChild("clientID").equalTo(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String petKey = snapshot.getKey();
                                    petsRef.child(petKey).child("activeFlag").setValue(activeFlag);
                                }
                                if (!activeFlag){
                                    Toast.makeText(EditClient.this, "Checked-Out Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(EditClient.this, "Failed to check out client and associated pets", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditClient.this, "Failed to check out client", Toast.LENGTH_SHORT).show();
                    }
                });
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