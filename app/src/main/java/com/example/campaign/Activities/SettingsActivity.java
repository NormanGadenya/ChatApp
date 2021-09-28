package com.example.campaign.Activities;

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
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Common.ServiceCheck;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.example.campaign.Services.updateStatusService;
import com.example.campaign.adapter.messageSettingsAdapter;
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

public class SettingsActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    private static final int GALLERY_REQUEST = 100;
    private final List<messageListModel> messageList = new ArrayList<>();
    private Uri selected;
    private BlurImageView imageView;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    int seekBarProgress = 1;
    boolean changed = false;
    private String chatWallpaperUrI;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;
    private boolean showOnline, showLastSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        Button applyButton = findViewById(R.id.done);
        imageView = findViewById(R.id.imageView);
        imageView.setClipToOutline(true);
        imageView.setBackgroundResource(R.drawable.card_background3);
        seekBar = findViewById(R.id.seekBar);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        CheckBox onlineStatus = findViewById(R.id.onlineStatus);
        CheckBox lastSeenStatus = findViewById(R.id.lastSeenStatus);
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

        onlineStatus.setChecked(online);
        lastSeenStatus.setChecked(lastSeen);

        recyclerView = findViewById(R.id.recycler_view_wall);
        FloatingActionButton editWallpaper = findViewById(R.id.editWallpaper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        com.example.campaign.adapter.messageSettingsAdapter messageSettingsAdapter = new messageSettingsAdapter(messageList, getApplicationContext());
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
        applyButton.setOnClickListener(v -> setSettings(firebaseUser.getUid()));
    }


    private void getCurrentWallpaper() throws IOException {
        chatWallpaperUrI = sharedPreferences.getString("chatWallpaper", null);
        int blur = sharedPreferences.getInt("chatBlur", 0);
        if (chatWallpaperUrI != null) {
            if (blur != 0) {
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

            } else {
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

        } else {
            imageView.setImageResource(R.drawable.whatsapp_wallpaper_121);
        }


    }

    private void getOpacity() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("showLastSeenState", showLastSeen);
        userDetail.put("showOnlineState", showOnline);
        myRef.updateChildren(userDetail);
        editor.putBoolean("showOnline", showOnline);
        editor.putBoolean("showLastSeen", showLastSeen);
        if (selected != null) {
            try {
                editor.putString("chatWallpaper", selected.toString());
                editor.putInt("chatBlur", seekBarProgress);
                Log.e("Error", sharedPreferences.getString("chatWallpaper", null));


            } catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
            }

        } else {

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
            try {
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
                Log.d("imageUri", String.valueOf(selected));

                getOpacity();


            } catch (Exception e) {
                Log.d("error", e.getMessage());


            }
        }

    }


}