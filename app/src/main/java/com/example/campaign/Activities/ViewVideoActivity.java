package com.example.campaign.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.campaign.Common.DownloadFromUrl;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

import static com.example.campaign.Activities.ViewImageActivity.PERMISSION_WRITE;

public class ViewVideoActivity extends AppCompatActivity {
    private String videoUrI,direction,caption;
    private String otherUserName;
    private EmojiconTextView emojiconTextView;
    private VideoView videoView;
    private ProgressBar progressBar;
    private View captionBox;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);
        videoView=findViewById(R.id.attachedVideo);
        captionBox=findViewById(R.id.captionBox);
        emojiconTextView=findViewById(R.id.caption);
        videoUrI=getIntent().getStringExtra("videoUrI");
        otherUserName=getIntent().getStringExtra("otherUserName");
        caption=getIntent().getStringExtra("caption");
        direction=getIntent().getStringExtra("direction");
        progressBar=findViewById(R.id.progressBar);
        ActionBar actionBar=getSupportActionBar();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        if(direction!=null ){
            if(direction.equals("to")){
                actionBar.setTitle("To "+ otherUserName);
            }else{
                actionBar.setTitle("From "+ otherUserName);
            }
        }

        if(caption!=null){
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

    private long downloadVideo(){
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(videoUrI);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle("Video is downloading")
                .setAllowedOverMetered(true)
                .setVisibleInDownloadsUi(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"letsTalk1.mp4")
                .allowScanningByMediaScanner();

        long reference = manager.enqueue(request);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"Question.mp4");
        return reference;

    }
    private void download() {
        Downback DB = new Downback();
        DB.execute("");

    }


    private class Downback extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            downloadFile(videoUrI);
            return null;
        }
    }

    private void downloadFile(String videoUrI) {
        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String name = "video" + date + ".mp4";
        try {
            String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + File.separator + "Lets Talk" ;
            File rootFile = new File(rootDir);
            rootFile.mkdir();
            URL url = new URL(videoUrI);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(rootFile,
                    name));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();

        } catch (IOException e) {
            Log.d("Error....", e.toString());
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.view_menu,menu);
        MenuItem saveItem=menu.findItem(R.id.saveFile);
        saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                checkPermission();
//                download();
                DownloadFromUrl d=new DownloadFromUrl();
                d.context=getApplicationContext();
                d.otherUserName=otherUserName;
                d.progressBar=progressBar;
                d.execute(videoUrI);
                return false;

//
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            saveImage(imageUrI);
        }
    }
}