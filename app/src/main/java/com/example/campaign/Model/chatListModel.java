package com.example.campaign.Model;

public class chatListModel implements Comparable {
    private String userId;
    private String userName;
    private String description,descriptionId;
    private String date,time;
    private String phoneNumber, profileUrI;

    public chatListModel() {
    }

    public chatListModel(String userId, String userName, String description, String date, String time, String profileUrI) {
        this.userId = userId;
        this.userName = userName;
        this.description = description;
        this.date = date;
        this.time = time;
        this.profileUrI=profileUrI;


    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userID) {
        this.userId = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getProfileUrI() {
        return profileUrI;
    }

    public void setProfileUrI(String profileUrI) {
        this.profileUrI = profileUrI;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    @Override
    public int compareTo(Object o) {
        chatListModel chatListModel =(chatListModel) o;
        return chatListModel.getDescriptionId().compareTo(this.descriptionId);
    }

    public void setDescriptionId(String descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getDescriptionId() {
        return descriptionId;
    }
}
