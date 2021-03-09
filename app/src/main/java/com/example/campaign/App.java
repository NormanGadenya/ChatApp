package com.example.campaign;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.campaign.Activities.userListActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App extends Application {
    public static final String MESSAGE_CHANNEL_ID="messageChannel";


    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        createNotificationChannel();
    }

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
