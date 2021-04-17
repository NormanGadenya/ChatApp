package com.example.campaign.Model;

import java.io.Serializable;

public class messageListModel implements Serializable {
    private String text,profileUrI;
    private int backgroundColor;
    private String receiver,date,time,messageStatus, imageUrI,type,userName,messageId;
    private boolean checked;




    public messageListModel(){

    }
    public messageListModel(String text, String sender, String date, String time, String messageStatus, String imageUrI, String type,String userName){

        this.text = text;
        this.receiver = sender;
        this.date= date;
        this.time= time;
        this.messageStatus = messageStatus;
        this.imageUrI=imageUrI;
        this.type = type;
        this.userName=userName;

    }
    public messageListModel(String date){
        this.date=date;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String sender) {
        this.receiver = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getImageUrI() {
        return imageUrI;
    }

    public void setImageUrI(String imageUrI) {
        this.imageUrI = imageUrI;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileUrI() {
        return profileUrI;
    }

    public void setProfileUrI(String profileUrI) {
        this.profileUrI = profileUrI;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
