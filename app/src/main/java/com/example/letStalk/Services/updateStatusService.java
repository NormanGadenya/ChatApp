package com.example.letStalk.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.letStalk.Common.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class updateStatusService extends Service {
    private final Tools tools=new Tools();
    private final String date= tools.getDate();
    private final String time= tools.getTime();
    private ResultReceiver myResultReceiver;
    private final Bundle bundle = new Bundle();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service killed", Toast.LENGTH_SHORT).show();
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(user.getUid());
            Map<String ,Object> lastSeenStatus=new HashMap<>();
            lastSeenStatus.put("lastSeenDate",date);
            lastSeenStatus.put("lastSeenTime",time);
            lastSeenStatus.put("online",false);
            userDetailRef.updateChildren(lastSeenStatus);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        updateStatus();
        return START_STICKY;
    }

    public void updateStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(user.getUid());
            Map<String ,Object> onlineStatus=new HashMap<>();
            onlineStatus.put("online",true);
            userDetailRef.updateChildren(onlineStatus);
            Map<String ,Object> lastSeenStatus=new HashMap<>();
            lastSeenStatus.put("lastSeenDate",date);
            lastSeenStatus.put("lastSeenTime",time);
            lastSeenStatus.put("online",false);
            userDetailRef.onDisconnect().updateChildren(lastSeenStatus);

        }

    }

}
