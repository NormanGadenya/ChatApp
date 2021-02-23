package com.example.campaign.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campaign.Model.usersModel;
import com.example.campaign.Model.chatList;

import java.util.ArrayList;
import java.util.List;

import Repository.Repo;

public class PageViewModel extends ViewModel {
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private MutableLiveData<ArrayList<chatList>> chatsList;
    private MutableLiveData<ArrayList<usersModel>> usersList;

    public void initChats(){
        if (chatsList!=null){
            return;
        }
        chatsList= Repo.getInstance().getChatList();
    }
    public void initContacts(List<String> contacts){
        if (usersList!=null){
            return;
        }
        usersList= Repo.getInstance().getUsersList(contacts);
    }
    public LiveData<ArrayList<chatList>> getChatData(){
        return chatsList;

    }
    public LiveData<ArrayList<usersModel>> getUsersData(){
        return usersList;

    }


    public void setIndex(int index) {
        mIndex.setValue(index);
    }




}