package com.neuralBit.letsTalk.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Interfaces.APIService;
import com.neuralBit.letsTalk.Model.messageListModel;
import com.neuralBit.letsTalk.Notifications.Client;
import com.neuralBit.letsTalk.Notifications.Data;
import com.neuralBit.letsTalk.Notifications.MyResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.neuralBit.letsTalk.Common.Tools.getMimeType;

import java.security.PublicKey;

public class AudioUploadService extends Service {
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    boolean notify=false;
    private Tools tools;
    private String date;
    private String time;
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
        tools=new Tools();
        date= tools.getDate();
        time=tools.getTime();
        mStorageReference= firebaseStorage.getReference().child("messageAudios");
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
            DatabaseReference fLMBranch=database.getReference().child("lastMessage").child(userId).child(otherUserId);
            DatabaseReference otherLMBranch=database.getReference().child("lastMessage").child(otherUserId).child(userId);
            DatabaseReference otherUserRef= database.getReference().child("chats").child(otherUserId).child(userId);
            DatabaseReference fUserChatRef= database.getReference().child("chats").child(userId).child(otherUserId).push();
            messageListModel fUserMessage=new messageListModel();
            fUserMessage.setTime(time);
            fUserMessage.setDate(date);
            fUserMessage.setType("AUDIO");
            fUserMessage.setAudioUrI(String.valueOf(uri));
            fUserMessage.setAudioDuration(duration);


            try {

                fUserMessage.setAudioUrI(tools.encryptText(String.valueOf(uri)));


            } catch (Exception e) {
                e.printStackTrace();
            }
            fUserMessage.setReceiver(otherUserId);
            fUserChatRef.setValue(fUserMessage);
            fUserChatRef.setValue(fUserMessage);
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
                bundle.putInt("uploadImagePercentage",currentProgress);
                bundle.putString("uploadImageTId",messageKey);

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
                    messageOtherUser.setType("AUDIO");
                    messageOtherUser.setAudioDuration(duration);
                    messageOtherUser.setTime(time);
                    messageOtherUser.setDate(date);

                    messageOtherUser.setReceiver(otherUserId);
                    try {
                        messageOtherUser.setAudioUrI(tools.encryptText(downloadUri.toString()));

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
                        Data data=new Data(userId, R.mipmap.small_icon_round, tools.encryptText("AUDIO"),fPhoneNumber,otherUserId,"New message");
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
