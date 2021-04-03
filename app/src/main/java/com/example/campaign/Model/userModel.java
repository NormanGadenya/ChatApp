package com.example.campaign.Model;

public class userModel implements Comparable{
    private String userName,userId;
    private String phoneNumber;
    private String profileUrI;
    private Boolean online;
    private String lastSeenDate;
    private String lastSeenTime;
    private String chatWallpaper;
    private int chatBlur;
    private Boolean Typing;

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

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
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

    public String getChatWallpaper() {
        return chatWallpaper;
    }

    public void setChatWallpaper(String chatWallpaper) {
        this.chatWallpaper = chatWallpaper;
    }

    public int getChatBlur() {
        return chatBlur;
    }

    public void setChatBlur(int chatBlur) {
        this.chatBlur = chatBlur;
    }

    public Boolean getTyping() {
        return Typing;
    }

    public void setTyping(Boolean typing) {
        this.Typing = typing;
    }

    @Override
    public int compareTo(Object o) {
        userModel userModel =(userModel) o;
        return this.userName.compareTo(userModel.userName);
    }
}
