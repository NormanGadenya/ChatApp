package com.example.campaign.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campaign.Repository.Repo;

import java.util.List;

public class MessageViewModel extends ViewModel {
    private MutableLiveData<List<messageListModel>> messageList;

    public void initChats(String otherUserId){
        if(messageList!=null){
            return;
        }
        messageList= Repo.getInstance().getMessages(otherUserId);
    }

    public LiveData<List<messageListModel>> getMessages(){
        return messageList;
    }
}
