package com.neuralBit.letsTalk.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.neuralBit.letsTalk.Common.DownloadFromUrl;
import com.example.campaign.R;
import com.neuralBit.letsTalk.Common.Tools;

import org.jetbrains.annotations.NotNull;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ViewVideoActivity extends AppCompatActivity {
    private String videoUrI;
    private String otherUserName;
    private VideoView videoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);
        videoView=findViewById(R.id.attachedVideo);
        View captionBox = findViewById(R.id.captionBox);
        EmojiconTextView emojiconTextView = findViewById(R.id.caption);
        videoUrI=getIntent().getStringExtra("videoUrI");
        Tools tools = new Tools();
        otherUserName=getIntent().getStringExtra("otherUserName");
        String caption = getIntent().getStringExtra("caption");

        try {
            videoUrI=tools.decryptText(videoUrI);
            caption= tools.decryptText(caption);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String direction = getIntent().getStringExtra("direction");
        progressBar=findViewById(R.id.progressBar);
        ActionBar actionBar=getSupportActionBar();

        if(direction !=null ){
            if(direction.equals("to")){
                actionBar.setTitle("To "+ otherUserName);
            }else{
                actionBar.setTitle("From "+ otherUserName);
            }
        }

        if(caption !=null){
            captionBox.setVisibility(View.VISIBLE);
            emojiconTextView.setText(caption);
        }else{
            captionBox.setVisibility(View.GONE);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        videoView.setVideoURI(Uri.parse(videoUrI));
        videoView.setOnPreparedListener(I->{
            progressBar.setVisibility(View.GONE);
            videoView.start();
        });
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.view_menu,menu);
        MenuItem saveItem=menu.findItem(R.id.saveFile);
        saveItem.setOnMenuItemClickListener(item -> {
            checkPermission();
            DownloadFromUrl d=new DownloadFromUrl();
            d.setContext(getApplicationContext());
            d.otherUserName=otherUserName;
            d.progressBar=progressBar;
            d.execute(videoUrI);
            return false;

        });
        return super.onCreateOptionsMenu(menu);
    }

    public void checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ViewImageActivity.PERMISSION_WRITE);
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode== ViewImageActivity.PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            DownloadFromUrl d=new DownloadFromUrl();
            d.setContext(getApplicationContext());
            d.otherUserName=otherUserName;
            d.progressBar=progressBar;
            d.execute(videoUrI);
        }
    }
}