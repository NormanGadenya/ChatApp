package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class sendImage extends AppCompatActivity {

    private ImageButton sendButton;
    private String selected;
    private String otherUserName;
    private EmojiconEditText caption;
    private String otherUserId;
    private ZoomInImageView imageView;
    private ImageButton emojiButton;
    View rootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        sendButton=findViewById(R.id.sendButton);
        imageView=findViewById(R.id.attachedImage);
        emojiButton=findViewById(R.id.emoji_button);
        rootView=findViewById(R.id.constraint_layout2);
        selected=getIntent().getStringExtra("imageUrI");
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

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(selected));
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

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