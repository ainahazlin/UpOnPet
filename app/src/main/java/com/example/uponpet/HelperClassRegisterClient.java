package com.example.uponpet;

import java.util.List;

public class HelperClassRegisterClient {
    private String clientcontactNumber;
    private String clientname;
    private String clientaddress;
    private String petHotelEmail;
    private String itemcheckedIn;
    private String paymentstatus;

    private int totalPets;
    private String clientKey;
    private boolean activeFlag;
    private List<String> petNames; // Add this field
    private List<String> petBreeds; // Add this field
    private List<List<String>> petVaccines; // Add this field


    public HelperClassRegisterClient(String clientcontactNumber, String clientname, String clientaddress, String petHotelEmail,String itemcheckedIn,String paymentstatus, int totalPets, String clientKey, boolean activeFlag) {
        this.clientcontactNumber = clientcontactNumber;
        this.clientname = clientname;
        this.clientaddress = clientaddress;
        this.petHotelEmail = petHotelEmail;
        this.itemcheckedIn = itemcheckedIn;
        this.paymentstatus = paymentstatus;
        this.totalPets = totalPets;
        this.clientKey = clientKey;
        this.activeFlag = activeFlag;
    }
    public HelperClassRegisterClient() {
        // Empty constructor required for Firebase deserialization
    }

    public String getPaymentstatus() {
        return paymentstatus;
    }

    public void setPaymentstatus(String paymentstatus) {
        this.paymentstatus = paymentstatus;
    }

    public String getClientcontactNumber() {
        return clientcontactNumber;
    }

    public void setClientcontactNumber(String clientcontactNumber) {
        this.clientcontactNumber = clientcontactNumber;
    }

    public String getItemcheckedIn() {
        return itemcheckedIn;
    }

    public void setItemcheckedIn(String itemcheckedIn) {
        this.itemcheckedIn = itemcheckedIn;
    }

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getClientaddress() {
        return clientaddress;
    }

    public void setClientaddress(String clientaddress) {
        this.clientaddress = clientaddress;
    }

    public String getPetHotelEmail() {
        return petHotelEmail;
    }

    public void setPetHotelEmail(String petHotelEmail) {
        this.petHotelEmail = petHotelEmail;
    }

    public int getTotalPets() {
        return totalPets;
    }

    public void setTotalPets(int totalPets) {
        this.totalPets = totalPets;
    }

    public String getClientKey() {
        return clientKey;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public List<String> getPetNames() {
        return petNames;
    }

    public void setPetNames(List<String> petNames) {
        this.petNames = petNames;
    }

    public List<String> getPetBreeds() {
        return petBreeds;
    }

    public void setPetBreeds(List<String> petBreeds) {
        this.petBreeds = petBreeds;
    }

    public List<List<String>> getPetVaccines() {
        return petVaccines;
    }

    public void setPetVaccines(List<List<String>> petVaccines) {
        this.petVaccines = petVaccines;
    }
}

