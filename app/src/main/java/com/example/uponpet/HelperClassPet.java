package com.example.uponpet;

public class HelperClassPet {
    private String petID;
    private String clientID;
    private String petName;
    private String petType;
    private String petBreed;
    private String dateIn;
    private String dateOut;
    private Integer duration;
    private String imageUrl;
    private boolean activeFlag;

    public HelperClassPet(String petID, String clientID, String petName, String petType,String petBreed, String dateIn, String dateOut, Integer duration,String imageUrl, boolean activeFlag) {
        this.petID = petID;
        this.clientID = clientID;
        this.petName = petName;
        this.petType = petType;
        this.petBreed = petBreed;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.activeFlag = activeFlag;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public void setPetBreed(String petBreed) {
        this.petBreed = petBreed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public HelperClassPet() {
        // Initialize any necessary variables or fields
    }

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getDateIn() {
        return dateIn;
    }

    public void setDateIn(String dateIn) {
        this.dateIn = dateIn;
    }

    public String getDateOut() {
        return dateOut;
    }

    public void setDateOut(String dateOut) {
        this.dateOut = dateOut;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }
}
