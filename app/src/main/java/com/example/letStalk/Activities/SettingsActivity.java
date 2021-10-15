package com.example.letStalk.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.letStalk.Common.ServiceCheck;
import com.example.letStalk.Model.UserViewModel;
import com.example.letStalk.Model.messageListModel;
import com.example.campaign.R;
import com.example.letStalk.Services.updateStatusService;
import com.example.letStalk.adapter.messageSettingsAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jgabrielfreitas.core.BlurImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.letStalk.Common.Tools.GALLERY_REQUEST;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private final List<messageListModel> messageList = new ArrayList<>();
    private Uri selected;
    private BlurImageView imageView;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    private int seekBarProgress = 1;
    private boolean changed = false;
    private String chatWallpaperUrI;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;
    private boolean showOnline, showLastSeen,setFingerprint;
    private Button logout;
    private FloatingActionButton restoreButton;
    public static final String TAG="SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        Button applyButton = findViewById(R.id.done);
        imageView = findViewById(R.id.imageView);
        restoreButton=findViewById(R.id.wallPaperRestore);
        imageView.setClipToOutline(true);
        imageView.setBackgroundResource(R.drawable.card_background3);
        seekBar = findViewById(R.id.seekBar);
        logout=findViewById(R.id.logout);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        CheckBox onlineStatus = findViewById(R.id.onlineStatus);
        CheckBox lastSeenStatus = findViewById(R.id.lastSeenStatus);
        CheckBox fingerprintStatus = findViewById(R.id.fingerprint);
        userViewModel.initFUserInfo();
        progressBar = findViewById(R.id.progressBarChatWallpaper);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck= new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
        try {
            getOpacity();
            getCurrentWallpaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean lastSeen = sharedPreferences.getBoolean("showLastSeen", true);
        boolean online = sharedPreferences.getBoolean("showOnline", true);
        boolean fingerprint = sharedPreferences.getBoolean("setFingerprint",false);
        onlineStatus.setChecked(online);
        lastSeenStatus.setChecked(lastSeen);
        fingerprintStatus.setChecked(fingerprint);

        recyclerView = findViewById(R.id.recycler_view_wall);
        FloatingActionButton editWallpaper = findViewById(R.id.editWallpaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageSettingsAdapter messageSettingsAdapter = new messageSettingsAdapter(messageList, getApplicationContext());
        recyclerView.setAdapter(messageSettingsAdapter);
        messageList.add(new messageListModel(null, "123", "17-04-2020", "02:00", "", "", "TEXT", ""));
        messageList.add(new messageListModel(null, firebaseUser.getUid(), "17-04-2020", "02:00", "", "", "TEXT", ""));
        messageList.add(new messageListModel(null, "firebaseUser.getUid()", "17-04-2020", "02:00", "", "", "TEXT", ""));
        messageList.add(new messageListModel(null, firebaseUser.getUid(), "17-04-2020", "02:00", "", "", "TEXT", ""));
        messageList.add(new messageListModel(null, "firebaseUser.getUid()", "17-04-2020", "02:00", "", "", "TEXT", ""));
        messageList.add(new messageListModel(null, firebaseUser.getUid(), "17-04-2020", "02:00", "", "", "TEXT", ""));

        messageSettingsAdapter.notifyDataSetChanged();

        editWallpaper.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),

                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST);
            } else {
                requestStoragePermission();
            }
        });
        onlineStatus.setOnCheckedChangeListener((buttonView, isChecked) -> showOnline = isChecked);

        lastSeenStatus.setOnCheckedChangeListener((buttonView, isChecked) -> showLastSeen = isChecked);
        fingerprintStatus.setOnCheckedChangeListener((buttonView, isChecked) -> setFingerprint = isChecked);
        applyButton.setOnClickListener(v -> setSettings(firebaseUser.getUid()));
        logout.setOnClickListener(v->{
            new AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to sign out")
                    .setPositiveButton("Yes", (dialog, which) -> signOut(serviceCheck))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });
        restoreButton.setOnClickListener(I->{
           selected=null;
           imageView.setImageResource(R.drawable.whatsapp_wallpaper_121);

        });
    }

    private void signOut(ServiceCheck service) {
        FirebaseAuth firebase= FirebaseAuth.getInstance();
        firebase.signOut();
//        moveTaskToBack(true);
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
//        AuthUI.getInstance().signOut(this).addOnSuccessListener(I->{
//            moveTaskToBack(true);
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(1);
//        });

    }


    private void getCurrentWallpaper()  {
        chatWallpaperUrI = sharedPreferences.getString("chatWallpaper", null);
        int blur = sharedPreferences.getInt("chatBlur", 0);
        if (chatWallpaperUrI != null) {
                RequestBuilder<Drawable> requestBuilder = Glide.with(getApplicationContext()).load(chatWallpaperUrI);

                if( blur!=0){
                    requestBuilder =requestBuilder.transform(new BlurTransformation(blur));

                }

                requestBuilder.addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        Log.e(TAG, "onLoadFailed: ", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        seekBar.setProgress(blur * 4);
                        return false;
                    }
                }).into(imageView);

        } else {
            imageView.setImageResource(R.drawable.whatsapp_wallpaper_121);
        }


    }

    private void getOpacity() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changed = true;
                if (chatWallpaperUrI != null || selected == null) {
                    seekBarProgress = progress / 4;
                    try {
                        if (seekBarProgress != 1) {
                            imageView.setBlur(seekBarProgress);
                        }
                    } catch (Exception e) {
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
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        }
    }

    private void setSettings(String userId) {
        DatabaseReference myRef = database.getReference().child("UserDetails").child(userId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        progressBar.setVisibility(VISIBLE);
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("showLastSeenState", showLastSeen);
        userDetail.put("showOnlineState", showOnline);
        myRef.updateChildren(userDetail);
        editor.putBoolean("showOnline", showOnline);
        editor.putBoolean("showLastSeen", showLastSeen);
        editor.putBoolean("setFingerprint",setFingerprint);
        Log.d(TAG, "setSettings: "+selected);
        if (selected != null) {
            try {
                editor.putString("chatWallpaper", selected.toString());
                editor.putInt("chatBlur", seekBarProgress);
                Log.e("Error", sharedPreferences.getString("chatWallpaper", null));


            } catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
            }

        } else {
            editor.putString("chatWallpaper",null);
            if (changed) {
                progressBar.setVisibility(View.GONE);
                editor.putInt("chatBlur", seekBarProgress);
            }

        }
        editor.apply();

        progressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
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
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selected = data.getData();
            restoreButton.setVisibility(VISIBLE);
            try {
                progressBar.setVisibility(VISIBLE);
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

                getOpacity();


            } catch (Exception e) {
                Log.d("error", e.getMessage());

            }

        }

    }


}