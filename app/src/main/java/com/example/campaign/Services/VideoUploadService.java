package com.example.campaign.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campaign.Common.Tools;
import com.example.campaign.Interfaces.APIService;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Notifications.Client;
import com.example.campaign.Notifications.Data;
import com.example.campaign.Notifications.Sender;
import com.example.campaign.Notifications.Token;
import com.example.campaign.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.campaign.Common.Tools.getMimeType;

public class VideoUploadService extends Service {
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    private boolean notify=false;
    private String fPhoneNumber;
    private String userId;
    private ResultReceiver myResultReceiver;
    private final Bundle bundle = new Bundle();
    private APIService apiService;
    private final String time=new Tools().getTime();
    private final String date=new Tools().getDate();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mStorageReference= firebaseStorage.getReference();
        myResultReceiver =  intent.getParcelableExtra("receiver");
        fPhoneNumber=intent.getStringExtra("fPhoneNumber");
        userId=intent.getStringExtra("userId");
        String otherUserId = intent.getStringExtra("otherUserId");
        String caption =intent.getStringExtra("caption");
        String uriString=intent.getStringExtra("uri");
        Uri uri = Uri.parse(uriString);
        Tools tools=new Tools();
        List<Object> array=tools.encryptMessage(caption);
        uploadVideo(userId, otherUserId, uri,getApplicationContext(),caption);
        return START_NOT_STICKY;
    }

    private void uploadVideo(String userId, String otherUserId, Uri uri, Context context, String caption){

        if(uri!=null){
            updateToken(FirebaseInstanceId.getInstance().getToken());
            apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setTime(time);
            message.setDate(date);
            message.setType("VIDEO");
            message.setText(caption);
            message.setVideoUrI(String.valueOf(uri));
            message.setReceiver(otherUserId);
            fUserChatRef.setValue(message);
            String messageKey=fUserChatRef.getKey();
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis()
                    + getMimeType(context,uri));

            fileReference.putFile(uri).addOnProgressListener(taskSnapshot -> {
                @SuppressWarnings("IntegerDivisionInFloatingPointContext") double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                System.out.println("Upload is " + progress + "% done");
                int currentProgress = (int) progress;
                bundle.putInt("uploadVideoPercentage",currentProgress);
                bundle.putString("uploadVideoTId",messageKey);

                myResultReceiver.send(200,bundle);
            }).addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused")).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setVideoUrI(downloadUri.toString());
                    messageOtherUser.setTime(time);
                    messageOtherUser.setDate(date);
                    messageOtherUser.setType("VIDEO");
                    message.setAudioUrI(uri.toString());
                    messageOtherUser.setReceiver(otherUserId);
                    DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                    messageRef.child(messageKey).setValue(messageOtherUser);
                    notify=true;
                    if(notify){
                        sendNotification(otherUserId,fPhoneNumber);
                    }
                }
            });
        }
    }

    private void updateToken(String token) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 =new Token(token);
        reference.child(userId).setValue(token1);
    }


    private void sendNotification(String otherUserId, String fPhoneNumber) {
        DatabaseReference tokens=database.getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(otherUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Token token=dataSnapshot.getValue(Token.class);
                    Data data=new Data(userId, R.mipmap.ic_launcher2, "VIDEO",fPhoneNumber,otherUserId,"New message");
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender);
                    notify=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




}
