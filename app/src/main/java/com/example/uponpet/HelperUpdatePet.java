package com.example.uponpet;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class HelperUpdatePet implements Parcelable {
    private String id;
    private String clientphoneNumber;
    private String petName;
    private String description;
    private String selectedDate;
    private String imageUrl;
    private String mediaType; // Add mediaType field

    public HelperUpdatePet(String id, String clientphoneNumber, String petName, String description, String selectedDate, String imageUrl, String mediaType) {
        this.id = id;
        this.clientphoneNumber = clientphoneNumber;
        this.petName = petName;
        this.description = description;
        this.selectedDate = selectedDate;
        this.imageUrl = imageUrl;
        this.mediaType = mediaType;
    }

    public HelperUpdatePet() {
    }

    protected HelperUpdatePet(Parcel in) {
        id = in.readString();
        clientphoneNumber = in.readString();
        petName = in.readString();
        description = in.readString();
        selectedDate = in.readString();
        imageUrl = in.readString();
        mediaType = in.readString();
    }

    public static final Creator<HelperUpdatePet> CREATOR = new Creator<HelperUpdatePet>() {
        @Override
        public HelperUpdatePet createFromParcel(Parcel in) {
            return new HelperUpdatePet(in);
        }

        @Override
        public HelperUpdatePet[] newArray(int size) {
            return new HelperUpdatePet[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientPhoneNumber() {
        return clientphoneNumber;
    }

    public void setClientPhoneNumber(String phoneNumber) {
        this.clientphoneNumber = phoneNumber;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMediaType() {
        return mediaType;
    } // Add getter for mediaType

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(clientphoneNumber);
        dest.writeString(petName);
        dest.writeString(description);
        dest.writeString(selectedDate);
        dest.writeString(imageUrl);
        dest.writeString(mediaType);
    }
}
