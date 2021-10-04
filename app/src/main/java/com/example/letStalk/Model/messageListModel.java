package com.example.letStalk.Model;

import java.io.Serializable;

import java.util.List;

public class messageListModel implements Serializable {
    private String text;
    private String videoUrI;
    private String audioUrI;
    private String audioDuration;

    private String receiver;
    private String date;
    private String time;
    private String imageUrI;
    private String type;
    private String userName;
    private String messageId;
    private boolean checked;




    public messageListModel(){

    }
    public messageListModel(String text, String sender, String date, String time, String messageStatus, String imageUrI, String type,String userName){

        this.text = text;
        this.receiver = sender;
        this.date= date;
        this.time= time;
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



    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }




    public boolean isChecked() {
        return checked;
    }

    public String getVideoUrI() {
        return videoUrI;
    }

    public void setVideoUrI(String videoUrI) {
        this.videoUrI = videoUrI;
    }

    public String getAudioUrI() {
        return audioUrI;
    }

    public void setAudioUrI(String audioUrI) {
        this.audioUrI = audioUrI;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(String audioDuration) {
        this.audioDuration = audioDuration;
    }
}
