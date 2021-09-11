package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.example.campaign.Common.ServiceCheck;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.R;
import com.example.campaign.Services.ProfileUploadService;
import com.example.campaign.Services.updateStatusService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.example.campaign.Activities.ViewImageActivity.PERMISSION_WRITE;

public class OtherUserActivity extends AppCompatActivity {

    private  Toolbar toolbar;
    private TextView userName,phoneNumber;
    private String profileUrI;
    private String otherUserId;
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
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck=new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
        otherUserId=getIntent().getStringExtra("otherUserId");
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
            try {
                profileBitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(profileUrI));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setSupportActionBar(toolbar);
            phoneNumber.setText(user.getPhoneNumber());
            userName.setText(user.getUserName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Glide.with(getApplicationContext()).load(profileUrI).into(imageView);
        });

    }

    public boolean checkPermission() throws IOException {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    private String saveImage(Bitmap image) {

        String savedImagePath = null;
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
        return savedImagePath;
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
        if (requestCode==PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            saveImage(imageUrI);
        }
    }


}