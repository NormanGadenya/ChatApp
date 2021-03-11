package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private  Toolbar toolbar;
    private TextView userName,phoneNumber;
    private FloatingActionButton editProfilePic;
    private ImageButton editUserNameBtn;
    private EditText editUserName;
    String profileUrI;
    private String editedUserName;
    private ImageView imageView;
    private Button doneButton;
    private ProgressBar progressBar;
    private StorageReference mStorageReference;
    private Uri selected;
    private static final int GALLERY_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        InitializeControllers();


        editUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserName.setVisibility(View.VISIBLE);
            }
        });
        editProfilePic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,GALLERY_REQUEST);
            } else {
                requestStoragePermission();
            }
        });

        editUserName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    editedUserName=editUserName.getText().toString();
                    userName.setText(editedUserName);
                    editUserName.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editUserName.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(firebaseUser.getUid());
            }
        });

        setupToolBar();



    }



    private void InitializeControllers() {
        toolbar=findViewById(R.id.toolbar);
        userName=findViewById(R.id.userName);
        editUserNameBtn=findViewById(R.id.editUserNameButton);
        phoneNumber=findViewById(R.id.phoneNumber);
        progressBar=findViewById(R.id.progressBar3);
        editUserName=findViewById(R.id.editUserName);
        doneButton=findViewById(R.id.done);
        firebaseStorage=FirebaseStorage.getInstance();
        mStorageReference= firebaseStorage.getInstance().getReference();
        editProfilePic=findViewById(R.id.editProfilePic);
        database=FirebaseDatabase.getInstance();
        imageView=findViewById(R.id.userProfilePic);
    }
    private void setupToolBar(){
        DatabaseReference userDetailsRef=database.getReference().child("UserDetails").child(firebaseUser.getUid());
        userDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel user=snapshot.getValue(userModel.class);
                profileUrI=user.getProfileUrI();
                setSupportActionBar(toolbar);
                phoneNumber.setText(user.getPhoneNumber());
                userName.setText(user.getUserName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                Glide.with(getApplicationContext()).load(profileUrI).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your storage")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(UserProfileActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        }
    }

    private void uploadFile( String userId) {

        if (selected != null) {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + ".jpg");

            fileReference.putFile(selected).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    firebaseStorage.getReferenceFromUrl(profileUrI).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Uri downloadUri = task.getResult();
                            DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
                            Map<String ,Object> userDetail=new HashMap<>();
                            if(editedUserName!=null){
                                userDetail.put("userName",editedUserName);
                            }
                            try{
                                userDetail.put("profileUrI",downloadUri.toString());
                                myRef.updateChildren(userDetail);
                            }catch (Exception e){
                                Log.e("Error",e.getLocalizedMessage());
                            }
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });
        } else {

            DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
            Map<String ,Object> userDetail=new HashMap<>();
            if(editedUserName!=null){
                userDetail.put("userName",editedUserName);
                myRef.updateChildren(userDetail);
            }
            Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            selected=data.getData();
            try{
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),selected);
                imageView.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        if(palette!=null){
                            Palette.Swatch vibrantSwatch = palette.getMutedSwatch();
                            if(vibrantSwatch != null){

                            }
                        }
                    }
                });


            }catch(Exception e){
                Log.d("error",e.getMessage());


            }
        }

    }
}