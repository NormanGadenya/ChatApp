package com.neuralBit.letsTalk.Activities;


import static android.view.View.GONE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.campaign.R;
import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OtherUserActivity extends AppCompatActivity {

    private  Toolbar toolbar;
    private TextView userName,phoneNumber;
    private String profileUrI;
    private String otherUserId;
    private String otherUserName;
    private Bitmap profileBitmap;
    private ImageView imageView;
    private FloatingActionButton saveProfilePicBtn;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);
        InitializeControllers();
        try {
            checkPermission();
        } catch (IOException e) {
            e.printStackTrace();
        }
        otherUserId=getIntent().getStringExtra("otherUserId");
        otherUserName=getIntent().getStringExtra("otherUserName");
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.initOtherUserInfo(otherUserId);
        setupToolBar();
        saveProfilePicBtn.setOnClickListener(I->{
            if(profileUrI!=null){
                saveImage(profileBitmap);
            }
        });
    }

    private void InitializeControllers() {
        toolbar=findViewById(R.id.toolbar);
        userName=findViewById(R.id.userName);
        phoneNumber=findViewById(R.id.phoneNumber);
        saveProfilePicBtn=findViewById(R.id.saveImage);
        imageView=findViewById(R.id.userProfilePic);
    }

    private void setupToolBar(){
        userViewModel.getOtherUserInfo().observe(this,user->{
            profileUrI=user.getProfileUrI();
            if(profileUrI!=null){
                try {
                    profileBitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(profileUrI));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                saveProfilePicBtn.setVisibility(GONE);
            }

            setSupportActionBar(toolbar);
            phoneNumber.setText(user.getPhoneNumber());
            if(otherUserId!=null) {
                userName.setText(otherUserName);

            }else{
                userName.setText(user.getUserName());
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Tools tools = new Tools();
            try {
                if(profileUrI!=null){
                    Glide.with(getApplicationContext()).load(tools.decryptText(profileUrI)).into(imageView);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public boolean checkPermission() throws IOException {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ViewImageActivity.PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    private void saveImage(Bitmap image) {

        String savedImagePath;
        String imageFileName = "JPEG_" + "FILE_NAME" + ".jpg";
        File storageDir = new File(            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/Lets Talk");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath);
            Toast.makeText(getApplicationContext(), "IMAGE SAVED", Toast.LENGTH_LONG).show();
        }
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode== ViewImageActivity.PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage(profileBitmap);
        }
    }


}