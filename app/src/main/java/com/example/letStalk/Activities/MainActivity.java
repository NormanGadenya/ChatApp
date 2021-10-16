package com.example.letStalk.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.text.TextUtils;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.letStalk.Common.ServiceCheck;

import com.example.letStalk.Model.ChatViewModel;

import com.example.letStalk.Model.userModel;

import com.example.letStalk.Services.updateStatusService;
import com.example.letStalk.adapter.chatListAdapter;
import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

import static com.example.letStalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_X;
import static com.example.letStalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_Y;

public class MainActivity extends AppCompatActivity   {
    private RecyclerView recyclerView;
    private FloatingActionButton newChat;
    private List<userModel> list=new ArrayList<>();
    private chatListAdapter chatListAdapter;
    private MenuInflater menuInflater;
    private ChatViewModel chatViewModel;
    private ActionBar actionBar;
    private final Activity activity=this;
    private ViewModelStoreOwner viewModelStoreOwner;
    private LifecycleOwner lifecycleOwner;
    private SharedPreferences contactsSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        actionBar=getSupportActionBar();
        actionBar.setTitle("Messages");
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        loadSharedPreferenceData();
        chatViewModel.initChatsList();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck= new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
        newChat.setOnClickListener(this::presentActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        list=chatViewModel.getChatListData().getValue();

        if(list!=null) {
            chatListAdapter = new chatListAdapter(list, MainActivity.this, this, viewModelStoreOwner, lifecycleOwner);
            chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);
            chatViewModel.getChatListData().observe(this, chatList -> chatListAdapter.notifyDataSetChanged());
            recyclerView.setAdapter(chatListAdapter);

        }
    }



    private void InitializeControllers() {

        recyclerView=findViewById(R.id.recyclerViewChatList);
        newChat=findViewById(R.id.newChat);
        list=new ArrayList<>();
        ImageView welcomeEmoji = findViewById(R.id.welcomeEmoji);
        TextView welcomeMsg = findViewById(R.id.startNewChatMsg);
        lifecycleOwner=this;
        viewModelStoreOwner=this;
    }

    private void loadSharedPreferenceData() {
        contactsSharedPrefs=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);

    }

    private ArrayList<userModel> FilterList(String newText){
        ArrayList<userModel> newList=new ArrayList<>();
        for(userModel user:list){
            if(user.getUserName().toLowerCase().contains(newText.toLowerCase())){
                newList.add(user);
            }
        }
        return newList;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.chat_listmenu,menu);
        MenuItem searchItem=menu.findItem(R.id.search);
        MenuItem profileDetails=menu.findItem(R.id.profileButton);
        MenuItem settings=menu.findItem(R.id.settingsButton);

        SearchView searchView= (SearchView) MenuItemCompat.getActionView(searchItem);

        LifecycleOwner lifecycleOwner=this;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (list != null) {
                    ArrayList<userModel> newList=new ArrayList<>();
                    newList.clear();

                    if (!TextUtils.isEmpty(s.trim())){
                        newList=FilterList(s);
                        chatListAdapter=new chatListAdapter(newList, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);
                        chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);
                        chatListAdapter.notifyDataSetChanged();
                    }else{
                        chatListAdapter=new chatListAdapter(list, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);
                        chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);
                    }

                    recyclerView.setAdapter(chatListAdapter);
                    chatListAdapter.notifyDataSetChanged();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                ArrayList<userModel> newList=new ArrayList<>();
                newList.clear();


                if (newText.length()>0){
                    newList=FilterList(newText);
                    chatListAdapter=new chatListAdapter(newList, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);
                    chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);
                    chatListAdapter.notifyDataSetChanged();


                }else{
                    chatListAdapter=new chatListAdapter(list, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);
                    chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);

                }
                recyclerView.setAdapter(chatListAdapter);
                chatListAdapter.notifyDataSetChanged();
                return false;
            }
        });
        searchView.setOnSearchClickListener(v -> actionBar.setTitle(""));
        searchView.setOnCloseListener(() -> {
            actionBar.setTitle("Messages");
            return false;
        });
        profileDetails.setOnMenuItemClickListener(item -> {
            Intent intent=new Intent(getApplicationContext(), UserProfileActivity.class);
            startActivity(intent);
            return false;
        });
        settings.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
            startActivity(intent);
            return false;
        });
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

    }

    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);

        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra(EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }




}