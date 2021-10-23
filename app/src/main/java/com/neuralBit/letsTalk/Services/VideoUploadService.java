package com.neuralBit.letsTalk.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Interfaces.APIService;
import com.neuralBit.letsTalk.Model.messageListModel;
import com.neuralBit.letsTalk.Notifications.Client;
import com.neuralBit.letsTalk.Notifications.Data;
import com.neuralBit.letsTalk.Notifications.Sender;
import com.neuralBit.letsTalk.Notifications.Token;
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

import static com.neuralBit.letsTalk.Common.Tools.getMimeType;

public class VideoUploadService extends Service {
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    private boolean notify=false;
    private String fPhoneNumber;
    private String userId;
    private ResultReceiver myResultReceiver;
    private final Bundle bundle = new Bundle();
    private APIService apiService;
    private Tools tools;
    private String date;
    private String time;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance();
        tools=new Tools();
        date= tools.getDate();
        time=tools.getTime();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mStorageReference= firebaseStorage.getReference().child("messageVideos");
        myResultReceiver =  intent.getParcelableExtra("receiver");
        fPhoneNumber=intent.getStringExtra("fPhoneNumber");
        userId=intent.getStringExtra("userId");
        String otherUserId = intent.getStringExtra("otherUserId");
        String caption =intent.getStringExtra("caption");

        String uriString=intent.getStringExtra("uri");
        Uri uri = Uri.parse(uriString);
        uploadVideo(userId, otherUserId, uri,getApplicationContext(),caption);
        return START_NOT_STICKY;
    }

    private void uploadVideo(String userId, String otherUserId, Uri uri, Context context, String caption){

        if(uri!=null){

            updateToken(FirebaseInstanceId.getInstance().getToken());
            apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);
            DatabaseReference fLMBranch=database.getReference().child("lastMessage").child(userId).child(otherUserId);
            DatabaseReference otherLMBranch=database.getReference().child("lastMessage").child(otherUserId).child(userId);

            DatabaseReference otherUserRef= database.getReference().child("chats").child(otherUserId).child(userId);
            DatabaseReference fUserChatRef= database.getReference().child("chats").child(userId).child(otherUserId).push();
            messageListModel fUserMessage=new messageListModel();
            fUserMessage.setTime(time);
            fUserMessage.setDate(date);
            fUserMessage.setType("VIDEO");
            fUserMessage.setText(caption);
            fUserMessage.setVideoUrI(String.valueOf(uri));
            try {
                fUserMessage.setVideoUrI(tools.encryptText(String.valueOf(uri)));
                if(caption!=null){
                    fUserMessage.setText(tools.encryptText(caption));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            fUserMessage.setReceiver(otherUserId);
            fUserChatRef.setValue(fUserMessage);
            fLMBranch.setValue(fUserMessage);
            String messageKey=fUserChatRef.getKey();
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis()
                    + getMimeType(context,uri));

            fileReference.putFile(uri).addOnProgressListener(taskSnapshot -> {

                @SuppressWarnings("IntegerDivisionInFloatingPointContext") double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                int currentProgress = (int) progress;
                currentProgress=(int)((float)(-0.25*currentProgress)+25);
                if(currentProgress==0){
                    currentProgress=1;
                }
                bundle.putInt("uploadVideoPercentage",currentProgress);
                bundle.putString("uploadVideoTId",messageKey);

                myResultReceiver.send(100,bundle);
            }).addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused")).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    try {
                        messageOtherUser.setVideoUrI(tools.encryptText(downloadUri.toString()));
                    } catch (Exception e) {
                        messageOtherUser.setVideoUrI(downloadUri.toString());
                        e.printStackTrace();
                    }
                    messageOtherUser.setTime(time);
                    messageOtherUser.setDate(date);
                    messageOtherUser.setType("VIDEO");
                    try {
                        messageOtherUser.setText(tools.encryptText(caption));
                    } catch (Exception e) {
                        messageOtherUser.setText(caption);
                        e.printStackTrace();
                    }
                    messageOtherUser.setReceiver(otherUserId);
                    try {
                        if (caption != null) {

                            messageOtherUser.setText(tools.encryptText(caption));

                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                    otherUserRef.child(messageKey).setValue(messageOtherUser);
                    otherLMBranch.setValue(messageOtherUser);
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
                    try{
                        Data data=new Data(userId, R.mipmap.ic_launcher2, tools.encryptText("VIDEO"),fPhoneNumber,otherUserId,"New message");
                        Sender sender = new Sender(data,token.getToken());
                        apiService.sendNotification(sender);
                        notify=false;
                    }catch(Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




}
