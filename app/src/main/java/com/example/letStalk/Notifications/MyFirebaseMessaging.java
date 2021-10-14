package com.example.letStalk.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.letStalk.Activities.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private SharedPreferences contactsSharedPrefs;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sender=remoteMessage.getData().get("sender");
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(fUser != null && sender.equals(fUser.getUid())){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                sendOreoNotification(remoteMessage);
            }else{
                sendNotification(remoteMessage);

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String message=remoteMessage.getData().get("message");
        String phoneNumber=remoteMessage.getData().get("phoneNumber");
        loadSharedPreferenceData();
        String userName=contactsSharedPrefs.getString(phoneNumber,null);
        String body;
        if(userName!=null){
            body = userName + ":"+ message ;
        }else{
            body =phoneNumber + ":" +message;
        }
        int j=Integer.parseInt(user.replaceAll("[\\D]", "")); // generates the notification id's
        Intent intent=new Intent(this, ChatActivity.class);
        intent.putExtra("userId",user);
        intent.putExtra("userName",userName);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri RingingSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoNotification oreoNotification=new OreoNotification(this);
        Notification.Builder builder= oreoNotification.getOreoNotification(title,body,pendingIntent,RingingSound,icon);
        int i=0;
        if(j>0){
            i=j;
        }
        oreoNotification.getNotificationManager().notify(i,builder.build());
    }

    private void loadSharedPreferenceData() {
        contactsSharedPrefs=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
    }
    private void sendNotification(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String message=remoteMessage.getData().get("message");
        String phoneNumber=remoteMessage.getData().get("phoneNumber");
        loadSharedPreferenceData();
        String userName=contactsSharedPrefs.getString(phoneNumber,null);
        String body;
        if(userName!=null){
            body = userName + ":"+ message ;
        }else{
            body =phoneNumber + ":" +message;
        }
        int j=Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent=new Intent(this, ChatActivity.class);
        intent.putExtra("userId",user);
        intent.putExtra("userName",userName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri RingingSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(RingingSound)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int i=0;
        if(j>0){
            i=j;
        }

        notificationManager.notify(i,builder.build());
    }
}
