package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class RegistrationActivity extends AppCompatActivity {
    private Button submit_button;
    private EditText Name;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton selProfilePic,gallery_button,camera_button,remove_button;
    private String userId;
    private String phoneNumber;
    private CardView wrapper;
    private Uri selected;
    private ProgressBar progressBar;
    private CircularImageView profilePic;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 100;
    private StorageReference mStorageReference;
    private boolean btnSelected=false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_activity);
        mStorageReference= FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

        InitializeControllers();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        selProfilePic.setOnClickListener(v -> {
            wrapper.setVisibility(View.VISIBLE);
            Name.setEnabled(false);
            btnSelected=true;
        });
        submit_button.setOnClickListener(v -> {
            String name = Name.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            if(name.isEmpty()){
                Toast.makeText(RegistrationActivity.this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            }else{
                if (user!=null) {

                    for (UserInfo profileData : user.getProviderData()){
                        userId=profileData.getUid();
                        phoneNumber=profileData.getPhoneNumber();
                        uploadFile(name,phoneNumber,userId);
                    }
                }
            }
        });
        Name.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    if (user!=null) {
                        progressBar.setVisibility(View.VISIBLE);
                        for (UserInfo profileData : user.getProviderData()){
                            String name=Name.getText().toString();
                            userId=profileData.getUid();
                            phoneNumber=profileData.getPhoneNumber();
                            uploadFile(name,phoneNumber,userId);

                        }
                    }

                    return true;
                }
                return false;
            }
        });

        gallery_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,GALLERY_REQUEST);
                    Name.setEnabled(false);
            } else {
                requestStoragePermission();
            }
        });
        camera_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,  CAMERA_REQUEST);
                Name.setEnabled(false);
            } else {
                requestCameraPermission();
            }

        });
        remove_button.setOnClickListener(v -> {
            selected=null;
            profilePic.setImageResource(R.drawable.person);
            wrapper.setVisibility(View.GONE);
        });

    }

    private void InitializeControllers() {
        Name=findViewById(R.id.editTextPersonName);
        submit_button=findViewById(R.id.RegistrationButton);
        selProfilePic=findViewById(R.id.selProfilePic);
        gallery_button=findViewById(R.id.gallery_button);
        remove_button=findViewById(R.id.remove_button);
        camera_button=findViewById(R.id.camera_button);
        wrapper=findViewById(R.id.layout_actions);
        profilePic=findViewById(R.id.image_profile);
        progressBar=findViewById(R.id.progressBar1);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile(String name, String phoneNumber, String userId) {
        
        if (selected != null) {
            Name.setEnabled(true);
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
                    userModel.setLastSeenDate(getDate());
                    userModel.setLastSeenTime(getTime());
                    userModel.setOnline(true);
                    userModel.setShowLastSeen(true);
                    userModel.setShowOnlineState(true);
                    userModel.setPhoneNumber(phoneNumber);
                    userModel.setProfileUrI(downloadUri.toString());


                    try{
                        myRef.child("UserDetails").child(userId).setValue(userModel);

                    }catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                    Intent chatList=new Intent(RegistrationActivity.this, SignUpActivity.class);
                    startActivity(chatList);

                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            userModel userModel =new userModel();
            userModel.setUserName(name);
            userModel.setPhoneNumber(phoneNumber);
            try{
                DatabaseReference myRef = database.getReference();
                myRef.child("UserDetails").child(userId).setValue(userModel);

            }catch(Exception e){
                System.out.println(e.getLocalizedMessage());
            }

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
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(RegistrationActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        }
    }
    private void closeKeyboard(){
        View view=this.getCurrentFocus();
        if (view!=null){
            InputMethodManager im=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your camera")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(RegistrationActivity.this,
                            new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (wrapper.isShown() && !btnSelected){
            wrapper.setVisibility(View.GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
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
                Name.setEnabled(true);

            }catch(Exception e){
                Log.d("error",e.getMessage());
                Name.setEnabled(true);

            }
        }
        else if(requestCode ==CAMERA_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            try{
                Bitmap bitmap=(Bitmap) data.getExtras().get("data");

                selected=getImageUri(bitmap);
                profilePic.setImageBitmap(bitmap);
                wrapper.setVisibility(View.GONE);
                Name.setEnabled(true);

            }catch(Exception e){
                Log.d("error",e.getMessage());
                Name.setEnabled(true);
            }

        }
        else{
            Name.setEnabled(true);
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

    @Override
    public void onBackPressed() {

     wrapper.setVisibility(View.GONE);
    }
}