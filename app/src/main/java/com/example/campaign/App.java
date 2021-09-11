package com.example.campaign;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleObserver;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class App  extends Application  {
    public static final String MESSAGE_CHANNEL_ID="messageChannel";




    public void onCreate(){
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        createNotificationChannel();
    }




    @RequiresApi(api = Build.VERSION_CODES.O)


    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel messageChannel=new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "MESSAGE_CHANNEL",
                    NotificationManager.IMPORTANCE_HIGH
            );
            messageChannel.setDescription("this is the message channel");
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(messageChannel);
        }
    }




}
