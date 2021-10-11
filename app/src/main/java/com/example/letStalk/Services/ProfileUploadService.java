package com.example.letStalk.Services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.letStalk.Common.Tools;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static com.example.letStalk.Common.Tools.getMimeType;


public class ProfileUploadService extends Service {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final StorageReference mStorageReference= FirebaseStorage.getInstance().getReference();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String userId=intent.getStringExtra("userId");
        Uri selected=intent.getData();
        uploadFile(userId,selected);
        return START_NOT_STICKY;
    }

    private void uploadFile( String userId,Uri selected) {

        if (selected != null) {
            StorageReference fileReference = mStorageReference.child("profilePic").child(System.currentTimeMillis()
                    + getMimeType(getApplicationContext(),selected));

            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();

                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {


                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();

                    try{
                        Tools tools = new Tools();
                        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
                        Map<String ,Object> profileUrI=new HashMap<>();
                        profileUrI.put("profileUrI",tools.encryptText(downloadUri.toString()));
                        myRef.updateChildren(profileUrI);

                    }catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }

                }
            });

        }

    }


}
