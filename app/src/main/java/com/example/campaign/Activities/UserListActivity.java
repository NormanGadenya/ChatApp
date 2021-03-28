package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.adapter.userListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserListActivity extends AppCompatActivity {
    public static final String EXTRA_CIRCULAR_REVEAL_X="EXTRA_CIRCULAR_REVEAL_X" ;
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";

    View rootLayout;
    private int revealX;
    private int revealY;
    private List<String> chatListId;
    private RecyclerView recyclerView;

    private FirebaseUser user;
    private FirebaseDatabase database;
    private List<userModel> list;
    private com.example.campaign.adapter.userListAdapter userListAdapter;
    private Handler handler;
    private String TAG ="userAct";
    private ProgressBar progressBar;
    private Context context;
    private MenuInflater menuInflater;
    public  Map<String, String> namePhoneMap ;
    private int CONTACTS_REQUEST=110;
    private Set<String> contactsList=new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        InitializeControllers();
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        database = FirebaseDatabase.getInstance();
        userListAdapter=new userListAdapter(list, UserListActivity.this);

        recyclerView.setAdapter(userListAdapter);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    loadSharedPreferenceData();
                    if (contactsList==null){
                        contactsList=getPhoneNumbers();
                    }

                    loadUsers(contactsList);
                }
            });

        } else {
            requestContactsPermission();
        }
        final Intent intent = getIntent();

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }


    }


    private void InitializeControllers() {
        user= FirebaseAuth.getInstance().getCurrentUser();
        recyclerView=findViewById(R.id.recyclerViewUserList);
        progressBar=findViewById(R.id.progressBarUserList);
        list=new ArrayList<>();
        handler=new Handler();
        context=getApplicationContext();
        namePhoneMap= new HashMap<String, String>();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Select Contact");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        rootLayout = findViewById(R.id.root_layout);

    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });


            circularReveal.start();
        }
    }
    private void loadSharedPreferenceData() {
        SharedPreferences sharedPreferences=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
        contactsList=sharedPreferences.getStringSet("contactsList",null);

    }

    private void loadUsers(Set<String> contacts){
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        handler.post(new Runnable() {
            @Override
            public void run() {
                userDetails.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List <String> phoneNumbersList=new ArrayList<>();
                        list.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                            userModel userListObj=new userModel();
                            String id=dataSnapshot.getKey();
                            userModel users=dataSnapshot.getValue(userModel.class);
                            String phoneNumber=user.getPhoneNumber();
                            phoneNumbersList.add(user.getPhoneNumber());

                            int i= Collections.frequency(phoneNumbersList,users.getPhoneNumber());
                            if (contacts!=null && !users.getPhoneNumber().equals(phoneNumber)) {
                                if (i <=1 && contacts.contains(phoneNumber)){
                                    userListObj.setUserName(users.getUserName());
                                    userListObj.setPhoneNumber(users.getPhoneNumber());
                                    userListObj.setProfileUrI(users.getProfileUrI());
                                    userListObj.setUserId(id);
                                    list.add(userListObj);
                                    userListAdapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(UserListActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(UserListActivity.this,
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CONTACTS_REQUEST)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && contactsList!=null) {
                loadSharedPreferenceData();
                if(contactsList==null){
                    System.out.println("scscdvf");
                    contactsList=getPhoneNumbers();
                }
                loadUsers(contactsList);

            } else {
                Toast.makeText(getApplicationContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent mainIntent = new Intent(UserListActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.chat_listmenu,menu);
        MenuItem searchItem=menu.findItem(R.id.search);
        MenuItem profileDetails=menu.findItem(R.id.profileButton);
        MenuItem settings=menu.findItem(R.id.settingsButton);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(searchItem);
        profileDetails.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(UserListActivity.this, UserProfileActivity.class);
                startActivity(intent);

                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }else{
                    if (contactsList!=null){
                        loadUsers(contactsList);
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())){
                    searchUsers(newText);
                }else{
                    if (contactsList!=null){
                        loadUsers(contactsList);
                    }
                }
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

    private void searchUsers(String query) {
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        handler.post(new Runnable() {
            @Override
            public void run() {
                userDetails.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List <String> phoneNumbersList=new ArrayList<>();
                        list.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                            userModel userListObj=new userModel();
                            String id=dataSnapshot.getKey();
                            userModel users=dataSnapshot.getValue(userModel.class);
                            String phoneNumber=user.getPhoneNumber();
                            phoneNumbersList.add(user.getPhoneNumber());

                            int i= Collections.frequency(phoneNumbersList,users.getPhoneNumber());
                            if (contactsList!=null && !users.getPhoneNumber().equals(phoneNumber)) {
                                if (i <=1 && contactsList.contains(phoneNumber)){
                                    userListObj.setUserName(users.getUserName());
                                    userListObj.setPhoneNumber(users.getPhoneNumber());
                                    userListObj.setProfileUrI(users.getProfileUrI());
                                    userListObj.setUserId(id);
                                    if(users.getUserName().toLowerCase().contains(query.toLowerCase())){
                                        list.add(userListObj);
                                        userListAdapter.notifyDataSetChanged();
                                    }


                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }
}