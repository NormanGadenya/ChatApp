package com.neuralBit.letsTalk.Activities;

import static android.view.View.GONE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campaign.R;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class SendVideo extends AppCompatActivity {

    private String selected;
    private EmojiconEditText caption;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_video);
        ImageButton sendButton = findViewById(R.id.sendButton);


        ImageButton emojiButton = findViewById(R.id.emoji_button);
        VideoView videoView = findViewById(R.id.attachedVideo);
        selected=getIntent().getStringExtra("videoUrI");
        otherUserId=getIntent().getStringExtra("otherUserId");
        String otherUserName = getIntent().getStringExtra("otherUserName");
        ImageButton attachButton =findViewById(R.id.attachButton);
        attachButton.setVisibility(GONE);
        caption=findViewById(R.id.message_container);
        View textArea=findViewById(R.id.constraint_layout2);
        EmojIconActions emojiIcon=new EmojIconActions(getApplicationContext(),textArea,caption, emojiButton,"#495C66","#DCE1E2","#0B1830");
        emojiIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojiIcon.ShowEmojIcon();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Send to "+ otherUserName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
       videoView.setVideoURI(Uri.parse(selected));
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        sendButton.setOnClickListener(v -> uploadFile(otherUserId));
    }


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