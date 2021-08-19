package com.example.campaign.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioService extends Service {
    private FirebaseDatabase database;
    private FirebaseStorage storage,firebaseStorage;
    private StorageReference mStorageReference;
    boolean notify=false;
    String fUserName,userId,otherUserId;
    Uri uri;
    APIService apiService;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance();
        firebaseStorage= FirebaseStorage.getInstance();
        storage= FirebaseStorage.getInstance();
        fUserName=intent.getStringExtra("fUserName");
        userId=intent.getStringExtra("userId");
        otherUserId=intent.getStringExtra("otherUserId");
        String uriString=intent.getStringExtra("uri");
        uri=Uri.parse(uriString);
        uploadAudio(userId,otherUserId,uri,getApplicationContext());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void uploadAudio(String userId, String otherUserId, Uri uri, Context context){
        if(uri!=null){
            updateToken(FirebaseInstanceId.getInstance().getToken());
            apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType("AUDIO");
            message.setReceiver(otherUserId);
            String messageKey=fUserChatRef.getKey();

            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis()
                    + getMimeType(context,uri));
            UploadTask uploadTask =fileReference.putFile(uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setAudioUrI(downloadUri.toString());
                    messageOtherUser.setTime(getTime());
                    messageOtherUser.setDate(getDate());
                    messageOtherUser.setType("AUDIO");
                    message.setAudioUrI(downloadUri.toString());
                    messageOtherUser.setReceiver(otherUserId);
                    DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                    fUserChatRef.setValue(message);
                    messageRef.child(messageKey).setValue(messageOtherUser);

                    notify=true;
                    if(notify){
                        sendNotification(otherUserId,fUserName,"AUDIO");
                    }
                }
            });
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter timeObj = DateTimeFormatter.ofPattern("HH:mm");
        return myDateObj.format(timeObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(dateObj);
    }

    private void updateToken(String token) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 =new Token(token);
        reference.child(userId).setValue(token1);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String otherUserId, String otherUserName, String message) {
        DatabaseReference tokens=database.getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(otherUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Token token=dataSnapshot.getValue(Token.class);
                    Data data=new Data(userId, R.mipmap.ic_launcher2,otherUserName+ ":" +message,otherUserId,"New message");
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>(){

                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success==1){
                                            showToast("failed");
                                        }else{
                                            showToast("achieved");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    showToast("failed2");
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



    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

}
