package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.adapter.messageListAdapter;
import com.google.android.material.button.MaterialButton;
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

import static android.view.View.GONE;

public class SettingsActivity extends AppCompatActivity {
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
    UserViewModel userViewModel;
    private SharedPreferences sharedPreferences ;
    private CheckBox onlineStatus,lastSeenStatus;
    private boolean showOnline,showLastSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences=getSharedPreferences("Settings",MODE_PRIVATE);
        toolbar=findViewById(R.id.toolbar);
        applyButton=findViewById(R.id.done);
        imageView=findViewById(R.id.imageView);
        imageView.setClipToOutline(true);
        imageView.setBackgroundResource(R.drawable.card_background3);
        seekBar=findViewById(R.id.seekBar);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        onlineStatus=findViewById(R.id.onlineStatus);
        lastSeenStatus=findViewById(R.id.lastSeenStatus);
        userViewModel.initFUserInfo();
        progressBar=findViewById(R.id.progressBarChatWallpaper);
        firebaseStorage=FirebaseStorage.getInstance();
        mStorageReference=firebaseStorage.getReference();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            getOpacity();
            getCurrentWallpaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean lastSeen=sharedPreferences.getBoolean("showLastSeen",true);
        boolean online=sharedPreferences.getBoolean("showOnline",true);


        onlineStatus.setChecked(online);
        lastSeenStatus.setChecked(lastSeen);

        recyclerView=findViewById(R.id.recycler_view_wall);
        editWallpaper=findViewById(R.id.editWallpaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageListAdapter=new messageListAdapter();
        messageListAdapter.setMContext(getApplicationContext());
        messageListAdapter.setMessageList(messageList);
        messageListAdapter.setActivity(this);

        messageListAdapter.setOtherUserId("");
        recyclerView.setAdapter(messageListAdapter);
        messageList.add(new messageListModel(" Hi","123","17-04-2020","02:00","","","TEXT",""));
        messageList.add(new messageListModel(" Hey",firebaseUser.getUid(),"17-04-2020","02:00","","","TEXT",""));
        messageList.add(new messageListModel(" How are you",firebaseUser.getUid(),"17-04-2020","02:00","","","TEXT",""));

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
        onlineStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showOnline=true;
                }else{
                    showOnline=false;
                }
            }
        });

        lastSeenStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showLastSeen=true;

                }else{
                    showLastSeen=false;
                }

            }
        });
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings(firebaseUser.getUid());
            }
        });
    }




    private void getCurrentWallpaper() throws IOException {
        chatWallpaperUrI=sharedPreferences.getString("chatWallpaper",null);
        Log.d("chekced", String.valueOf(sharedPreferences.getAll()));
        int blur=sharedPreferences.getInt("chatBlur",0);
        if (chatWallpaperUrI!=null){
            if(blur!=0) {
                Glide.with(getApplicationContext())
                        .load(chatWallpaperUrI)
                        .transform(new BlurTransformation(blur))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(GONE);
                                seekBar.setProgress(blur * 4);

                                return false;
                            }
                        })

                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView);

            }else{
                Glide.with(getApplicationContext())
                        .load(chatWallpaperUrI)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(GONE);
                                seekBar.setProgress(blur * 4);

                                return false;
                            }
                        })

                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView);

            }

        }else{
            imageView.setImageResource(R.drawable.whatsapp_wallpaper_121);
        }




    }

    private void getOpacity(){
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
          @RequiresApi(api = Build.VERSION_CODES.O)
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              changed=true;
              if(chatWallpaperUrI!=null || selected==null){
                  seekBarProgress=progress/4;
                  try{
                      if(seekBarProgress!=1){
                          imageView.setBlur(seekBarProgress);
                      }
                  }catch (Exception e){
                      e.getStackTrace();
                  }
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

    private void setSettings( String userId) {
        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        progressBar.setVisibility(View.VISIBLE);




        Map<String ,Object> userDetail=new HashMap<>();
        userDetail.put("showLastSeenState",showLastSeen);
        userDetail.put("showOnlineState",showOnline);
        myRef.updateChildren(userDetail);



        editor.putBoolean("showOnline",showOnline);
        editor.putBoolean("showLastSeen",showLastSeen);
        if (selected != null) {
            try{
                editor.putString("chatWallpaper",selected.toString());
                editor.putInt("chatBlur",seekBarProgress);
                Log.e("Error",sharedPreferences.getString("chatWallpaper",null));


            }catch (Exception e){
                Log.e("Error",e.getLocalizedMessage());
            }

//
//            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
//                    + ".jpg");
//            UploadTask uploadTask =fileReference.putFile(selected);
//            uploadTask.continueWithTask(task -> {
//
//                if (!task.isSuccessful()) {
//
//                    throw task.getException();
//                }
//                return fileReference.getDownloadUrl();
//
//            }).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    progressBar.setVisibility(View.GONE);
//                    Uri downloadUri = task.getResult();
////
//
//
//                   }
//            });
        }else{

            if(changed){
                progressBar.setVisibility(View.GONE);
                editor.putInt("chatBlur",seekBarProgress);
//                userDetail.put("chatBlur",seekBarProgress);
//

            }

        }
        editor.commit();

        progressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void status(boolean status){
        DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(firebaseUser.getUid());
        Map<String ,Object> onlineStatus=new HashMap<>();
        onlineStatus.put("online",status);
        userDetailRef.updateChildren(onlineStatus);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        status(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPause() {
        super.onPause();
        status(false);
    }
}