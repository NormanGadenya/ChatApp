package com.example.campaign.Model;

public class chatsListModel {
    private String name, phoneNumber, profileUrI;
    public chatsListModel(){

    }
    public chatsListModel(String name, String phoneNumber,String profileUrI){

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileUrI = profileUrI;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name=name;
    }
    public String getProfileUrI(){return profileUrI;}
    public void setProfileUrI(String profileUrI){this.profileUrI = profileUrI;}


}
