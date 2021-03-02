package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.view.MenuInflater;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.example.campaign.adapter.messageListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class chatActivity extends AppCompatActivity implements RecyclerViewInterface {

    private String otherUserId, message, profileUrI,text,date,time,messageStatus,messageId,otherUserName;
    private FirebaseDatabase database;
    private List<messageListModel> messageList = new ArrayList<>();
    private RecyclerView recyclerView ;
    private ImageButton sendButton,attachButton;
    private TextView newMessage,userName;
    private CircularImageView profilePic;
    private FirebaseUser user ;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageReference;
    private Uri selected;
    private Context context;
    private MenuInflater menuInflater;
    private ProgressBar progressBar;
    private messageListAdapter messageListAdapter;
    private Vibrator vibrator;



    private FirebaseStorage storage= FirebaseStorage.getInstance();
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user= FirebaseAuth.getInstance().getCurrentUser();
        InitialiseControllers();
        database = FirebaseDatabase.getInstance();
        firebaseStorage= FirebaseStorage.getInstance();
        mStorageReference=firebaseStorage.getReference();
        if(otherUserId==null){
            loadSharedPreferenceData();
            System.out.println("otherUserId"+otherUserId);
        }

        userName.setText(otherUserName);
        try{
            Glide.with(getApplicationContext()).load(profileUrI).listener(new RequestListener<Drawable>() {
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
            }).into(profilePic);
            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(getApplicationContext(),viewImageActivity.class);
                    intent.putExtra("imageUrI",profileUrI);
                    startActivity(intent);
                }
            });

        }catch(Exception e){
            profilePic.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageListAdapter=new messageListAdapter(messageList, chatActivity.this, profileUrI,this);
        recyclerView.setAdapter(messageListAdapter);



        try{
            getMessages();
        }catch (Exception e){
            Log.d("Error" ,e.getLocalizedMessage());
        }

        final MediaPlayer mediaPlayer= MediaPlayer.create(this,R.raw.messagesound);

        sendButton.setOnClickListener(view -> {
            DatabaseReference sMessage_1=database.getReference().child("chats").child(otherUserId).child(user.getUid()).push();
            DatabaseReference sMessage_2=database.getReference().child("chats").child(user.getUid()).child(otherUserId).push();
            message=newMessage.getText().toString();

            String formattedDate = getDate();
            String formattedTime=getTime();
            //mediaPlayer.start();
            messageListModel m=new messageListModel();
            m.setText(message);
            m.setReceiver(otherUserId);
            m.setDate(formattedDate);
            m.setTime(formattedTime);
            m.setType("TEXT");
            sMessage_1.setValue(m);
            sMessage_2.setValue(m);
            newMessage.setText("");


        });
//

        attachButton.setOnClickListener(view ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        });
        ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    int position = viewHolder.getAdapterPosition();
                    switch (messageList.get(position).getType()) {
                        case "TEXT":
                            vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(50);
                            messageId = messageList.get(position).getMessageId();
                            Toast.makeText(context, "itemDeleted", Toast.LENGTH_LONG).show();
                            DatabaseReference messageRef = database.getReference().child("chats").child(user.getUid()).child(otherUserId);
                            messageRef.child(messageId).removeValue();
                            messageList.remove(position);
                            messageListAdapter.notifyItemRemoved(position);

                            break;
                        case "IMAGE":
                            messageId = messageList.get(position).getMessageId();
                            String imageUrI = messageList.get(position).getImageUrI();
                            vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(50);
                            Toast.makeText(context, "itemDeleted", Toast.LENGTH_LONG).show();
                            StorageReference imageRef = firebaseStorage.getReferenceFromUrl(imageUrI);
                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DatabaseReference messageRef = database.getReference().child("chats").child(user.getUid()).child(otherUserId);
                                    messageRef.child(messageId).removeValue();
                                    messageList.remove(position);
                                    messageListAdapter.notifyItemRemoved(position);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                }catch (Exception e){
                    Log.e("Error",e.getLocalizedMessage());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(chatActivity.this,R.color.Red_200))
                        .addActionIcon(R.drawable.remove)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    private void loadSharedPreferenceData() {
        SharedPreferences sharedPreferences=getSharedPreferences("sharedPreferences",MODE_PRIVATE);
        otherUserName=sharedPreferences.getString("otherUserName",null);
        otherUserId=sharedPreferences.getString("otherUserId",null);
        profileUrI=sharedPreferences.getString("profileUrI",null);
    }

    private void saveSharedPreferenceData() {
        SharedPreferences sharedPreferences =getSharedPreferences("sharedPreferences",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("otherUserName",otherUserName);
        editor.putString("otherUserId",otherUserId);
        editor.putString("profileUrI",profileUrI);

        editor.apply();
    }


    private void InitialiseControllers() {
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater LayoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View actionBarView=LayoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionBarView);
        sendButton=findViewById(R.id.sendButton);
        attachButton= findViewById(R.id.attachButton);
        profilePic=findViewById(R.id.image_profile);
        newMessage=findViewById(R.id.message_container);
        progressBar=findViewById(R.id.progressBar2);
        otherUserId=getIntent().getStringExtra("userId");
        otherUserName=getIntent().getStringExtra("userName");
        profileUrI =getIntent().getStringExtra("profileUrI");
        if(otherUserId!=null){
            saveSharedPreferenceData();
        }
        context=getApplicationContext();
        userName=findViewById(R.id.userName);
        recyclerView=findViewById(R.id.recyclerView1);
    }

    private void getMessages(){

        DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid()).child(otherUserId);
        messageRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    try{
                        messageListModel message = snapshot.getValue(messageListModel.class);
                        messageStatus=message.getMessageStatus();
                        message.setMessageId(snapshot.getKey());
                        String receiver = message.getReceiver();
                        messageList.add(message);


                        if (messageList.size() >= 1) {
                            recyclerView.scrollToPosition(messageList.size()-1);
                        }

                        if (receiver.equals(user.getUid())){
                            messageRef.child(snapshot.getKey()).child("messageStatus").setValue("read");
                        }else{
                            messageRef.child(snapshot.getKey()).child("messageStatus").setValue("unread");
                        }

                    }catch(Exception e){
                        Log.d("error1",e.getMessage());
                    }

                    messageListAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode== Activity.RESULT_OK && data!=null){
            selected=data.getData();
            try{
                uploadFile(user.getUid(),otherUserId);
            }catch(Exception e){
                Log.d("error",e.getLocalizedMessage());
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile(String userId, String otherUserId) {
        ProgressBar progressBar=findViewById(R.id.progressBar);
        if (selected != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + ".jpg");

            fileReference.putFile(selected).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    DatabaseReference myRef = database.getReference();
                    messageListModel message=new messageListModel();
                    assert downloadUri != null;
                    message.setImageUrI(downloadUri.toString());
                    message.setTime(getTime());
                    message.setDate(getDate());
                    message.setType("IMAGE");
                    message.setReceiver(otherUserId);

                    try{
                        myRef.child("chats").child(userId).child(otherUserId).push().setValue(message);
                        myRef.child("chats").child(otherUserId).child(userId).push().setValue(message);


                    }catch(Exception e){
                        Log.d("error",e.getLocalizedMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        MenuItem settings=menu.findItem(R.id.settingsButton);
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(chatActivity.this , settingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(chatActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onItemClick(int position) {

        Intent intent =new Intent(context, viewImageActivity.class)
                .putExtra("imageUrI", messageList.get(position).getImageUrI())
                .putExtra("profileUrI", messageList.get(position).getProfileUrI())
                .putExtra("userId",otherUserId)
                .putExtra("userName",otherUserName);
        startActivity(intent);

    }

    @Override
    public void onLongItemClick(int position) {
        switch (messageList.get(position).getType()){
            case "TEXT":
                vibrator=(Vibrator)context.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(50);
                messageId= messageList.get(position).getMessageId();
                Toast.makeText(context,"itemDeleted",Toast.LENGTH_LONG).show();
                DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid()).child(otherUserId);
                messageRef.child(messageId).removeValue();
                messageListAdapter.notifyItemRemoved(position);

                break;
            case "IMAGE":
                messageId= messageList.get(position).getMessageId();
                String imageUrI= messageList.get(position).getImageUrI();
                vibrator=(Vibrator)context.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(50);
                Toast.makeText(context,"itemDeleted",Toast.LENGTH_LONG).show();
                StorageReference imageRef=firebaseStorage.getReferenceFromUrl(imageUrI);
                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid()).child(otherUserId);
                        messageRef.child(messageId).removeValue();
                        messageListAdapter.notifyItemRemoved(position);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_LONG).show();
                    }
                });
        }

    }
}