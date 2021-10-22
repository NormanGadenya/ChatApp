package com.neuralBit.letsTalk.Repository;

import static android.view.View.GONE;
import static com.neuralBit.letsTalk.Common.Tools.MESSAGE_LEFT;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.messageListModel;
import com.neuralBit.letsTalk.Model.userModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Repo {
    static Repo instance;
    public static final String TAG = "Repo";
    private final MutableLiveData<ArrayList<userModel>> chatList=new MutableLiveData<>();
    private final MutableLiveData <HashMap<String,messageListModel>> lastMessage=new MutableLiveData<>();
    private final HashMap<String,messageListModel> messageSet=new HashMap<>();
    private final ArrayList<userModel> chats_List_Model= new ArrayList<>();
    private final ArrayList<userModel> user_List_Model= new ArrayList<>();
    private final MutableLiveData<userModel> otherUserInfo=new MutableLiveData<>();
    private final MutableLiveData<userModel> fUserInfo=new MutableLiveData<>();
    private final MutableLiveData<ArrayList<userModel>> userList=new MutableLiveData<>();
    private final HashMap<String, String> messageArrange=new HashMap<>();
    private ArrayList<String> arrangedChatListId, chatListId,chatUIds;
    private final MutableLiveData<ArrayList<messageListModel>> messageList=new MutableLiveData<>();
    private final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
    private final ArrayList<messageListModel> messageListModel=new ArrayList<>();
    private Tools tools = new Tools();
    public static Repo getInstance() {
        if(instance == null){
            instance= new Repo();

        }
        return instance;
    }

    public MutableLiveData<ArrayList<userModel>> getChatList(){
        if (chats_List_Model!=null && fUser!=null) {
            GetChatList getChatList=new GetChatList();
            new Thread(getChatList).start();
            chatList.setValue(chats_List_Model);
            chats_List_Model.clear();
        }
        return chatList;
    }

    public MutableLiveData<HashMap<String , messageListModel>> getLastMessage(String userId){
        if(fUser!=null){
            GetLastMessage getLastMessage=new GetLastMessage(userId);
            new Thread(getLastMessage).start();
        }

        return  lastMessage;
    }

    public MutableLiveData<ArrayList<messageListModel>> getMessages(String otherUserId){
        if(fUser!=null){
            GetMessages getMessages =new GetMessages(otherUserId);
            new Thread(getMessages).start();
            messageList.setValue(messageListModel);
        }

        return messageList;
    }

    public MutableLiveData<userModel> getOtherUserInfo(String otherUserId){
        if(fUser!=null){
            GetOtherUserInfo getOtherUserInfo =new GetOtherUserInfo(otherUserId);
            new Thread(getOtherUserInfo).start();
        }

        return otherUserInfo;
    }
    public MutableLiveData<userModel> getFUserInfo(){
        if(fUser!=null){
            GetFUserInfo getFUserInfo= new GetFUserInfo();
            new Thread(getFUserInfo).start();
        }

        return fUserInfo;
    }

    public MutableLiveData <ArrayList<userModel>> getAllUsers(SharedPreferences contactsSharedPrefs){
        if(fUser!=null){
            GetAllUsers getAllUsers = new GetAllUsers(contactsSharedPrefs);
            new Thread(getAllUsers).start();
            userList.setValue(user_List_Model);
        }

        return userList;
    }

    Comparator comparator(){
        return (Comparator<Map.Entry<String, String>>) (e1, e2) -> {
            String v1 = e1.getValue();
            String v2 = e2.getValue();
            return v2.compareTo(v1);
        };
    }

    private void arrangeIdList(){
        Set<Map.Entry<String, String>> entries = messageArrange.entrySet();
        List<Map.Entry<String, String>> listOfEntries = new ArrayList<>(entries);
        Collections.sort(listOfEntries, comparator());
        LinkedHashMap<String, String> sortedByValue = new LinkedHashMap<>(listOfEntries.size());
        for(Map.Entry<String, String> entry : listOfEntries){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }
        Set<Map.Entry<String, String>> entrySetSortedByValue = sortedByValue.entrySet();

        for(Map.Entry<String, String> mapping : entrySetSortedByValue) {
            arrangedChatListId.add(mapping.getKey());
        }
    }

    private void loadChatList() {
        chatListId=new ArrayList<>();
        arrangedChatListId=new ArrayList<>();
        chatUIds=new ArrayList<>();
        DatabaseReference mRef=database.getReference().child("chats").child(fUser.getUid());
        DatabaseReference chatListRef=database.getReference().child("chats");
        chatListRef.child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats_List_Model.clear();
                chatListId.clear();
                messageArrange.clear();
                arrangedChatListId.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    chatListId.add(dataSnapshot.getKey());
                    chatUIds.add(dataSnapshot.getKey());
                    String otherUserId=dataSnapshot.getKey();
                    mRef.child(otherUserId).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            arrangedChatListId.clear();
                            for(DataSnapshot snapshot1:snapshot.getChildren()){
                                messageArrange.put(snapshot.getKey(),snapshot1.getKey());
                            }
                            arrangeIdList();
                            getChatUserInfo();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getChatUserInfo() {
        DatabaseReference userDetailRef=database.getReference().child("UserDetails");
        userDetailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String,userModel> chatListOrder=new HashMap<>();
                chatListOrder.clear();
                chats_List_Model.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if(arrangedChatListId.contains(dataSnapshot.getKey())){
                        String userId=dataSnapshot.getKey();
                        userModel user= dataSnapshot.getValue(userModel.class);
                        assert user != null;
                        String profileUrI;
                        try {
                            if(user.getProfileUrI()!=null){
                                profileUrI = tools.decryptText(user.getProfileUrI());
                                user.setProfileUrI(profileUrI);
                            }

                            user.setUserId(userId);
                            chatListOrder.put(dataSnapshot.getKey(),user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }
                for (String id:arrangedChatListId){
                    chats_List_Model.add(chatListOrder.get(id));
                    chatList.setValue(chats_List_Model);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadLastMessage(String userId) {
        DatabaseReference messageRef=database.getReference();
        messageRef.child("lastMessage").child(fUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageListModel message;
                message=snapshot.getValue(messageListModel.class);
                String text = null;
                String translatedText = null;
                try {
                    translatedText = message.getTranslatedText()!=null? tools.decryptText(message.getTranslatedText()) : null;

                    text = tools.decryptText(message.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(translatedText!=null){
                    message.setTranslatedText(translatedText);
                }
                message.setText(text);
                messageSet.put(userId,message);
                lastMessage.postValue(messageSet);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMessages(String otherUserId){
        DatabaseReference fMRef=database.getReference().child("chats").child(fUser.getUid()).child(otherUserId);
        DatabaseReference otherUserMRef=database.getReference().child("chats").child(otherUserId).child(fUser.getUid());
        DatabaseReference otherUserLMRef=database.getReference().child("lastMessage").child(otherUserId).child(fUser.getUid());

        Thread getMessageThread= new Thread(() -> fMRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageListModel.clear();
                Log.d(TAG, "onDataChange: "+dataSnapshot);

                ArrayList<String> mKeys=new ArrayList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    try{
                        messageListModel message = snapshot.getValue(messageListModel.class);
                        message.setMessageId(snapshot.getKey());
                        String receiver = message.getReceiver();
                        message.setText(tools.decryptText(message.getText()));
                        String translatedText = message.getTranslatedText()!=null? tools.decryptText(message.getTranslatedText()) : null;
                        String imageUri=message.getImageUrI()!=null? tools.decryptText(message.getText()) : null;
                        String audioUrI=message.getAudioUrI()!=null? tools.decryptText(message.getAudioUrI()) : null;
                        String videoUrI=message.getVideoUrI()!=null? tools.decryptText(message.getVideoUrI()) : null;
                        if(imageUri!=null){
                            message.setImageUrI(imageUri);
                        }else if(videoUrI!=null){
                            message.setVideoUrI(videoUrI);
                        }else if(audioUrI!=null){
                            message.setAudioUrI(audioUrI);
                        }

                        if(translatedText!=null){
                            message.setTranslatedText(translatedText);
                        }
                        message.setReceiver(receiver);
                        messageListModel.add(message);
                        messageList.postValue(messageListModel);
                        mKeys.add(snapshot.getKey());




                    }catch(Exception e){
                        Log.d("error1",e.getMessage());
                    }

                }
                Thread readMessageThread = new Thread(() -> otherUserMRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot otherSnapshot) {

                        for(DataSnapshot s:otherSnapshot.getChildren()){

                            if(mKeys.contains(s.getKey())){
                                messageListModel m = s.getValue(messageListModel.class);
                                if(m.getReceiver()!=null){
                                    HashMap<String,Object> messageStatus=new HashMap<>();
                                    if(m.getReceiver().equals(fUser.getUid())){
                                        messageStatus.put("checked",true);
                                        otherUserMRef.child(s.getKey()).updateChildren(messageStatus);
                                        otherUserLMRef.updateChildren(messageStatus);
                                    }

                                }

                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }));
                readMessageThread.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
        getMessageThread.start();



    }
    private void loadOtherUserInfo(String otherUserId) {
        DatabaseReference reference=database.getReference().child("UserDetails").child(otherUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel user= snapshot.getValue(userModel.class);
                try {

                    if(user.getProfileUrI()!=null){
                        user.setProfileUrI( tools.decryptText(user.getProfileUrI()));

                    }
                    otherUserInfo.postValue(user);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCurrentUserInfo(){
        DatabaseReference myRef = database.getReference().child("UserDetails").child(fUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel user=snapshot.getValue(userModel.class);
                try {
                    assert user != null;
                    if(user.getProfileUrI()!=null){
                        user.setProfileUrI( tools.decryptText(user.getProfileUrI()));
                    }
                    fUserInfo.postValue(user);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllUsers(SharedPreferences contactsSharedPrefs){
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List <String> phoneNumbersList=new ArrayList<>();
                user_List_Model.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    userModel userListObj=new userModel();
                    String userId=dataSnapshot.getKey();
                    userModel users=dataSnapshot.getValue(userModel.class);
                    String phoneNumber=fUser.getPhoneNumber();

                    phoneNumbersList.add(fUser.getPhoneNumber());

                    int i= Collections.frequency(phoneNumbersList,users.getPhoneNumber());
                    if(contactsSharedPrefs!=null && users!= null){
                        if ( !users.getPhoneNumber().equals(phoneNumber)) {

                            if (i <=1 && contactsSharedPrefs.contains(phoneNumber)){
                                String userName=contactsSharedPrefs.getString(users.getPhoneNumber(),null);
                                if(userName!=null){
                                    userListObj.setUserName(userName);
                                }else{
                                    userListObj.setUserName(users.getUserName());
                                }
                                userListObj.setPhoneNumber(users.getPhoneNumber());
                                userListObj.setProfileUrI(users.getProfileUrI());
                                userListObj.setUserId(userId);
                                userListObj.setOnline(users.getOnline());
                                user_List_Model.add(userListObj);
                                Collections.sort(user_List_Model,userModel::compareTo);
                                userList.postValue(user_List_Model);
                            }
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class GetChatList implements Runnable {
        @Override
        public void run() {
            loadChatList();
        }
    }

    class GetLastMessage implements Runnable {
        final String userId;


        GetLastMessage(String userId){

            this.userId=userId;

        }

        @Override
        public void run() {
            loadLastMessage(userId);
        }
    }

    class GetMessages implements Runnable {
        final String otherUserId;

         GetMessages(String otherUserId){
             this.otherUserId=otherUserId;
         }
        @Override
        public void run() {
            loadMessages(otherUserId);
        }
    }

    class GetOtherUserInfo implements Runnable {
        final String otherUserId;
        GetOtherUserInfo(String otherUserId){
             this.otherUserId=otherUserId;
         }
        @Override
        public void run() {
            loadOtherUserInfo(otherUserId);
        }
    }

    class GetFUserInfo implements Runnable {

        @Override
        public void run() {
            loadCurrentUserInfo();
        }
    }

    class GetAllUsers implements Runnable {
         private final SharedPreferences contactsSharedPrefs;
        GetAllUsers(SharedPreferences contactsSharedPrefs){
            this.contactsSharedPrefs=contactsSharedPrefs;
        }
        @Override
        public void run() {
            loadAllUsers(contactsSharedPrefs);
        }
    }






}
