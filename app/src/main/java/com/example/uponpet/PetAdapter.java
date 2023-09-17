package com.example.uponpet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<HelperClassPet> petList;

    // Constructor
    public PetAdapter() {
        petList = new ArrayList<>();
    }

    // ViewHolder class
    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetName;
        TextView tvPetType;
        TextView tvDateIn;
        TextView tvDateOut;

        TextView tvPetBreed;
        TextView tvPetVaccines;
        ImageView tvPetImage;


        public PetViewHolder(View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.petname);
            tvPetType = itemView.findViewById(R.id.pettype);
            tvDateIn = itemView.findViewById(R.id.checkin);
            tvDateOut = itemView.findViewById(R.id.checkout);
            tvPetBreed = itemView.findViewById(R.id.petbreed);
            tvPetVaccines = itemView.findViewById(R.id.petvaccines);
            tvPetImage = itemView.findViewById(R.id.imagepet);

        }
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_list, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        HelperClassPet pet = petList.get(position);
        holder.tvPetName.setText("Pet Name: " + pet.getPetName());
        holder.tvPetType.setText("Pet Type: " + pet.getPetType());
        holder.tvDateIn.setText("Check-In Date: " + pet.getDateIn());
        holder.tvDateOut.setText("Check-Out Date: " + pet.getDateOut());
        holder.tvPetBreed.setText("Breed: " + pet.getPetBreed());

        DatabaseReference vaccinesReference = FirebaseDatabase.getInstance().getReference("Pet").child(pet.getPetID()).child("Vaccines");
        vaccinesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> vaccines = new ArrayList<>();
                for (DataSnapshot vaccineSnapshot : dataSnapshot.getChildren()) {
                    String vaccine = vaccineSnapshot.getValue(String.class);
                    vaccines.add(vaccine);
                }

                // Set the vaccine information
                StringBuilder vaccinesText = new StringBuilder("Vaccines: ");
                for (String vaccine : vaccines) {
                    vaccinesText.append(vaccine).append(", ");
                }
                // Remove the trailing comma and space
                vaccinesText.setLength(vaccinesText.length() - 2);
                holder.tvPetVaccines.setText(vaccinesText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
            }
        });

        // Load the image using Glide
        Glide.with(holder.itemView.getContext()).load(pet.getImageUrl()).into(holder.tvPetImage);
    }


    @Override
    public int getItemCount() {
        return petList.size();
    }

    // Method to update the pet list
    public void setPetList(List<HelperClassPet> pets) {
        petList = pets;
    }
}
