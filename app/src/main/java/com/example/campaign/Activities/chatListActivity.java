package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.adapter.chatListAdapter;
import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class chatListActivity extends AppCompatActivity {
     private List<String> chatListId;
     private RecyclerView recyclerView;
     private FloatingActionButton newChat;
     private FirebaseUser user;
     private FirebaseDatabase database;
     private List<chatListModel> list;
     private chatListAdapter chatListAdapter;
     private Handler handler;
     private String lastMessage,time,date,userName,profileUrI;
     private String TAG ="chatListAct";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        InitializeControllers();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        database = FirebaseDatabase.getInstance();

        chatListAdapter=new chatListAdapter(list,chatListActivity.this);
        recyclerView.setAdapter(chatListAdapter);
        getChatList();
        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(chatListActivity.this , userListActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

    }



    private void InitializeControllers() {
        user= FirebaseAuth.getInstance().getCurrentUser();
        recyclerView=findViewById(R.id.recyclerViewChatList);
        newChat=findViewById(R.id.newChat);
        list=new ArrayList<>();
        chatListId =new ArrayList<>();
        handler=new Handler();

    }

    private void getChatList() {
        chatListId.clear();
        list.clear();


        DatabaseReference chatListRef=database.getReference().child("chats");
        chatListRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                chatListId.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    chatListId.add(dataSnapshot.getKey());

                }
                getUserInfo();

                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }



        });
    }
    private void getLastMessage(String userId,String userName,String profileUrI) {
        DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid());

        handler.post(new Runnable() {
            @Override
            public void run() {
                messageRef.child(userId).addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            try{
                                messageListModel m=dataSnapshot.getValue(messageListModel.class);
                                String text=m.getText();
                                String imageUrI=m.getImageUrI();
                                if(text!=null){
                                    if (text.length()>30){
                                        String i=text.substring(0,30);
                                        lastMessage=i+"...";
                                    } else{
                                        lastMessage=text;
                                    }
                                }
                                if(imageUrI!=null){
                                    lastMessage="photo";
                                }
                                date=m.getDate();
                                time=m.getTime();

                            }catch(Exception e){
                                Log.d("error",e.getMessage());
                            }
                        }
                        chatListModel chat=new chatListModel();
                        chat.setDate(date);
                        chat.setUserName(userName);
                        Log.d(TAG,userName);
                        chat.setUserId(userId);
                        chat.setProfileUrI(profileUrI);
                        chat.setDescription(lastMessage);
                        chat.setTime(time);
                        list.add(chat);
                        if (chatListAdapter!=null){
//                                chatListAdapter.notifyItemInserted(0);
                                chatListAdapter.notifyDataSetChanged();
//
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });



    }


    private void getUserInfo() {
        DatabaseReference userDetailRef=database.getReference().child("UserDetails");
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (String userID : chatListId){
                    userDetailRef.child(userID).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            userModel user= snapshot.getValue(userModel.class);
                            Log.d(TAG,user.getUserName());
                            userName=user.getUserName();
                            profileUrI=user.getProfileUrI();
                            getLastMessage(userID,userName,profileUrI);
                            Log.d(TAG,snapshot.toString());

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
//
                }
            }
        });
    }


}