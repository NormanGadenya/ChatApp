package com.example.campaign.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

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

public class VideoUploadService extends Service {
    private FirebaseDatabase database;
    private FirebaseStorage storage,firebaseStorage;
    private StorageReference mStorageReference;
    boolean notify=false;
    private static final String FORMAT = "%02d:%02d";
    String fPhoneNumber,userId,otherUserId;
    ResultReceiver myResultReceiver;
    Bundle bundle = new Bundle();
    Uri uri;
    APIService apiService;
    private String time=new Tools().getTime();
    private String date=new Tools().getDate();

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
        mStorageReference=firebaseStorage.getReference();
        myResultReceiver =  intent.getParcelableExtra("receiver");
        fPhoneNumber=intent.getStringExtra("fPhoneNumber");
        userId=intent.getStringExtra("userId");
        otherUserId=intent.getStringExtra("otherUserId");
        String caption =intent.getStringExtra("caption");
        String uriString=intent.getStringExtra("uri");
        uri=Uri.parse(uriString);
        uploadVideo(userId,otherUserId,uri,getApplicationContext(),caption);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void uploadVideo(String userId, String otherUserId, Uri uri, Context context,String caption){

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

            fileReference.putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    System.out.println("Upload is " + progress + "% done");
                    int currentProgress = (int) progress;
                    bundle.putInt("uploadVideoPercentage",currentProgress);
                    bundle.putString("uploadVideoTId",messageKey);

                    myResultReceiver.send(200,bundle);
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).continueWithTask(task -> {
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
                        sendNotification(otherUserId,fPhoneNumber,"VIDEO");
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

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String otherUserId, String fPhoneNumber, String message) {
        DatabaseReference tokens=database.getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(otherUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Token token=dataSnapshot.getValue(Token.class);
                    Data data=new Data(userId, R.mipmap.ic_launcher2,message,fPhoneNumber,otherUserId,"New message");
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
