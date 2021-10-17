package com.neuralBit.letsTalk.Activities;

import static android.view.View.GONE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campaign.R;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.IOException;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class sendImage extends AppCompatActivity {

    private String selected;
    private EmojiconEditText caption;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ZoomInImageView imageView = findViewById(R.id.attachedImage);
        ImageButton emojiButton = findViewById(R.id.emoji_button);

        ImageButton attachButton =findViewById(R.id.attachButton);
        attachButton.setVisibility(GONE);
        selected=getIntent().getStringExtra("imageUrI");
        otherUserId=getIntent().getStringExtra("otherUserId");
        String otherUserName = getIntent().getStringExtra("otherUserName");
        caption=findViewById(R.id.message_container);
        View textArea=findViewById(R.id.constraint_layout2);
        EmojIconActions emojiIcon=new EmojIconActions(getApplicationContext(),textArea,caption, emojiButton,"#495C66","#DCE1E2","#0B1830");
        emojiIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojiIcon.ShowEmojIcon();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Send to "+ otherUserName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(selected));
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(v -> uploadFile(otherUserId));
    }


    private void uploadFile( String otherUserId) {
        if (selected != null) {
            Intent intent=new Intent(getApplicationContext(),ChatActivity.class)
                    .putExtra("imageUrI",selected)
                    .putExtra("receiver",otherUserId)
                    .putExtra("caption",caption.getText().toString())
                    ;
            startActivity(intent);
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

}