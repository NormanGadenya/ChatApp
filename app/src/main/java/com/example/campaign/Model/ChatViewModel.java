package com.example.campaign.Model;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campaign.Repository.Repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatViewModel extends ViewModel {

    MutableLiveData<String> mutableLiveData=new MutableLiveData<>();
    private MutableLiveData<ArrayList<userModel>> chatsList;
    private MutableLiveData<HashMap<String,messageListModel>> lastMessage;
    private MutableLiveData<List<messageListModel>> messageList;
    private MutableLiveData <userModel> otherUserInfo;
    private MutableLiveData <userModel> fUserInfo;
    private MutableLiveData<Uri>selectedUri=new MutableLiveData<>();

    public void setText(String s){
        mutableLiveData.setValue(s);
    }

    public MutableLiveData<String> getText(){
        return mutableLiveData;
    }



    public void initChatsList(){
        if (chatsList!=null){
            return;
        }
        chatsList= Repo.getInstance().getChatList();
    }

    public void initLastMessage(String userId){
//        if(lastMessage!=null){
//            return;
//        }

        lastMessage=Repo.getInstance().getLastMessage(userId);
    }

    public void initChats(String otherUserId){
        if(messageList!=null){
            return;
        }
        messageList=Repo.getInstance().getMessages(otherUserId);
    }

    public void initOtherUserInfo(String otherUserId){
        if(otherUserInfo!=null){
            return;
        }
        otherUserInfo=Repo.getInstance().getOtherUserInfo(otherUserId);
    }
    public void initFUserInfo(){
        if(fUserInfo!=null){
            return;
        }
        fUserInfo=Repo.getInstance().getFUserInfo();
    }

    public LiveData<ArrayList<userModel>> getChatListData(){
        return chatsList;
    }


    public LiveData<HashMap<String,messageListModel>> getLastMessage() {

        return lastMessage;
    }

    public void setLastMessage(MutableLiveData<HashMap<String,messageListModel>> lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LiveData<List<messageListModel>> getMessages(){
        return messageList;
    }

    public LiveData<userModel> getOtherUserInfo(){
        return otherUserInfo;
    }

    public LiveData<userModel> getFUserInfo(){
        return fUserInfo;
    }

    public LiveData<Uri> getSelectedUri() {

        return selectedUri;
    }

    public void setSelectedUri(Uri Uri) {
        selectedUri.setValue(Uri);
    }
}
