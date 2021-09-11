package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campaign.Common.ServiceCheck;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.Repository.Repo;
import com.example.campaign.Services.updateStatusService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity   {
    private List<String> chatListId,arrangedChatListId;
    private RecyclerView recyclerView;
    private FloatingActionButton newChat;
    private TextView welcomeMsg;
    private ImageView welcomeEmoji;
    HashMap <String,String> messageArrange=new HashMap<>();

    private Context context;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private List<userModel> list=new ArrayList<>();
    private chatListAdapter chatListAdapter;
    private Handler handler;
    public  Map<String, String> namePhoneMap= new HashMap<String, String>(); ;
    private MenuInflater menuInflater;
    private String userName,profileUrI;
    private String TAG ="chatListAct";
    private NotificationManagerCompat notificationManagerCompat;
    private List<String> messageKeys=new ArrayList<>();
    private Set<String> chatUIds=new HashSet<>();
    private HashMap <String ,userModel>userInfo=new HashMap<>();
    private HashMap <String ,messageListModel>messages=new HashMap<>();
    private int CONTACTS_REQUEST=110;
    private Set<String> contactsList=new HashSet<>();
    private ChatViewModel chatViewModel;
    ActionBar actionBar;
    Activity activity=this;
    private ViewModelStoreOwner viewModelStoreOwner;
    private LifecycleOwner lifecycleOwner;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();
        requestContactsPermission();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        actionBar=getSupportActionBar();
        actionBar.setTitle("Messages");
        actionBar.setElevation(10);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.initChatsList();
        database = FirebaseDatabase.getInstance();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck= new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(contactsList==null){
                        contactsList=getPhoneNumbers();
                        saveSharedPreferenceData();
                    }

                }
            });

        } else {
            requestContactsPermission();
        }


        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        list=chatViewModel.getChatListData().getValue();
        if(list!=null){
            chatListAdapter=new chatListAdapter(list, MainActivity.this,this,viewModelStoreOwner,lifecycleOwner);
            chatViewModel.getChatListData().observe(this, chatList -> chatListAdapter.notifyDataSetChanged());
            recyclerView.setAdapter(chatListAdapter);
        }


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
        notificationManagerCompat=NotificationManagerCompat.from(context);
        lifecycleOwner=this;
        viewModelStoreOwner=this;

    }




    private void saveSharedPreferenceData() {
        SharedPreferences sharedPreferences =getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putStringSet("contactsList",contactsList);
        editor.apply();
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
                        chatListAdapter.notifyDataSetChanged();
                    }else{
                        chatListAdapter=new chatListAdapter(list, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);
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
                    chatListAdapter.notifyDataSetChanged();


                }else{
                    chatListAdapter=new chatListAdapter(list, MainActivity.this,activity,viewModelStoreOwner,lifecycleOwner);

                }
                recyclerView.setAdapter(chatListAdapter);
                chatListAdapter.notifyDataSetChanged();
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionBar.setTitle("");
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                actionBar.setTitle("Messages");
                return false;
            }
        });
        profileDetails.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(intent);
                return false;
            }
        });
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
                startActivity(intent);
                return false;
            }
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
        intent.putExtra(UserListActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(UserListActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }


    public boolean isAlphanumeric2(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
                return false;
        }
        return true;
    }
    private Set<String> getPhoneNumbers() {
        Set<String> phoneNumbers=new HashSet<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            namePhoneMap.put(phoneNumber, name);

        }
        for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("+")){
                phoneNumbers.add(key);
            }else{
                if(isAlphanumeric2(key)){
                    Long i=Long.parseLong(key);
                    String j="+256"+i;
                    phoneNumbers.add(j);
                }
            }
        }
        phones.close();
        return phoneNumbers;
    }

    private void requestContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                android.Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && contactsList != null) {
                contactsList = getPhoneNumbers();
                saveSharedPreferenceData();

            } else {
                Toast.makeText(getApplicationContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }



}