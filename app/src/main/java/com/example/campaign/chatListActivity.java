package com.example.campaign;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.campaign.Model.chatsListModel;
import com.example.campaign.Model.chatList;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.adapter.chatListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class chatListActivity extends AppCompatActivity {
    private List<chatList> list;
    private RecyclerView recyclerView;
    private String lastMessage,date,time;
    private static List<String>  chatUserNames,chatUserIds=new ArrayList<>();
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(chatListActivity.this));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent=new Intent(chatListActivity.this,UsersActivity.class);
            startActivity(intent);
        });
        database = FirebaseDatabase.getInstance();
        list=new ArrayList<>();

        getChatList();
        if(chatUserIds.size() > 0){
            for(int i=0;i>chatUserIds.size();i++){
                list.add(new chatList(chatUserIds.get(i),chatUserNames.get(i),"","",""));
            }
            recyclerView.setAdapter(new chatListAdapter(list, getApplicationContext()));
        }
    }
    private void getChatList(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        chatUserIds=new ArrayList<>();
        chatUserNames=new ArrayList<>();
        chatUserIds.clear();
        DatabaseReference chatUserIdRef=database.getReference().child("chats").child(user.getUid());
        DatabaseReference chatUserNameRef=database.getReference().child("UserDetails");
        chatUserIdRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    String userId= Objects.requireNonNull(snapshot.getKey());
                    chatUserIdRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener(){
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    if(chatUserIds.contains(userId)){
                    }
                    else{
                        chatUserIds.add(userId);
                        chatUserNameRef.child(userId).addValueEventListener(new ValueEventListener(){
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                chatsListModel user=dataSnapshot.getValue(chatsListModel.class);
                                String userName=user.getName();
                                String profileUrl=user.getProfileUrI();
                                chatUserNames.add(userName);
                                int i= Collections.frequency(chatUserNames,userName);
                                if(i<=1){
                                    try{
                                        LocalDateTime myDateObj = LocalDateTime.now();
                                        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                        String formattedDate = myDateObj.format(dateObj);
                                        if(formattedDate.equals(date)){
                                            list.add(new chatList(userId,userName,lastMessage,time,profileUrl));
                                        }else{
                                            list.add(new chatList(userId,userName,lastMessage,date,profileUrl));
                                        }

                                    }catch(Exception e){
                                        Log.d("error",e.getLocalizedMessage());
                                    }

                                }
                                recyclerView.setAdapter(new chatListAdapter(list,chatListActivity.this));


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}