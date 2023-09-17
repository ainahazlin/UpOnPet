package com.example.uponpet;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Api;

import java.util.ArrayList;
import java.util.List;

public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolder> {
    private List<HelperClassRegisterClient> clients;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ClientListAdapter(Context context, List<HelperClassRegisterClient> clients, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.clients = clients;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClassRegisterClient client = clients.get(position);
        holder.textView1.setText("Name: " + client.getClientname());
        holder.textView2.setText("Contact Number: " + client.getClientcontactNumber());
        holder.textView3.setText("Address: " + client.getClientaddress());
        holder.textView4.setText("Item Checked-In: " + client.getItemcheckedIn());
        holder.textView5.setText("Payment Status: " + client.getPaymentstatus());
        holder.textView6.setText("Pet Registered: " + client.getTotalPets() + " pet");

        List<String> petNames = client.getPetNames();
        List<String> petBreeds = client.getPetBreeds();
        List<List<String>> petVaccines = client.getPetVaccines();

        if (petNames != null && petBreeds != null && petVaccines != null) {
            StringBuilder petInfoBuilder = new StringBuilder();
            for (int i = 0; i < petNames.size(); i++) {
                String petName = petNames.get(i);
                String petBreed = petBreeds.get(i);
                List<String> petVaccineList = petVaccines.get(i);

                // Convert the list of vaccines to a comma-separated string
                String petVaccinesString = TextUtils.join(", ", petVaccineList);

                // Append pet information to the builder
                petInfoBuilder.append("Pet ").append(i + 1).append(":\n")
                        .append("Name: ").append(petName).append("\n")
                        .append("Breed: ").append(petBreed).append("\n")
                        .append("Vaccine(s): ").append(petVaccinesString).append("\n\n");
            }

            holder.textViewPetName.setText(petInfoBuilder.toString());
        } else {
            holder.textViewPetName.setText("Pet Information: N/A");
        }
    }


    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView1, textView2, textView3, textView4, textView5,textView6, textViewPetName;

        public ViewHolder(View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.textView3);
            textView4 = itemView.findViewById(R.id.textView4);
            textView5 = itemView.findViewById(R.id.textView5);
            textView6 = itemView.findViewById(R.id.textView6);
            textViewPetName = itemView.findViewById(R.id.textViewCatName);

            itemView.setOnClickListener(this); // Set click listener on the item view
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

    public void setData(List<HelperClassRegisterClient> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }
}
