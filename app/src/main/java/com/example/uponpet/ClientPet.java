package com.example.uponpet;

public class ClientPet {
    private String clientPhoneNumber;
    private String petName;

    public ClientPet(String clientPhoneNumber, String petName) {
        this.clientPhoneNumber = clientPhoneNumber;
        this.petName = petName;
    }

    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    public String getPetName() {
        return petName;
    }

    @Override
    public String toString() {
        return clientPhoneNumber + " - " + petName;
    }
}
