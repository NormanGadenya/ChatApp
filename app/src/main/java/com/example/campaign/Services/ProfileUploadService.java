package com.example.campaign.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.campaign.Model.userModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;



public class ProfileUploadService extends Service {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();;
    private StorageReference mStorageReference= FirebaseStorage.getInstance().getReference();;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile( String userId,Uri selected) {

        if (selected != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
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
                        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
                        Map<String ,Object> profileUrI=new HashMap<>();
                        profileUrI.put("profileUrI",downloadUri.toString());
                        myRef.updateChildren(profileUrI);

                    }catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }

                }
            });

        }

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
