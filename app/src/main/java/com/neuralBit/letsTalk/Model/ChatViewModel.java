package com.neuralBit.letsTalk.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neuralBit.letsTalk.Repository.Repo;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatViewModel extends ViewModel {

    MutableLiveData<String> mutableLiveData=new MutableLiveData<>();
    private MutableLiveData<ArrayList<userModel>> chatsList;
    private MutableLiveData<HashMap<String,messageListModel>> lastMessage;




    public void initChatsList(){
        if (chatsList!=null){
            return;
        }
        chatsList= Repo.getInstance().getChatList();
    }

    public void initLastMessage(String userId){

        lastMessage=Repo.getInstance().getLastMessage(userId);
    }



    public void setText(String s){
        mutableLiveData.setValue(s);
    }

    public MutableLiveData<String> getText(){

        return mutableLiveData;
    }


    public LiveData<ArrayList<userModel>> getChatListData(){

        return chatsList;
    }

    public LiveData <HashMap<String,messageListModel>> getLastMessage() {

        return lastMessage;
    }

    public void setLastMessage(MutableLiveData<HashMap<String,messageListModel>> lastMessage) {
        this.lastMessage = lastMessage;
    }


}
