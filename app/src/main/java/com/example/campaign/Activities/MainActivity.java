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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
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

public class MainActivity extends AppCompatActivity  implements RecyclerViewInterface {
    private List<String> chatListId,arrangedChatListId;
    private RecyclerView recyclerView;
    private FloatingActionButton newChat;
    private TextView welcomeMsg;
    private ImageView welcomeEmoji;
    HashMap <String,String> messageArrange=new HashMap<>();

    private Context context;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private List<chatListModel> list;
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeControllers();
        requestContactsPermission();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Inbox");



        recyclerView.setHasFixedSize(true);
        database = FirebaseDatabase.getInstance();

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
        chatListAdapter=new chatListAdapter(list, MainActivity.this,this,this);
        recyclerView.setAdapter(chatListAdapter);
        getChatList();

        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
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
        notificationManagerCompat=NotificationManagerCompat.from(context);

    }

    Comparator comparator(){
        Comparator<Entry<String, String>> valueComparator = new Comparator<Entry<String,String>>() {

            @Override
            public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                String v1 = e1.getValue();
                String v2 = e2.getValue();
                return v2.compareTo(v1);
            }
        };
        return valueComparator;
    }

    void arrangeIdList(){
        Set<Entry<String, String>> entries = messageArrange.entrySet();

        List<Entry<String, String>> listOfEntries = new ArrayList<>(entries);
        Collections.sort(listOfEntries, comparator());
        LinkedHashMap<String, String> sortedByValue = new LinkedHashMap<>(listOfEntries.size());

        for(Entry<String, String> entry : listOfEntries){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }

        Set<Entry<String, String>> entrySetSortedByValue = sortedByValue.entrySet();

        for(Entry<String, String> mapping : entrySetSortedByValue) {
            arrangedChatListId.add(mapping.getKey());
        }
    }

    private void getChatList() {
        chatListId.clear();
        list.clear();
        arrangedChatListId=new ArrayList<>();
        DatabaseReference mRef=database.getReference().child("chats").child(user.getUid());
        DatabaseReference chatListRef=database.getReference().child("chats");
        chatListRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //list.clear();
                chatListId.clear();
                arrangedChatListId.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    chatListId.add(dataSnapshot.getKey());
                    chatUIds.add(dataSnapshot.getKey());
                    welcomeMsg.setVisibility(View.GONE);
                    welcomeEmoji.setVisibility(View.GONE);
                    String otherUserId=dataSnapshot.getKey();
                    mRef.child(otherUserId).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            arrangedChatListId.clear();
                            for(DataSnapshot snapshot1:snapshot.getChildren()){
                                messageArrange.put(snapshot.getKey(),snapshot1.getKey());

                            }
                            arrangeIdList();
                            getUserInfo();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                if(chatListId.size()==0){
                    welcomeMsg.setVisibility(View.VISIBLE);
                    welcomeEmoji.setVisibility(View.VISIBLE);
                }
//                getUInfo();
//                chatListAdapter.notifyDataSetChanged();

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
    private void getUserInfo() {
        DatabaseReference userDetailRef=database.getReference().child("UserDetails");
        userDetailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                HashMap <String,chatListModel> chatListOrder=new HashMap<>();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if(arrangedChatListId.contains(dataSnapshot.getKey())){
                        System.out.println(dataSnapshot);
                        String userId=dataSnapshot.getKey();
                        userModel user= dataSnapshot.getValue(userModel.class);
                        userName=user.getUserName();
                        profileUrI=user.getProfileUrI();
                        chatListModel chat=new chatListModel();
                        chat.setUserName(userName);
                        chat.setUserId(userId);
                        chat.setTyping(user.getTyping());
                        chat.setProfileUrI(profileUrI);
                        chat.setOnline(user.getOnline());
                        chatListOrder.put(dataSnapshot.getKey(),chat);
                    }
                }
                for (String id:arrangedChatListId){
                    list.add(chatListOrder.get(id));
                    chatListAdapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }




    private void saveSharedPreferenceData() {
        SharedPreferences sharedPreferences =getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putStringSet("contactsList",contactsList);
        editor.apply();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.chat_listmenu,menu);
        MenuItem searchItem=menu.findItem(R.id.search);
        MenuItem profileDetails=menu.findItem(R.id.profileButton);
        MenuItem settings=menu.findItem(R.id.settingsButton);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(searchItem);
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

    private void searchUsers(String query) {
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

            for (String userId : chatListId){
                userDetailRef.child(userId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        userModel user= snapshot.getValue(userModel.class);
                        Log.d(TAG,user.getUserName());
                        userName=user.getUserName();
                        profileUrI=user.getProfileUrI();
                        if (userName.toLowerCase().contains(query.toLowerCase())){
                            chatListModel chat=new chatListModel();
                            chat.setUserName(userName);
                            chat.setUserId(userId);
                            chat.setProfileUrI(profileUrI);
                            chat.setTyping(chat.getTyping());
                            list.add(chat);
                            chatListAdapter.notifyDataSetChanged();
                        }

                        Log.d(TAG,snapshot.toString());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
    }


    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }

//    public void sendNotifications(){
//        Notification notification=new NotificationCompat.Builder(context,MESSAGE_CHANNEL_ID)
//                .setSmallIcon(R.drawable.chat_communication_svgrepo_com)
//                .setContentTitle("Message: " + userName)
//                .setContentText(lastMessage)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .build();
//        notificationManagerCompat.notify(1,notification);
//    }

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
        if (requestCode == CONTACTS_REQUEST)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && contactsList!=null) {
                contactsList=getPhoneNumbers();
                saveSharedPreferenceData();

            } else {
                Toast.makeText(getApplicationContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter timeObj = DateTimeFormatter.ofPattern("HH:mm");
        return myDateObj.format(timeObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(dateObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void status(boolean status){
        DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(user.getUid());
        Map<String ,Object> onlineStatus=new HashMap<>();
        onlineStatus.put("lastSeenDate",getDate());
        onlineStatus.put("lastSeenTime",getTime());
        onlineStatus.put("online",status);
        userDetailRef.updateChildren(onlineStatus);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        status(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPause() {
        super.onPause();
        status(false);
    }
}