package com.example.campaign.Model;

public class userModel {
    private String userName,userId;
    private String phoneNumber;
    private String profileUrI;
    private String online;
    private String lastSeenDate;
    private String lastSeenTime;

    public userModel(){

    }
    public userModel(String userName, String phoneNumber, String profileUrI, String userId){
        this.userName = userName;
        this.phoneNumber=phoneNumber;
        this.profileUrI=profileUrI;
        this.userId=userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }



    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileUrI(String profileUrI) {
        this.profileUrI = profileUrI;
    }

    public String getProfileUrI() {
        return profileUrI;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getLastSeenDate() {
        return lastSeenDate;
    }

    public void setLastSeenDate(String lastSeenDate) {
        this.lastSeenDate = lastSeenDate;
    }

    public String getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }
}
