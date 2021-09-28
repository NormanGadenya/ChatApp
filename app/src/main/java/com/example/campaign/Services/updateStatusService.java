package com.example.campaign.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.campaign.Common.Tools;
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
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        updateStatus();
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(user.getUid());
        Map<String ,Object> lastSeenStatus=new HashMap<>();
        lastSeenStatus.put("lastSeenDate",date);
        lastSeenStatus.put("lastSeenTime",time);
        lastSeenStatus.put("online",false);
        userDetailRef.updateChildren(lastSeenStatus);
        Toast.makeText(this,"Service done",Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service started",Toast.LENGTH_SHORT).show();
        updateStatus();
        return START_STICKY;
    }

    public void updateStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
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
