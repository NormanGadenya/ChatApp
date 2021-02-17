package com.example.campaign;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

import com.example.campaign.Model.Users;
import com.example.campaign.Model.chatsListModel;
import com.example.campaign.adapter.userListAdapter;
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

public class UsersActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    public List<Users> list=new ArrayList<>();
    public List<String> contacts;
    public  Map<String, String> namePhoneMap = new HashMap<String, String>();
    public FirebaseDatabase database;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        recyclerView=findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
        database = FirebaseDatabase.getInstance();
        contacts=new ArrayList<>();
        getUsers();
        back= findViewById(R.id.back);

        contacts=getPhoneNumbers();
        back.setOnClickListener(view ->{
            Intent chatListAct=new Intent(this,chatListActivity.class);
            startActivity(chatListAct);
        });

    }

    private void getUsers(){
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List <String> phoneNumbersList=new ArrayList<>();
                list.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String id=dataSnapshot.getKey();
                    chatsListModel user=dataSnapshot.getValue(chatsListModel.class);
                    String phoneNumber=user.getPhoneNumber();
                    phoneNumbersList.add(phoneNumber);
                    int i= Collections.frequency(phoneNumbersList,phoneNumber);
                    if (i <=1 && contacts.contains(phoneNumber)){
                        Users user_1=new Users();
                        user_1.setName(user.getName());
                        user_1.setPhoneNumber(user.getPhoneNumber());
                        user_1.setProfileUrl(user.getProfileUrI());
                        user_1.setUserId(id);
                        list.add(user_1);
                    }
                }
                recyclerView.setAdapter(new userListAdapter(list,UsersActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            namePhoneMap.put(phoneNumber, name);

        }

        // Get The Contents of Hash Map in Log
        for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
            String key = entry.getKey();
            Log.d("tag", "Phone :" + key);
            String value = entry.getValue();
//            Log.d("tag", "Name :" + value);
            if (key.contains("+")){
                phoneNumbers.add(key);
            }else{
                if(isAlphanumeric2(key)){
                    Long i=Long.parseLong(key);
                    Log.d("tagdscs", String.valueOf(i));
                    String j="+256"+i;
                    phoneNumbers.add(j);
                }
            }
        }
        phones.close();
        return phoneNumbers;
    }
}