package com.neuralBit.letsTalk.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.neuralBit.letsTalk.Repository.Repo;

import java.util.ArrayList;

public class MessageViewModel extends ViewModel {
    private MutableLiveData<ArrayList<messageListModel>> messageList;
    private MutableLiveData<String> otherUserLang;
    private MutableLiveData<String> fUserPrefLang;
    private MutableLiveData<Boolean> useTranslator;

    public void initChats(String otherUserId, FirebaseTranslator Translator){
        if(messageList!=null){
            return;
        }
        messageList= Repo.getInstance().getMessages(otherUserId, Translator);

    }

    public LiveData<ArrayList<messageListModel>> getMessages(){

        ArrayList<messageListModel> arrayList = messageList.getValue();
        for ( messageListModel m: arrayList){

        }
        return messageList;
    }

    public MutableLiveData<Boolean> getUseTranslator() {
        return useTranslator;
    }

    public void setUseTranslator(MutableLiveData<Boolean> useTranslator) {
        this.useTranslator = useTranslator;
    }

    public MutableLiveData<String> getfUserPrefLang() {
        return fUserPrefLang;
    }

    public void setfUserPrefLang(MutableLiveData<String> fUserPrefLang) {
        this.fUserPrefLang = fUserPrefLang;
    }

    public MutableLiveData<String> getOtherUserLang() {
        return otherUserLang;
    }

    public void setOtherUserLang(MutableLiveData<String> otherUserLang) {
        this.otherUserLang = otherUserLang;
    }
}
