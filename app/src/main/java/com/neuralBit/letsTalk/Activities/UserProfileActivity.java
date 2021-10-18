package com.neuralBit.letsTalk.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import androidx.appcompat.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.neuralBit.letsTalk.Common.ServiceCheck;
import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.UserViewModel;
import com.example.campaign.R;
import com.neuralBit.letsTalk.Services.ProfileUploadService;
import com.neuralBit.letsTalk.Services.updateStatusService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static com.neuralBit.letsTalk.Common.Tools.GALLERY_REQUEST;

@SuppressWarnings("ALL")
public class UserProfileActivity extends AppCompatActivity {
    private final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private  Toolbar toolbar;
    private TextView userName,phoneNumber;
    private FloatingActionButton editProfilePic;
    private ImageButton editUserNameBtn;
    private EditText editUserName;
    private String profileUrI;
    private String editedUserName;
    private ImageView imageView;
    private Button doneButton;
    private ProgressBar progressBar;
    private StorageReference mStorageReference;
    private Uri selected;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        InitializeControllers();
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.initFUserInfo();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck= new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
        editUserNameBtn.setOnClickListener(v -> {
            userName.setVisibility(View.GONE);
            editUserNameBtn.setVisibility(View.GONE);
            editUserName.setVisibility(View.VISIBLE);

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

        editUserName.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                userName.setVisibility(View.VISIBLE);
                editUserNameBtn.setVisibility(View.VISIBLE);
                editedUserName=editUserName.getText().toString();
                userName.setText(editedUserName);
                editUserName.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editUserName.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        doneButton.setOnClickListener(v -> {
            updateUserDetails(firebaseUser.getUid());
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
        mStorageReference= FirebaseStorage.getInstance().getReference();
        editProfilePic=findViewById(R.id.editProfilePic);
        database=FirebaseDatabase.getInstance();
        imageView=findViewById(R.id.userProfilePic);
    }

    private void setupToolBar(){

        userViewModel.getFUserInfo().observe(this,user->{
            profileUrI=user.getProfileUrI();
            setSupportActionBar(toolbar);
            phoneNumber.setText(user.getPhoneNumber());
            userName.setText(user.getUserName());
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
                    if(profileUrI!=null){
                        firebaseStorage.getReferenceFromUrl(profileUrI).delete().addOnSuccessListener(aVoid -> {
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
                        });

                    }else{

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

    private void updateUserDetails( String userId) {
        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
        Map<String ,Object> userDetail=new HashMap<>();
        if (selected != null) {
            if(editedUserName!=null){
                userDetail.put("userName",editedUserName);
                myRef.updateChildren(userDetail);
            }
            Intent i= new Intent (getApplicationContext(), ProfileUploadService.class);
            i.putExtra("userId",userId);
            i.setData(selected);
            startService(i);

        }else{
            userDetail.put("userName",editedUserName);
            myRef.updateChildren(userDetail);
        }

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
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            selected=data.getData();
            userViewModel.setSelectedUri(selected);

            userViewModel.getSelectedUri().observe(this,selectedUri->{
                try{
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),selectedUri);
                    imageView.setImageBitmap(bitmap);
                    Palette.from(bitmap).generate(palette -> {
                        if(palette!=null){
                            Palette.Swatch vibrantSwatch = palette.getMutedSwatch();
                            if(vibrantSwatch != null){
                                getResources().getDrawable(R.drawable.title_background).setTint(vibrantSwatch.getRgb());
                            }
                        }
                    });


                }catch(Exception e){
                    Log.d("error",e.getMessage());
                }
            });

        }

    }
}