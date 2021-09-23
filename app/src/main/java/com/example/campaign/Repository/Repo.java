package com.example.campaign.Repository;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.view.View.GONE;

public class Repo {
    static Repo instance;
    private MutableLiveData<ArrayList<userModel>> chatList=new MutableLiveData<>();
    private MutableLiveData <HashMap<String,messageListModel>> lastMessage=new MutableLiveData<>();
    private HashMap<String,messageListModel> messageSet=new HashMap<>();
    private ArrayList<userModel> chats_List_Model= new ArrayList<>();
    private ArrayList<userModel> user_List_Model= new ArrayList<>();

    private MutableLiveData<userModel> otherUserInfo=new MutableLiveData<>();
    private MutableLiveData<userModel> fUserInfo=new MutableLiveData<>();
    private MutableLiveData<ArrayList<userModel>> userList=new MutableLiveData<>();
    private HashMap<String, String> messageArrange=new HashMap<>();
    private ArrayList<String> arrangedChatListId, chatListId,chatUIds;
    private MutableLiveData<ArrayList<messageListModel>> messageList=new MutableLiveData<>();

    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<messageListModel> messageListModel=new ArrayList<>();

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

//        loadCurrentUserInfo();
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
        Comparator<Map.Entry<String, String>> valueComparator = new Comparator<Map.Entry<String,String>>() {

            @Override
            public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                String v1 = e1.getValue();
                String v2 = e2.getValue();
                return v2.compareTo(v1);
            }
        };
        return valueComparator;
    }

    void arrangeIdList(){
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
                Log.d("snapshot",snapshot.toString());
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if(arrangedChatListId.contains(dataSnapshot.getKey())){
                        System.out.println(dataSnapshot);
                        String userId=dataSnapshot.getKey();
                        userModel user= dataSnapshot.getValue(userModel.class);
                        user.setUserId(userId);
                        chatListOrder.put(dataSnapshot.getKey(),user);

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

    private void loadLastMessage(String userId){
        DatabaseReference messageRef=database.getReference();
        messageRef.child("chats").child(fUser.getUid()).child(userId).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageListModel message=new messageListModel();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    message=dataSnapshot.getValue(messageListModel.class);
                    messageSet.put(userId,message);
                }

                lastMessage.postValue(messageSet);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMessages(String otherUserId){
        DatabaseReference messageRef=database.getReference().child("chats").child(fUser.getUid()).child(otherUserId);
        DatabaseReference otherUserMRef=database.getReference().child("chats").child(otherUserId).child(fUser.getUid());
        Thread getMessageThread= new Thread(new Runnable() {
            @Override
            public void run() {
                messageRef.addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messageListModel.clear();
                        ArrayList<String> mKeys=new ArrayList<>();

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            try{
                                messageListModel message = snapshot.getValue(messageListModel.class);
                                message.setMessageId(snapshot.getKey());
                                String receiver = message.getReceiver();
                                message.setReceiver(receiver);
                                messageListModel.add(message);
                                messageList.postValue(messageListModel);
                                mKeys.add(snapshot.getKey());

                            }catch(Exception e){
                                Log.d("error1",e.getMessage());
                            }

                        }
                        Thread readMessageThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                otherUserMRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot otherSnapshot) {

                                        for(DataSnapshot s:otherSnapshot.getChildren()){

                                            if(mKeys.contains(s.getKey())){
                                                HashMap<String,Object> messageStatus=new HashMap<>();
                                                messageStatus.put("checked",true);
                                                Log.d("otherSnapshot",otherSnapshot.getKey() + "" + s.getKey());
                                                otherUserMRef.child(s.getKey()).updateChildren(messageStatus);


                                            }
                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        });
                        readMessageThread.start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        getMessageThread.start();



    }

    private void loadOtherUserInfo(String otherUserId) {
        DatabaseReference reference=database.getReference().child("UserDetails").child(otherUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel user= snapshot.getValue(userModel.class);
                otherUserInfo.postValue(user);

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
                fUserInfo.postValue(user);

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

    private void loadAllUsers(){
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List <String> phoneNumbersList=new ArrayList<>();
                user_List_Model.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    userModel userListObj=new userModel();
                    String id=dataSnapshot.getKey();
                    userModel users=dataSnapshot.getValue(userModel.class);
                    String phoneNumber=fUser.getPhoneNumber();
                    phoneNumbersList.add(fUser.getPhoneNumber());

                    userListObj.setUserName(users.getUserName());
                    userListObj.setPhoneNumber(users.getPhoneNumber());
                    userListObj.setProfileUrI(users.getProfileUrI());
                    userListObj.setUserId(id);
                    userListObj.setOnline(users.getOnline());
                    user_List_Model.add(userListObj);
                    Collections.sort(user_List_Model,userModel::compareTo);
                    userList.postValue(user_List_Model);

//                    int i= Collections.frequency(phoneNumbersList,users.getPhoneNumber());
//                    if(contacts!=null && users!= null){
//                        if ( !users.getPhoneNumber().equals(phoneNumber)) {
//                            if (i <=1 && contacts.contains(phoneNumber)){
//
//                            }
//                        }
//
//                    }

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
        String userId;

        GetLastMessage(String userId){
            this.userId=userId;
        }

        @Override
        public void run() {
            loadLastMessage(userId);
        }
    }

    class GetMessages implements Runnable {
        String otherUserId;
         GetMessages(String otherUserId){
             this.otherUserId=otherUserId;
         }
        @Override
        public void run() {
            loadMessages(otherUserId);
        }
    }

    class GetOtherUserInfo implements Runnable {
        String otherUserId;
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
         private SharedPreferences contactsSharedPrefs;
        GetAllUsers(SharedPreferences contactsSharedPrefs){
            this.contactsSharedPrefs=contactsSharedPrefs;
        }
        @Override
        public void run() {
            loadAllUsers(contactsSharedPrefs);
        }
    }




}
