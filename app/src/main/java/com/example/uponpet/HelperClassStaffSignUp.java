package com.example.uponpet;

public class HelperClassStaffSignUp {
    String pethotelemail,pethotelname,pethoteladdress,pethotelcontact,pethotelpass;
    Boolean activeFlag;

    public HelperClassStaffSignUp(String pethotelemail, String pethotelname, String pethoteladdress, String pethotelcontact, String pethotelpass, Boolean activeFlag) {
        this.pethotelemail = pethotelemail;
        this.pethotelname = pethotelname;
        this.pethoteladdress = pethoteladdress;
        this.pethotelcontact = pethotelcontact;
        this.pethotelpass = pethotelpass;
        this.activeFlag = activeFlag;
    }

    public HelperClassStaffSignUp() {
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public String getPethotelname() {
        return pethotelname;
    }

    public void setPethotelname(String pethotelname) {
        this.pethotelname = pethotelname;
    }

    public String getPethoteladdress() {
        return pethoteladdress;
    }

    public void setPethoteladdress(String pethoteladdress) {
        this.pethoteladdress = pethoteladdress;
    }

    public String getPethotelemail() {
        return pethotelemail;
    }

    public void setPethotelemail(String pethotelemail) {
        this.pethotelemail = pethotelemail;
    }

    public String getPethotelcontact() {
        return pethotelcontact;
    }

    public void setPethotelcontact(String pethotelcontact) {
        this.pethotelcontact = pethotelcontact;
    }

    public String getPethotelpass() {
        return pethotelpass;
    }

    public void setPethotelpass(String pethotelpass) {
        this.pethotelpass = pethotelpass;
    }
}
