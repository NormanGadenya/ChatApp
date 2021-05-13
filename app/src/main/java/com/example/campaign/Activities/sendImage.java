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

    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase database;
    private StorageReference mStorageReference;
    private String selected;
    private String otherUserName;
    private EmojiconEditText caption;
    private String otherUserId;
    private FirebaseUser firebaseUser;
    private ZoomInImageView imageView;
    private ImageButton emojiButton;
    View rootView,layoutActions,textArea;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        sendButton=findViewById(R.id.sendButton);
        firebaseStorage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        imageView=findViewById(R.id.attachedImage);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        mStorageReference=firebaseStorage.getReference();
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
                uploadFile(firebaseUser.getUid(),otherUserId);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile(String userId, String otherUserId) {


        if (selected != null) {
//            SharedPreferences sharedPreferences=getSharedPreferences("selectedImagePref",MODE_PRIVATE);
//            SharedPreferences.Editor editor=sharedPreferences.edit();
//            editor.putString("imageUrI",selected);
//            editor.putString("receiver",otherUserId);
//            editor.putString("caption",caption.getText().toString());
//            editor.apply();

            Intent intent=new Intent(getApplicationContext(),ChatActivity.class)
                    .putExtra("imageUrI",selected)
                    .putExtra("receiver",otherUserId)
                    .putExtra("caption",caption.getText().toString())
                    ;


            startActivity(intent);

//            imageLoadProgressBar.setVisibility(View.VISIBLE);
//
//            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
//                    + ".jpg");
//            UploadTask uploadTask =fileReference.putFile(Uri.parse(selected));
//            uploadTask.continueWithTask(task -> {
//                if (!task.isSuccessful()) {
//                    throw task.getException();
//                }
//                return fileReference.getDownloadUrl();
//            }).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Uri downloadUri = task.getResult();
//
//                    Bitmap bitmap = null;
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(selected));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                        DatabaseReference myRef = database.getReference();
//                        messageListModel messageUser=new messageListModel();
//                        messageListModel messageOtherUser=new messageListModel();
//
//                        assert downloadUri != null;
//                        messageOtherUser.setText(caption.getText().toString());
//                        messageOtherUser.setImageUrI(downloadUri.toString());
//                        messageOtherUser.setTime(getTime());
//                        messageOtherUser.setDate(getDate());
//                        messageOtherUser.setType("IMAGE");
//                        messageOtherUser.setReceiver(otherUserId);
//
//                        messageUser.setText(caption.getText().toString());
//                        messageUser.setImageUrI(selected);
//                        messageUser.setTime(getTime());
//                        messageUser.setDate(getDate());
//                        messageUser.setType("IMAGE");
//                        messageUser.setReceiver(otherUserId);
//                        try{
//                            myRef.child("chats").child(userId).child(otherUserId).push().setValue(messageUser);
//                            myRef.child("chats").child(otherUserId).child(userId).push().setValue(messageOtherUser);
//
//                        }catch(Exception e){
//                            Log.d("error",e.getLocalizedMessage());
//                            progressBar.setVisibility(View.GONE);
//                        }
//                        imageLoadProgressBar.setVisibility(View.GONE);
//                        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
//                        startActivity(intent);
//
//
//                    }

//            });
////            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
////                @Override
////                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
////                    double progress=(100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
////                    imageLoadProgressBar.setProgress((int)progress);
////                }
////            });

        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter timeObj = DateTimeFormatter.ofPattern("HH:mm");
        return myDateObj.format(timeObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(dateObj);
    }
}