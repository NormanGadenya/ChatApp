package com.example.campaign.Model;

public class Users {
    private String name,userId;
    private String about;
    private String phoneNumber;
    private String profileUrl;

    public Users(){

    }
    public Users(String name,String about,String phoneNumber,String profileUrl,String userId){
        this.name = name;
        this.about= about;
        this.phoneNumber=phoneNumber;
        this.profileUrl=profileUrl;
        this.userId=userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
