package com.example.uponpet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.uponpet.AnalyticPetHotelStaff;
import com.example.uponpet.MainScreenStaff;
import com.example.uponpet.NotificationSender;
import com.example.uponpet.ProfilePetHotel;
import com.example.uponpet.R;
import com.example.uponpet.SearchActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdatePetStaff extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri imageUri;

    private Uri latestMediaUri;

    private static final int REQUEST_IMAGE_SELECT = 2;
    private static final int REQUEST_VIDEO_CAPTURE = 3;
    private static final int REQUEST_MEDIA_SELECT = 100;

    private static final int PERMISSION_REQUEST_CODE = 4;
    private static final int REQUEST_VIDEO_TRIM = 5;
    private Uri mediaUri;
    private StorageReference storageRef;

    EditText updatedescription;
    TextView phonenum;
    ImageView imagetoupdate;
    Button mediabutton, updatetofirebase;
    String phonenumberclient;
    ProgressBar progresstoupdate;
    DatabaseReference databaseRef;
    private MediaController mediaController;
    private Uri videoUri;
    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet_staff);
        FirebaseApp.initializeApp(this);

        storageRef = FirebaseStorage.getInstance().getReference();
        videoView = findViewById(R.id.video_view);
        mediabutton = findViewById(R.id.addimage);

        SharedPreferences sharedPreferences = getSharedPreferences("PetHotel", Context.MODE_PRIVATE);
        String pethotelemail = sharedPreferences.getString("phemail", "");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavistaff);
        bottomNavigationView.setSelectedItemId(R.id.update);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homes) {
                    startActivity(new Intent(getApplicationContext(), MainScreenStaff.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.update) {
                    return true;
                } else if (itemId == R.id.analytics) {
                    startActivity(new Intent(getApplicationContext(), AnalyticPetHotelStaff.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (itemId == R.id.profiles) {
                    startActivity(new Intent(getApplicationContext(), ProfilePetHotel.class));
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            }
        });
        progresstoupdate = findViewById(R.id.progresstoupdatepet);
        updatedescription = findViewById(R.id.description);
        imagetoupdate = findViewById(R.id.imagepettoupdate);
        updatetofirebase = findViewById(R.id.updatepet);
        progresstoupdate.setVisibility(View.INVISIBLE);
        phonenum = findViewById(R.id.clientphonenum);

        phonenum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdatePetStaff.this, SearchActivity.class);
                intent.putExtra("pethotelmail", pethotelemail);
                startActivity(intent);
            }
        });


        Intent intent = getIntent();
        phonenumberclient = intent.getStringExtra("selectedItem");
        phonenum.setText(phonenumberclient);

        updatetofirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if latestMediaUri is not null (an image has been selected or captured)
                if (latestMediaUri != null) {
                    updatepetToFirebase();
                } else {
                    Toast.makeText(UpdatePetStaff.this, "Please select an image or video", Toast.LENGTH_SHORT).show();
                    progresstoupdate.setVisibility(View.INVISIBLE);
                }
            }
        });


        mediabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    try {
                        showMediaSelectionOptions();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    requestPermissions();
                }
            }
        });
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                try {
                    showMediaSelectionOptions();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "Permission denied. You may set the permissions manually in the app settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMediaSelectionOptions() throws IOException{
        // Create an Intent to pick media from the gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/* video/*"); // Allow both image and video selection

        startActivityForResult(galleryIntent, REQUEST_MEDIA_SELECT);
    }




    private void updatepetToFirebase() {
        String description = updatedescription.getText().toString().trim();
        String dateTimeSelected = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        databaseRef = FirebaseDatabase.getInstance().getReference("PetUpdate");
        String petId = databaseRef.push().getKey();
        StorageReference mediaRef = storageRef.child("media/" + petId + getFileExtension(mediaUri));

        if (phonenum == null || description.isEmpty() || latestMediaUri == null) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (latestMediaUri != null) {
            progresstoupdate.setVisibility(View.VISIBLE);

            UploadTask uploadTask = mediaRef.putFile(latestMediaUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mediaRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String[] petData = phonenumberclient.split(" - ");
                            String petName = petData[0];
                            String clientPhoneNumber = petData[1];

                            // Create a HelperUpdatePet object with the media URL
                            HelperUpdatePet pet = new HelperUpdatePet(petId, clientPhoneNumber, petName, description, dateTimeSelected, uri.toString(), getMediaType(latestMediaUri));
//                            Toast.makeText(UpdatePetStaff.this, "media "+getMediaType(mediaUri), Toast.LENGTH_SHORT).show();

                            // Save the HelperUpdatePet object to the Realtime Database
                            databaseRef.child(petId).setValue(pet);
                            progresstoupdate.setVisibility(View.INVISIBLE);

                            Toast.makeText(UpdatePetStaff.this, "Pet update saved successfully!", Toast.LENGTH_SHORT).show();
                            updatedescription.setText("");
                            phonenum.setText("");
                            imagetoupdate.setVisibility(View.GONE);
                            videoView.setVisibility(View.GONE);

                            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens").child(clientPhoneNumber);
                            tokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String token = dataSnapshot.getValue(String.class);
                                        if (token != null) {
                                            NotificationSender sender = new NotificationSender();
                                            sender.sendNotification(token, "UpOnPet", "You have new update on your Pet", pet);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(UpdatePetStaff.this, "Internet connection interrupt, Try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(UpdatePetStaff.this, "Failed to upload media! Try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Pet data saved unsuccessfully! Try again", Toast.LENGTH_SHORT).show();
        }
    }


    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            // Get the file extension from the URI
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            String type = mimeTypeMap.getMimeTypeFromExtension(fileExtension);

            if (type != null) {
                if (type.startsWith("image/")) {
                    return "image";
                } else if (type.startsWith("video/")) {
                    return "video";
                }
            }
        }
        return "";
    }



    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), MainScreenStaff.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_left);
        finish();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_SELECT) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri mediaUri = data.getData();
                    String mediaType = getMediaType(mediaUri);
                    if (mediaType.equals("image")) {
                        // Handle image selection
                        handleImage(mediaUri);
                    } else if (mediaType.equals("video")) {
                        // Handle video selection
                        try {
                            if (isVideoDurationValid(mediaUri, 15000)) {
                                handleVideo(mediaUri);
                            } else {
                                handleVideo(mediaUri);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Media selection canceled
                imagetoupdate.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                latestMediaUri=null;
                Toast.makeText(this, "Media selection canceled! Please select another media", Toast.LENGTH_SHORT).show();

            }
        }else{
            Toast.makeText(this, "Please select any media", Toast.LENGTH_LONG).show();

        }
    }


    private boolean isVideoDurationValid(Uri videoUri, long maxDuration) throws IOException {
        long videoDuration = getVideoDuration(videoUri);
        return videoDuration <= maxDuration;
    }


    private void handleImage(Uri imageUri) {
        imagetoupdate.setVisibility(View.VISIBLE);
        imagetoupdate.setScaleType(ImageView.ScaleType.FIT_CENTER); // Set ScaleType
        Picasso.get()
                .load(imageUri)
                .fit()
                .centerInside()
                .into(imagetoupdate);

        // Use the imageUri as the latestMediaUri
        this.latestMediaUri = imageUri;

        videoView.setVisibility(View.GONE); // Hide VideoView
    }

    private void handleVideo(Uri videoUri) throws IOException {
        long videoDuration;
        videoDuration = getVideoDuration(videoUri);
        if (videoDuration > 15000) {
            Toast.makeText(this, "Video exceed 15 seconds, You may choose video less than 15 seconds", Toast.LENGTH_LONG).show();

        } else {
            // If the video duration is within the allowed limit, proceed with displaying it
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(videoUri);
            videoView.start();

            // Use the videoUri as the latestMediaUri
            this.latestMediaUri = videoUri;

            imagetoupdate.setVisibility(View.GONE); // Hide ImageView
        }
    }


    private String getMediaType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Get the file extension from the URI
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String type = mimeTypeMap.getMimeTypeFromExtension(fileExtension);

        if (type != null) {
            if (type.startsWith("image/")) {
                return "image";
            } else if (type.startsWith("video/")) {
                return "video";
            }
        }
        return "";
    }


    private long getVideoDuration(Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoUri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = 0;
        if (durationString != null) {
            duration = Long.parseLong(durationString);
        }
        retriever.release();
        return duration;
    }


}

