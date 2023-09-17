package com.example.uponpet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.media.MediaScannerConnection;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageDetailsActivity extends AppCompatActivity {

    private TextView petname, updatedate, updatedesc;
    private PhotoView imageDetails;
    private static final int VIDEO_PLAYER_REQUEST_CODE = 123; // You can choose any request code


    private ImageView videoDetails;
    ProgressBar progressBar;
    private boolean isVideoPlaying = false;
    private boolean isVideoDetailsVisible = false; // Store the visibility state

    private Uri videoUri;
    ImageButton buttonPlay, buttonPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);
        petname = findViewById(R.id.petname);
        updatedate = findViewById(R.id.date);
        updatedesc = findViewById(R.id.description);
        imageDetails = findViewById(R.id.imageDetails);
        videoDetails = findViewById(R.id.videoDetails);
        buttonPlay = findViewById(R.id.buttonPlay);
        progressBar = findViewById(R.id.progressBar);


        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVideoPlaying) {
                    playVideo();
                } else {
                    // Handle video pause here if needed
                }
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("petId")) {
            String petId = getIntent().getStringExtra("petId");
            String phoneNumberClient = getIntent().getStringExtra("phoneNumberClient");
            String petName = getIntent().getStringExtra("petName");
            String description = getIntent().getStringExtra("description");
            String dateTimeSelected = getIntent().getStringExtra("dateTime");
            String imageUrl = getIntent().getStringExtra("imageUri");
            String mediatype = getIntent().getStringExtra("mediatype");
            petname.setText("Pet Name: " + petName);
            updatedate.setText("Update Date & Time:" + dateTimeSelected);
            updatedesc.setText("Description: " + description);
            if (mediatype.equals("image")) {
                videoDetails.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imageDetails.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).into(imageDetails);
            } else if (mediatype.equals("video")) {
                buttonPlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                videoDetails.setVisibility(View.VISIBLE);
                videoUri = Uri.parse(imageUrl);
                Glide.with(this)
                        .load(imageUrl)  // Replace with the actual video thumbnail URL
                        .into(videoDetails);

                buttonPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to play video! Check your internet connection", Toast.LENGTH_SHORT).show();
            }


        } else {
            String imageUrl = getIntent().getStringExtra("imageUrl");
            String name = getIntent().getStringExtra("name");
            String date = getIntent().getStringExtra("date");
            String description = getIntent().getStringExtra("description");
            String mediatype = getIntent().getStringExtra("mediatype");
            petname.setText("Pet Name: " + name);
            updatedate.setText("Update Date & Time:" + date);
            updatedesc.setText("Description: " + description);
            if (mediatype.equals("image")) {
                videoDetails.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imageDetails.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).into(imageDetails);
            } else if (mediatype.equals("video")) {
                progressBar.setVisibility(View.GONE);
                videoDetails.setVisibility(View.VISIBLE);
                buttonPlay.setVisibility(View.VISIBLE);
                videoUri = Uri.parse(imageUrl);
                Glide.with(this)
                        .load(imageUrl)  // Replace with the actual video thumbnail URL
                        .into(videoDetails);

                buttonPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to play video! Check your internet connection", Toast.LENGTH_SHORT).show();

            }
        }

        imageDetails.setMaximumScale(10); // Set the maximum zoom scale as desired

        imageDetails.setOnLongClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission to save the image
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // Permission already granted, save the image
                saveImageToGallery();
            }
            return true;
        });
        videoDetails.setOnLongClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission to save the video
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // Permission already granted, save the video
                saveVideoToGallery();
            }
            return true;
        });

        if (savedInstanceState != null) {
            isVideoDetailsVisible = savedInstanceState.getBoolean("isVideoDetailsVisible", false);
            if (isVideoDetailsVisible) {
                // Restore the visibility of videoDetails
                videoDetails.setVisibility(View.VISIBLE);
            }
        }
    }


    private void saveVideoToGallery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Video");
        builder.setMessage("Do you want to save this video to your gallery?");
        builder.setPositiveButton("Save", (dialog, which) -> {
            if (videoUri != null) {
                // Save the video to the gallery
                String filename = "uponpet_video_" + System.currentTimeMillis() + ".mp4";
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Change the directory to DOWNLOADS
                File file = new File(directory, filename);

                try {
                    InputStream inputStream = getContentResolver().openInputStream(videoUri);
                    OutputStream outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Scan the newly saved video file so that it appears in the gallery
                    MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);

                    Toast.makeText(this, "Video saved to gallery", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the image
                saveImageToGallery();
            } else {
                Toast.makeText(this, "Permission denied. You may give permission on app info", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private void saveImageToGallery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Image");
        builder.setMessage("Do you want to save this image to your gallery?");
        builder.setPositiveButton("Save", (dialog, which) -> {
            Drawable drawable = imageDetails.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = drawableToBitmap(drawable);
                if (bitmap != null) {
                    // Save the image to the gallery
                    String filename = "uponpet_image_" + System.currentTimeMillis() + ".jpg";
                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File file = new File(directory, filename);

                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }



    private void playVideo() {
        if (videoUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");

            try {
                startActivityForResult(intent, VIDEO_PLAYER_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // If no video player is available, catch the exception and handle it accordingly
                Toast.makeText(ImageDetailsActivity.this, "No video player found", Toast.LENGTH_SHORT).show();
            }
        }
        buttonPlay.setVisibility(View.GONE);
        isVideoPlaying = true;

        // Store the visibility state before starting the video player
        isVideoDetailsVisible = videoDetails.getVisibility() == View.VISIBLE;
        videoDetails.setVisibility(View.VISIBLE);
        buttonPlay.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the visibility state
        outState.putBoolean("isVideoDetailsVisible", isVideoDetailsVisible);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_PLAYER_REQUEST_CODE) {
            // Check if the user finished watching the video
            if (resultCode == RESULT_OK) {
                // User returned from video player, navigate to MainScreenClient
                navigateToHome();
            } else {
                // Video playback may have been canceled, handle it as needed
            }
        }
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
}