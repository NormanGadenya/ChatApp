package com.example.campaign.Model;

public class messageListModel {
    private String text;
    private String receiver,date,time,messageStatus, imageUrI,type;



    public messageListModel(){

    }
    public messageListModel(String text, String sender, String date, String time, String messageStatus, String imageUrI, String type){

        this.text = text;
        this.receiver = sender;
        this.date= date;
        this.time= time;
        this.messageStatus = messageStatus;
        this.imageUrI=imageUrI;
        this.type = type;
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
}
