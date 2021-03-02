package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.adapter.chatListAdapter;
import com.example.campaign.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity  {
     private List<String> chatListId;
     private RecyclerView recyclerView;
     private FloatingActionButton newChat;
     private TextView welcomeMsg;
     private ImageView welcomeEmoji;
     private Context context;
     private FirebaseUser user;
     private FirebaseDatabase database;
     private List<chatListModel> list;
     private chatListAdapter chatListAdapter;
     private Handler handler;
     private MenuInflater menuInflater;
     private String lastMessage,time,date,userName,profileUrI,descriptionId;
     private String TAG ="chatListAct";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setHasFixedSize(true);
        database = FirebaseDatabase.getInstance();
        DatabaseReference onlineRef=database.getReference();
        onlineRef.child("onlineStatus").child(user.getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean online=snapshot.getValue(Boolean.class);
                if(!online){
                    onlineRef.setValue(true);
                    onlineRef.onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        context=getApplicationContext();
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
        ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    int position = viewHolder.getAdapterPosition();

                        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(50);
                        String otherUserId = list.get(position).getUserId();
                        Toast.makeText(context, "chat deleted", Toast.LENGTH_LONG).show();
                        DatabaseReference chatRef = database.getReference().child("chats").child(user.getUid()).child(otherUserId);
                        chatRef.removeValue();
                        list.remove(position);
                        chatListAdapter.notifyItemRemoved(position);




                }catch (Exception e){
                    Log.e("Error",e.getLocalizedMessage());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.Red_200))
                        .addActionIcon(R.drawable.remove)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
    private void getLastMessage(String userId,String userName,String profileUrI) {
        DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid());

        handler.post(() -> messageRef.child(userId).addValueEventListener(new ValueEventListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
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
                        descriptionId=dataSnapshot.getKey();

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
                chat.setDescriptionId(descriptionId);
                chat.setTime(time);
                list.add(chat);
                Collections.sort(list);

                if (chatListAdapter!=null){
                        chatListAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));



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