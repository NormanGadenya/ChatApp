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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;
import java.util.Map;

public class userListActivity extends AppCompatActivity {
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
    private List<String> contactsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        InitializeControllers();
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        database = FirebaseDatabase.getInstance();
        userListAdapter=new userListAdapter(list,userListActivity.this);

        recyclerView.setAdapter(userListAdapter);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    contactsList=getPhoneNumbers();
                    loadUsers(contactsList);
                }
            });

        } else {
            requestContactsPermission();
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
        contactsList=new ArrayList<>();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Select Contact");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

    }

    private void loadUsers(List<String> contacts){
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
    private List<String> getPhoneNumbers() {
        List<String> phoneNumbers=new ArrayList<>();
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(userListActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(userListActivity.this,
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CONTACTS_REQUEST)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && contactsList!=null) {
                contactsList=getPhoneNumbers();
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
        Intent mainIntent = new Intent(userListActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
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