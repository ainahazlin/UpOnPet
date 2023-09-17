package com.example.uponpet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<HelperUpdatePet> dataList;
    private Context context;

    public MyAdapter(Context context, ArrayList<HelperUpdatePet> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HelperUpdatePet data = dataList.get(position);

        if (data.getMediaType().equals("image")) {
            // Display image

            holder.recyclerImage.setVisibility(View.VISIBLE);

            Glide.with(context).load(data.getImageUrl()).into(holder.recyclerImage);
            holder.recyclerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open the image details activity
                    Intent intent = new Intent(context, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", data.getImageUrl());
                    intent.putExtra("description", data.getDescription());
                    intent.putExtra("date", data.getSelectedDate());
                    intent.putExtra("name", data.getPetName());
                    intent.putExtra("mediatype", data.getMediaType());
                    context.startActivity(intent);
                }
            });
        } else if (data.getMediaType().equals("video")) {
            // Display video with custom layout
            holder.recyclerImage.setVisibility(View.GONE);
            holder.recyclerVideoThumbnail.setVisibility(View.VISIBLE);
            holder.playButtonOverlay.setVisibility(View.VISIBLE);


            // Load a video thumbnail (you can use Glide or another library)
            // First, you'll need to capture a frame from the video
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(data.getImageUrl()); // Set the video's data source
            Bitmap videoFrame = retriever.getFrameAtTime(0); // Capture the frame at the start of the video
            try {
                retriever.release(); // Release the MediaMetadataRetriever
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Glide.with(context)
                    .load(videoFrame)
                    .into(holder.recyclerVideoThumbnail);


            // Set a click listener to open the image details activity
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", data.getImageUrl());
                    intent.putExtra("description", data.getDescription());
                    intent.putExtra("date", data.getSelectedDate());
                    intent.putExtra("name", data.getPetName());
                    intent.putExtra("mediatype", data.getMediaType());
                    context.startActivity(intent);
                }
            });
        }

        holder.recyclerCaption.setText(data.getDescription());
        holder.recyclerDate.setText(data.getSelectedDate());
        holder.recyclerName.setText(data.getPetName());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView recyclerImage, playButtonOverlay,recyclerVideoThumbnail;
        TextView recyclerCaption;
        TextView recyclerDate;
        TextView recyclerName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImage = itemView.findViewById(R.id.recyclerImage);
            recyclerVideoThumbnail = itemView.findViewById(R.id.recyclerVideoThumbnail);
            playButtonOverlay = itemView.findViewById(R.id.playButtonOverlay);
            recyclerCaption = itemView.findViewById(R.id.recyclerCaption);
            recyclerDate = itemView.findViewById(R.id.recyclerDate);
            recyclerName = itemView.findViewById(R.id.recylerName);
        }
    }
}
