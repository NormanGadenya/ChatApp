package com.example.campaign.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campaign.Common.Tools;
import com.example.campaign.Interfaces.APIService;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Notifications.Client;
import com.example.campaign.Notifications.Data;
import com.example.campaign.Notifications.MyResponse;
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
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.campaign.Common.Tools.getMimeType;

public class AudioUploadService extends Service {
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    boolean notify=false;
    private final String date=new Tools().getDate();
    private final String time=new Tools().getTime();
    private String fPhoneNumber;
    private String userId;
    private ResultReceiver myResultReceiver;
    private final Bundle bundle = new Bundle();
    private APIService apiService;

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
        String uriString=intent.getStringExtra("uri");
        String duration=intent.getStringExtra("audioDuration");
        Uri uri = Uri.parse(uriString);
        uploadAudio(userId, otherUserId, uri,getApplicationContext(),duration);
        return START_NOT_STICKY;
    }


    private void uploadAudio(String userId, String otherUserId, Uri uri, Context context, String duration){

        if(uri!=null){
            updateToken(FirebaseInstanceId.getInstance().getToken());
            apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setTime(time);
            message.setDate(date);
            message.setType("AUDIO");
            message.setAudioUrI(String.valueOf(uri));
            message.setAudioDuration(duration);
            message.setReceiver(otherUserId);
            fUserChatRef.setValue(message);
            String messageKey=fUserChatRef.getKey();
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis()
                    + getMimeType(context,uri));
            fileReference.putFile(uri).addOnProgressListener(taskSnapshot -> {
                double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                int currentProgress = (int) progress;
                bundle.putInt("uploadAudioPercentage",currentProgress);
                bundle.putString("uploadAudioTId",messageKey);
                myResultReceiver.send(300,bundle);
            }).addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused")).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setAudioUrI(downloadUri.toString());
                    messageOtherUser.setTime(time);
                    messageOtherUser.setDate(date);
                    messageOtherUser.setType("AUDIO");
                    messageOtherUser.setAudioDuration(duration);
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
                    Data data=new Data(userId, R.mipmap.ic_launcher2, "AUDIO",fPhoneNumber,otherUserId,"New message");
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>(){

                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success==1){
                                        }else{
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    t.fillInStackTrace();
                                }
                            });
                    notify=false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





}
