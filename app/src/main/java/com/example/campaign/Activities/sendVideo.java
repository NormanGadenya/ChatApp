package com.example.campaign.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class sendVideo extends AppCompatActivity {

    private ImageButton sendButton;

    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    private String selected;
    private String otherUserName;
    private EmojiconEditText caption;
    private String otherUserId;
    private FirebaseUser firebaseUser;
    private VideoView videoView;
    private ImageButton emojiButton;
    View rootView,layoutActions,textArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_video);
        sendButton=findViewById(R.id.sendButton);
        firebaseStorage= FirebaseStorage.getInstance();
        database= FirebaseDatabase.getInstance();
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        mStorageReference=firebaseStorage.getReference();
        emojiButton=findViewById(R.id.emoji_button);
        videoView=findViewById(R.id.attachedVideo);
        rootView=findViewById(R.id.constraint_layout2);
        selected=getIntent().getStringExtra("videoUrI");
        otherUserId=getIntent().getStringExtra("otherUserId");
        otherUserName=getIntent().getStringExtra("otherUserName");
        caption=findViewById(R.id.caption);
        EmojIconActions emojIcon=new EmojIconActions(getApplicationContext(),rootView,caption,emojiButton,"#495C66","#DCE1E2","#0B1830");
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojIcon.ShowEmojIcon();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Send to "+otherUserName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
       videoView.setVideoURI(Uri.parse(selected));
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                uploadFile(otherUserId);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile( String otherUserId) {
        if (selected != null) {
            Intent intent=new Intent(getApplicationContext(),ChatActivity.class)
                    .putExtra("videoUrI",selected)
                    .putExtra("receiver",otherUserId)
                    .putExtra("caption",caption.getText().toString())
                    ;
            startActivity(intent);
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

}