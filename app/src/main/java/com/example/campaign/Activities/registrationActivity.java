package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.campaign.Model.userModel;

import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;


public class registrationActivity extends AppCompatActivity {
    private Button submit_button;
    private EditText Name;
    private EditText About;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton selProfilePic,gallery_button,camera_button,remove_button;
    private String userId;
    private String phoneNumber;
    private View wrapper;
    private Uri selected;
    private CircularImageView profilePic;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 100;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_activity);
        mStorageReference= FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

        InitializeControllers();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        selProfilePic.setOnClickListener(v -> wrapper.setVisibility(View.VISIBLE));
        submit_button.setOnClickListener(v -> {
            String name = Name.getText().toString();
            String aboutPerson=About.getText().toString();

            if(name.isEmpty() || aboutPerson.isEmpty()){
                Toast.makeText(registrationActivity.this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            }else{
                if (user!=null) {

                    for (UserInfo profileData : user.getProviderData()){
                        userId=profileData.getUid();
                        phoneNumber=profileData.getPhoneNumber();
                        uploadFile(name,aboutPerson,phoneNumber,userId);
                    }
                }
            }
        });
        gallery_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(registrationActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,GALLERY_REQUEST);
            } else {
                requestStoragePermission();
            }
        });
        camera_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(registrationActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,  CAMERA_REQUEST);
            } else {
                requestCameraPermission();
            }

        });
        remove_button.setOnClickListener(v -> {
            selected=null;
            profilePic.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
            wrapper.setVisibility(View.GONE);
        });
    }

    private void InitializeControllers() {
        Name=findViewById(R.id.editTextPersonName);
        About= findViewById(R.id.editTextAboutPerson);
        submit_button=findViewById(R.id.Registration_button);
        selProfilePic=findViewById(R.id.selProfilePic);
        gallery_button=findViewById(R.id.gallery_button);
        remove_button=findViewById(R.id.remove_button);
        camera_button=findViewById(R.id.camera_button);
        wrapper=findViewById(R.id.wrapper_sel_profile);
        profilePic=findViewById(R.id.image_profile);
    }


    private void uploadFile(String name,String aboutPerson,String phoneNumber,String userId) {
        
        if (selected != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + ".jpg");

            fileReference.putFile(selected).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    DatabaseReference myRef = database.getReference();
                    userModel userModel =new userModel();
                    userModel.setUserName(name);
                    userModel.setPhoneNumber(phoneNumber);
                    userModel.setProfileUrI(downloadUri.toString());
                    userModel.setAbout(aboutPerson);
                    try{
                        myRef.child("UserDetails").child(userId).setValue(userModel);

                    }catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                    Intent chatList=new Intent(registrationActivity.this, signUpActivity.class);
                    startActivity(chatList);

                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri getImageUri( Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your storage")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(registrationActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your camera")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(registrationActivity.this,
                            new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_REQUEST)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,GALLERY_REQUEST);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode ==CAMERA_REQUEST){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,  CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            selected=data.getData();
            try{
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),selected);
                profilePic.setImageBitmap(bitmap);
                wrapper.setVisibility(View.GONE);

            }catch(Exception e){
                Log.d("error",e.getMessage());
            }
        }
        else if(requestCode ==CAMERA_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            try{
                Bitmap bitmap=(Bitmap) data.getExtras().get("data");
                selected=getImageUri(bitmap);
                profilePic.setImageBitmap(bitmap);
                wrapper.setVisibility(View.GONE);

            }catch(Exception e){
                Log.d("error",e.getMessage());
            }

        }

    }
}