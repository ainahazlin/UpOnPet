package com.example.uponpet;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhoneNumberAdapter extends ArrayAdapter<String> implements Filterable {

    private List<String> phoneNumbers;
    private List<String> filteredPhoneNumbers;

    public PhoneNumberAdapter(Context context, int resource, List<String> phoneNumbers) {
        super(context, resource, phoneNumbers);
        this.phoneNumbers = phoneNumbers;
        this.filteredPhoneNumbers = new ArrayList<>(phoneNumbers);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint != null) {
                    String input = constraint.toString().trim().toLowerCase(Locale.getDefault());

                    for (String phoneNumber : phoneNumbers) {
                        String number = phoneNumber.toLowerCase(Locale.getDefault());

                        if (number.contains(input)) {
                            filteredList.add(phoneNumber);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredPhoneNumbers.clear();
                filteredPhoneNumbers.addAll((List<String>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return filteredPhoneNumbers.size();
    }

    @Override
    public String getItem(int position) {
        return filteredPhoneNumbers.get(position);
    }
}
