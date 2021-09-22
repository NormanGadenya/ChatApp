package com.example.campaign.Notifications;

public class Data {
    private String user;
    private int icon;
//    private String body;
    private String sender;
    private String title;
    private String message;
    private String phoneNumber;
    public Data(String user, int icon, String message,String phoneNumber, String sender,String title) {
        this.user = user;
        this.icon = icon;
        this.message = message;
//        this.body = body;
        this.phoneNumber=phoneNumber;

        this.sender = sender;
        this.title=title;


    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

//    public String getBody() {
//        return body;
//    }
//
//    public void setBody(String body) {
//        this.body = body;
//    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
