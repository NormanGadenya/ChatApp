package com.example.campaign.Model;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campaign.Repository.Repo;

import java.util.ArrayList;
import java.util.Set;

public class UserViewModel extends ViewModel {
    private MutableLiveData<userModel> fUserInfo;
    private MutableLiveData <userModel> otherUserInfo;
    private MutableLiveData<ArrayList<userModel>> userList;
    private MutableLiveData<Uri>selectedUri=new MutableLiveData<>();

    public void initUserList(Set<String > contacts){
        if(userList!=null){
            return;
        }
        userList= Repo.getInstance().getAllUsers(contacts);
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

    public LiveData<ArrayList<userModel>> getAllUsers(){
        return userList;
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
