package com.example.campaign.Activities;

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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.campaign.Common.Tools;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.Services.ProfileUploadService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.zolad.zoominimageview.ZoomInImageView;

import java.io.ByteArrayOutputStream;

import static android.view.View.GONE;
import static com.example.campaign.Common.Tools.CAMERA_REQUEST;
import static com.example.campaign.Common.Tools.CONTACTS_REQUEST;
import static com.example.campaign.Common.Tools.GALLERY_REQUEST;

public class RegistrationActivity extends AppCompatActivity {
    private Button submit_button;
    private EditText userNameTV;
    private FirebaseDatabase database;

    private FloatingActionButton selProfilePic,gallery_button,camera_button,remove_button;
    private String userId;
    private String phoneNumber;
    private CardView wrapper;
    private Uri selected;
    private ProgressBar progressBar;
    private ZoomInImageView profilePic;


    private boolean btnSelected,clickedDone=false;
    private final String date=new Tools().getDate();
    private final String time=new Tools().getTime();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_activity);
        database = FirebaseDatabase.getInstance();

        InitializeControllers();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        selProfilePic.setOnClickListener(v -> {

            btnSelected=true;
            if(wrapper.getVisibility()== GONE){
                wrapper.setVisibility(View.VISIBLE);
                wrapper.setAlpha(0.0f);
                wrapper.animate()
                        .translationY(0)
                        .setDuration(300)
                        .alpha(1.0f)
                        .setListener(null);
                userNameTV.setEnabled(false);

            }else{
                wrapper.setVisibility(GONE);
                wrapper.setAlpha(1.0f);
                wrapper.animate()
                        .translationY(0)
                        .setDuration(300)
                        .alpha(0.0f)
                        .setListener(null);
                userNameTV.setEnabled(true);

            }

        });

        submit_button.setOnClickListener(v -> {
            String userName = userNameTV.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            if(userName.isEmpty()){
                Toast.makeText(RegistrationActivity.this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            }else{
                if (user!=null) {
                    Log.d("done", String.valueOf(clickedDone));
                    if(!clickedDone){

                        userId = user.getUid();
                        phoneNumber = user.getPhoneNumber();

                        updateUserDetails(userName,phoneNumber,userId);

                        if(selected!=null){
                            Intent i= new Intent (getApplicationContext(), ProfileUploadService.class);
                            i.putExtra("userId",userId);
                            i.setData(selected);
                            startService(i);
                        }
                    }

                    if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                            Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        Intent chatList=new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(chatList);
                        userNameTV.setEnabled(false);
                    } else {
                        requestContactsPermission();
                    }

                }
            }
        });
        userNameTV.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            clickedDone=false;
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (user!=null) {
                    progressBar.setVisibility(View.VISIBLE);
                    String userName=userNameTV.getText().toString();
                    userId=user.getUid();
                    phoneNumber=user.getPhoneNumber();

//                            uploadFile(name,phoneNumber,userId);

                    updateUserDetails(userName,phoneNumber,userId);
                    clickedDone=true;
                    if(selected!=null){
                        Intent i= new Intent (getApplicationContext(), ProfileUploadService.class);
                        i.putExtra("userId",userId);
                        i.setData(selected);
                        startService(i);

                    }
                    if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                            Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        Intent chatList=new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(chatList);
                        userNameTV.setEnabled(false);
                    } else {
                        requestContactsPermission();
                    }
//                        progressBar.setVisibility(View.GONE);

                }

                return true;
            }
            return false;
        });

        gallery_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,GALLERY_REQUEST);
                    userNameTV.setEnabled(false);
            } else {
                requestStoragePermission();
            }
        });
        camera_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,  CAMERA_REQUEST);
                userNameTV.setEnabled(false);
            } else {
                requestCameraPermission();
            }

        });
        remove_button.setOnClickListener(v -> {
            selected=null;
            profilePic.setImageResource(R.drawable.person);
            wrapper.setVisibility(GONE);
            userNameTV.setEnabled(true);
        });

    }

    private void updateUserDetails(String userName, String phoneNumber, String userId) {
            userModel userModel =new userModel();
            userModel.setUserName(userName);
            userModel.setPhoneNumber(phoneNumber);
            userModel.setTyping(false);
            userModel.setLastSeenDate(date);
            userModel.setLastSeenTime(time);
            userModel.setOnline(true);
            userModel.setShowLastSeen(true);
            userModel.setShowOnlineState(true);
            DatabaseReference myRef = database.getReference();
            myRef.child("UserDetails").child(userId).setValue(userModel);
    }

    private void InitializeControllers() {
        userNameTV=findViewById(R.id.editTextPersonName);
        submit_button=findViewById(R.id.RegistrationButton);
        selProfilePic=findViewById(R.id.selProfilePic);
        gallery_button=findViewById(R.id.gallery_button);
        remove_button=findViewById(R.id.remove_button);
        camera_button=findViewById(R.id.camera_button);
        wrapper=findViewById(R.id.layout_actions);
        profilePic=findViewById(R.id.image_profile);
        progressBar=findViewById(R.id.progressBar1);
        profilePic.setClipToOutline(true);

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
    private void requestContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your contacts")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(RegistrationActivity.this,
                            new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
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
            wrapper.setVisibility(GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CONTACTS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent chatList = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(chatList);
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
                wrapper.setVisibility(GONE);
                userNameTV.setEnabled(true);

            }catch(Exception e){
                Log.d("error",e.getMessage());
                userNameTV.setEnabled(true);

            }
        }
        else if(requestCode ==CAMERA_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            try{
                Bitmap bitmap=(Bitmap) data.getExtras().get("data");

                selected=getImageUri(bitmap);
                profilePic.setImageBitmap(bitmap);
                wrapper.setVisibility(GONE);
                userNameTV.setEnabled(true);

            }catch(Exception e){
                Log.d("error",e.getMessage());

            }

        }
        else{
            userNameTV.setEnabled(true);
        }

    }

    @Override
    public void onBackPressed() {

     wrapper.setVisibility(GONE);
     userNameTV.setEnabled(true);
    }

}