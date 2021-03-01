package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

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
import java.util.NavigableMap;

public class MainActivity extends AppCompatActivity {
     private List<String> chatListId;
     private RecyclerView recyclerView;
     private FloatingActionButton newChat;
     private TextView welcomeMsg;
     private ImageView welcomeEmoji;
     private FirebaseUser user;
     private FirebaseDatabase database;
     private List<chatListModel> list;
     private chatListAdapter chatListAdapter;
     private Handler handler;
     private MenuInflater menuInflater;
     private String lastMessage,time,date,userName,profileUrI;
     private String TAG ="chatListAct";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setHasFixedSize(true);
        database = FirebaseDatabase.getInstance();

        chatListAdapter=new chatListAdapter(list, MainActivity.this);
        recyclerView.setAdapter(chatListAdapter);
        getChatList();
        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(MainActivity.this , userListActivity.class);
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
        welcomeEmoji=findViewById(R.id.welcomeEmoji);
        welcomeMsg=findViewById(R.id.startNewChatMsg);

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
                welcomeMsg.setVisibility(View.VISIBLE);
                welcomeEmoji.setVisibility(View.VISIBLE);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    chatListId.add(dataSnapshot.getKey());
                    welcomeMsg.setVisibility(View.GONE);
                    welcomeEmoji.setVisibility(View.GONE);
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
                                    lastMessage="issa_photo";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.chatmenu,menu);
        MenuItem item=menu.findItem(R.id.search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }else{
                    getChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())){
                    searchUsers(newText);
                }else{
                    getChatList();
                }
                return false;
            }
        });
        return true;
    }

    private void searchUsers(String query) {
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
                getSearchUserInfo(query);
                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getSearchUserInfo(String query) {
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
                            if (userName.toLowerCase().contains(query.toLowerCase())){
                                getLastMessage(userID,userName,profileUrI);
                            }

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