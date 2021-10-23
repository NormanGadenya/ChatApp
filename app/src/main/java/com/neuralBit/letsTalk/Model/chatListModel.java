package com.neuralBit.letsTalk.Model;

public class chatListModel implements Comparable {
    private String userId;
    private String userName;
    private String description,descriptionId;
    private String date,time;
    private String phoneNumber;


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


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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


    public String getDescriptionId() {
        return descriptionId;
    }

}
