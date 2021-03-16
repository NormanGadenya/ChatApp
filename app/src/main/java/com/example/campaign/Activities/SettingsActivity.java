package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.adapter.messageListAdapter;
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
import com.google.firebase.storage.UploadTask;
import com.jgabrielfreitas.core.BlurImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SettingsActivity extends AppCompatActivity implements RecyclerViewInterface {
    Toolbar toolbar;
    RecyclerView recyclerView;
    private messageListAdapter messageListAdapter;
    private static final int GALLERY_REQUEST = 100;
    private List<messageListModel> messageList = new ArrayList<>();
    private Uri selected;
    private BlurImageView imageView;
    private SeekBar seekBar;
    private Button applyButton;
    private ProgressBar progressBar;
    int seekBarProgress=1;
    boolean changed=false;
    private String chatWallpaperUrI;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageReference;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    private FloatingActionButton editWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar=findViewById(R.id.toolbar);
        applyButton=findViewById(R.id.done);
        imageView=findViewById(R.id.imageView);
        imageView.setClipToOutline(true);
        imageView.setBackgroundResource(R.drawable.card_background3);
        seekBar=findViewById(R.id.seekBar);
        progressBar=findViewById(R.id.progressBarChatWallpaper);
        firebaseStorage=FirebaseStorage.getInstance();
        mStorageReference=firebaseStorage.getReference();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getCurrentWallpaper();
        getOpacity();
        recyclerView=findViewById(R.id.recycler_view_wall);
        editWallpaper=findViewById(R.id.editWallpaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String profileUrI="";
        messageListAdapter=new messageListAdapter(messageList, getApplicationContext(), profileUrI,this);
        recyclerView.setAdapter(messageListAdapter);
        messageList.add(new messageListModel(" Hi","123","","02:00","","","TEXT","",""));
        messageList.add(new messageListModel(" Hey",firebaseUser.getUid(),"","02:00","","","TEXT","",""));
        messageList.add(new messageListModel(" How are you",firebaseUser.getUid(),"","02:00","","","TEXT","",""));

        messageListAdapter.notifyDataSetChanged();
        editWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),

                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,GALLERY_REQUEST);
                } else {
                    requestStoragePermission();
                }
            }
        });
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(firebaseUser.getUid());
            }
        });
    }

    @Override
    public void onItemClick(int position) {

    }


    @Override
    public void onLongItemClick(int position) {

    }
    private void getCurrentWallpaper(){
        DatabaseReference myRef = database.getReference().child("UserDetails").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel user=snapshot.getValue(userModel.class);
                chatWallpaperUrI=user.getChatWallpaper();
                seekBarProgress=user.getChatBlur();
                progressBar.setVisibility(View.VISIBLE);
                if (chatWallpaperUrI!=null){
                    Glide.with(getApplicationContext())
                            .load(chatWallpaperUrI)
                            .transform(new BlurTransformation(seekBarProgress))
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imageView)
                    ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getOpacity(){
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              changed=true;
              if(chatWallpaperUrI!=null || selected==null && imageView!=null){
                  seekBarProgress=progress/4;
                  imageView.setBlur(seekBarProgress);
              }

          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {

          }
      });

    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your storage")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(SettingsActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        }
    }

    private void uploadFile( String userId) {
        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
        progressBar.setVisibility(View.VISIBLE);
        if (selected != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + ".jpg");
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {

                if (!task.isSuccessful()) {

                    throw task.getException();
                }
                return fileReference.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Uri downloadUri = task.getResult();
//


                    Map<String ,Object> userDetail=new HashMap<>();

                    try{
                        userDetail.put("chatWallpaper",downloadUri.toString());
                        if(changed){
                            userDetail.put("chatBlur",seekBarProgress);
                        }

                        myRef.updateChildren(userDetail);
                    }catch (Exception e){
                        Log.e("Error",e.getLocalizedMessage());
                    }
                    Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Map<String ,Object> userDetail=new HashMap<>();
            if(changed){
                progressBar.setVisibility(View.GONE);
                userDetail.put("chatBlur",seekBarProgress);
                myRef.updateChildren(userDetail);
                Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
            }

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
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext())
                        .load(selected)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView)
                ;
                Log.d("imageUri",String.valueOf(selected));

                getOpacity();
//                Glide.with(getApplicationContext()).load(selected.toString()).into(imageView);




            }catch(Exception e){
                Log.d("error",e.getMessage());


            }
        }

    }
}