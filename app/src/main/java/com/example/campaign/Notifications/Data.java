package com.example.campaign.Notifications;

public class Data {
    private String user;
    private int icon;
    private String body;
    private String sender;
    private String title;
    public Data(String user, int icon, String body, String sender,String title) {
        this.user = user;
        this.icon = icon;
        this.body = body;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
