package com.example.campaign.Repository;

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
    private MutableLiveData<List<messageListModel>> messageList=new MutableLiveData<>();

    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
    List<messageListModel> messageListModel=new ArrayList<>();

    public static Repo getInstance() {
        if(instance == null){
            instance= new Repo();
        }

        return instance;
    }

    public MutableLiveData<ArrayList<userModel>> getChatList(){
        if (chats_List_Model!=null) {
            loadChatList();
            chatList.setValue(chats_List_Model);
            chats_List_Model.clear();
        }

        return chatList;
    }

    public MutableLiveData<HashMap<String , messageListModel>> getLastMessage(String userId){
        loadLastMessage(userId);

        return  lastMessage;
    }

    public MutableLiveData<List<messageListModel>> getMessages(String otherUserId){
        loadMessages(otherUserId);
        messageList.setValue(messageListModel);
        return messageList;
    }

    public MutableLiveData<userModel> getOtherUserInfo(String otherUserId){
        loadOtherUserInfo(otherUserId);
        return otherUserInfo;
    }
    public MutableLiveData<userModel> getFUserInfo(){
        loadCurrentUserInfo();
        return fUserInfo;
    }

    public MutableLiveData <ArrayList<userModel>> getAllUsers(Set<String> contacts){
        loadAllUsers(contacts);
        userList.setValue(user_List_Model);
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
//        ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                try {
//                    int position = viewHolder.getAdapterPosition();
//
//                        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
//                        vibrator.vibrate(50);
//                        String otherUserId = list.get(position).getUserId();
//                        Toast.makeText(context, "chat deleted", Toast.LENGTH_LONG).show();
//                        DatabaseReference chatRef = database.getReference().child("chats").child(user.getUid()).child(otherUserId);
//                        chatListId.remove(otherUserId);
//                        arrangedChatListId.remove(position);
////                        list.remove(position);
//                        chatRef.removeValue();
//
//
//                        //list.remove(position);
//                        chatListAdapter.notifyItemRemoved(position);
//
//
//                }catch (Exception e){
//                    Log.e("Error",e.getLocalizedMessage());
//                }
//            }
//
//            @Override
//            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                        .addBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.Red_200))
//                        .addActionIcon(R.drawable.remove)
//                        .create()
//                        .decorate();
//
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//
//            }
//        };
//        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);

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

        messageRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageListModel.clear();
                ArrayList<String> mKeys=new ArrayList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    try{

                        messageListModel message = snapshot.getValue(messageListModel.class);
                        String imageUrI=message.getImageUrI();
                        message.setMessageId(snapshot.getKey());
                        String receiver = message.getReceiver();
                        message.setReceiver(receiver);


                        messageListModel.add(message);
                        messageList.postValue(messageListModel);

                        mKeys.add(snapshot.getKey());
                        if(message.getReceiver()!=null){
//                            otherUserMRef.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot otherSnapshot) {
//                                    for(DataSnapshot s:otherSnapshot.getChildren()){
//
//                                        if(mKeys.contains(s.getKey())){
//                                            HashMap<String,Object> messageStatus=new HashMap<>();
//                                            messageStatus.put("checked",true);
//                                            otherUserMRef.child(s.getKey()).updateChildren(messageStatus);
//                                        }
//                                    }
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });

                        }



                    }catch(Exception e){
                        Log.d("error1",e.getMessage());
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadOtherUserInfo(String otherUserId) {
        DatabaseReference reference=database.getReference().child("UserDetails").child(otherUserId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void loadAllUsers(Set<String> contacts){
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

                    int i= Collections.frequency(phoneNumbersList,users.getPhoneNumber());
                    if (contacts!=null && !users.getPhoneNumber().equals(phoneNumber)) {
                        if (i <=1 && contacts.contains(phoneNumber)){
                            userListObj.setUserName(users.getUserName());
                            userListObj.setPhoneNumber(users.getPhoneNumber());
                            userListObj.setProfileUrI(users.getProfileUrI());
                            userListObj.setUserId(id);
                            userListObj.setOnline(users.getOnline());
                            user_List_Model.add(userListObj);
                            Collections.sort(user_List_Model,userModel::compareTo);
                            userList.postValue(user_List_Model);

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}
