package com.neuralBit.letsTalk.Activities;

import static com.neuralBit.letsTalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_X;
import static com.neuralBit.letsTalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_Y;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

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

import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neuralBit.letsTalk.Common.ServiceCheck;
import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.ChatViewModel;
import com.neuralBit.letsTalk.Model.userModel;
import com.neuralBit.letsTalk.Services.updateStatusService;
import com.neuralBit.letsTalk.adapter.chatListAdapter;

import java.util.ArrayList;
import java.util.List;

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
    public static final String TAG="MainActivity";
    private Tools tools;
    private TextView textView1,textView2;
    private SharedPreferences settingsSharedPreferences;
    private CountDownTimer ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();

        newChat.setOnClickListener(this::presentActivity);
        list=chatViewModel.getChatListData().getValue();

        if(list!=null) {
            chatListAdapter = new chatListAdapter(list, MainActivity.this, this, viewModelStoreOwner, lifecycleOwner);
            chatListAdapter.textView1 =textView1;
            chatListAdapter.textView2 =textView2;

            chatListAdapter.setContactsSharedPrefs(contactsSharedPrefs);
            chatViewModel.getChatListData().observe(this, chatList -> {
                chatListAdapter.notifyDataSetChanged();

            });
            recyclerView.setAdapter(chatListAdapter);

        }
    }




    private void InitializeControllers() {

        recyclerView=findViewById(R.id.recyclerViewChatList);
        newChat=findViewById(R.id.newChat);
        list=new ArrayList<>();
        textView1 =findViewById(R.id.textView1);
        textView2 =findViewById(R.id.textView2);
        lifecycleOwner=this;
        viewModelStoreOwner=this;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        actionBar=getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Messages");
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        loadSharedPreferenceData();
        chatViewModel.initChatsList();
        tools = new Tools();
        settingsSharedPreferences=getSharedPreferences("Settings",MODE_PRIVATE);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck= new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
    }

    private void loadSharedPreferenceData() {
        contactsSharedPrefs=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);

    }

    private ArrayList<userModel> FilterList(String newText){
        ArrayList<userModel> newList=new ArrayList<>();
        for(userModel user:list){
            String name = contactsSharedPrefs.getString(user.getPhoneNumber(),null);
            if(name==null){
                name = user.getPhoneNumber();
            }
            if(name.toLowerCase().contains(newText.toLowerCase())){
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
                        Log.d(TAG, "onQueryTextSubmit: ");
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

    @Override
    protected void onPause() {
        super.onPause();
        fpCountDown();
    }

    private void fpCountDown(){

        if(settingsSharedPreferences.getBoolean("setFingerprint",false)){
            tools.context=getApplicationContext();
            ct=tools.setUpFPTime();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAndRemoveTask();

    }

    @Override
    protected void onResume() {

        if(tools.fpTimeout){
            if(settingsSharedPreferences.getBoolean("setFingerprint",false)){
                Intent intent = new Intent( MainActivity.this,FingerprintActivity.class);
                intent.putExtra("ActivityName",getClass().getCanonicalName());
                startActivity(intent);
            }
        }else{
            if(ct!=null){
                ct.cancel();
            }

        }
        super.onResume();
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