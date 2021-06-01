package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.MenuInflater;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Interfaces.APIService;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.MessageViewModel;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.Notifications.Client;
import com.example.campaign.Notifications.Data;
import com.example.campaign.Notifications.MyResponse;
import com.example.campaign.Notifications.Sender;
import com.example.campaign.Notifications.Token;
import com.example.campaign.R;
import com.example.campaign.adapter.messageListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.alterac.blurkit.BlurKit;
import io.alterac.blurkit.BlurLayout;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChatActivity extends AppCompatActivity implements RecyclerViewInterface {

    private String otherUserId, message, profileUrI,otherUserName,lastSeen;
    private FirebaseDatabase database;
    private List<messageListModel> messageList = new ArrayList<>();
    private RecyclerView recyclerView ;
    private ImageButton sendButton,attachButton,emojiButton;
    private TextView userName,onlineStatus,typing,msgGroupDate;
    private EmojiconEditText newMessage;
    private CircularImageView profilePic;
    private FirebaseUser user ;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageReference;
    private Uri selected;
    private Context context;
    private MenuInflater menuInflater;
    private ProgressBar progressBar,imageUploadProgress;
    private messageListAdapter messageListAdapter;
    private ImageView backgroundImageView;
    private SharedPreferences imageSharedPreferences,settingsSharedPreferences;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> imageUrIList=new ArrayList<>();
    boolean notify=false;
    FloatingActionButton imageButton,videoButton,audioButton;
    MessageViewModel messageViewModel;
    UserViewModel userViewModel;
    final int IMAGEREQUEST =2;
    final int AUDIOREQUEST=3;
    final int VIDEOREQUEST=4;

    MenuItem profileDetails;
    MenuItem settings,delete;
    View rootView,layoutActions,textArea;
    APIService apiService;
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
        emojiButton=findViewById(R.id.emoji_button);
        rootView=findViewById(R.id.constraint_layout2);
        layoutActions=findViewById(R.id.layout_actions);
        imageButton=findViewById(R.id.att_image);
        videoButton=findViewById(R.id.att_vid);
        audioButton=findViewById(R.id.att_audio);
        textArea=findViewById(R.id.constraint_layout2);
        mStorageReference=firebaseStorage.getReference();
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        userViewModel=new ViewModelProvider(this).get(UserViewModel.class);
        EmojIconActions emojIcon=new EmojIconActions(getApplicationContext(),rootView,newMessage,emojiButton,"#495C66","#DCE1E2","#0B1830");
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojIcon.ShowEmojIcon();

        if(otherUserId==null){
            loadSharedPreferenceData();
            System.out.println("otherUserId"+otherUserId);
        }
        if(otherUserId!=null){
            getOtherUserDetails(otherUserId);
        }
        setTypingStatus();
//        statusCheck(otherUserId);
        updateStatus();
        if(imageSharedPreferences.getString("imageUrI",null)!=null && imageSharedPreferences.getString("receiver",null)!=null){
            if(otherUserId!=null && imageSharedPreferences.getString("receiver",null).equals(otherUserId)){
//                uploadFile(user.getUid(),otherUserId);
            }
        }


        layoutManager= new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        messageViewModel.initChats(otherUserId);
        messageList=messageViewModel.getMessages().getValue();
        try{
            messageListAdapter=new messageListAdapter();
            messageListAdapter.setMContext(ChatActivity.this);
            messageListAdapter.setMessageList(messageList);
            messageListAdapter.setActivity(this);
            messageListAdapter.setRecyclerViewInterface(this);
            messageListAdapter.setOtherUserId(otherUserId);

            recyclerView.setAdapter(messageListAdapter);
        }catch(Exception e){

        }
        imageButton.setOnClickListener(I->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,IMAGEREQUEST);
        });

        videoButton.setOnClickListener(I->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,VIDEOREQUEST);
        });

        audioButton.setOnClickListener(I->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            try{

            }catch(Exception e){
                startActivityForResult(intent,AUDIOREQUEST);

            }

        });

        getCurrentWallpaper();



        try{
            getMessages();
        }catch (Exception e){
            Log.d("Error" ,e.getLocalizedMessage());
        }
        String imageUrI=getIntent().getStringExtra("imageUrI");
        String caption=getIntent().getStringExtra("caption");
        String videoUrI=getIntent().getStringExtra("videoUrI");
//        String audioUrI=getIntent().getStringExtra("audioUrI");
        if(imageUrI!=null){
            uploadImage(user.getUid(),otherUserId,Uri.parse(imageUrI),caption);

        }else if(videoUrI!=null){
            uploadVideo(user.getUid(),otherUserId,Uri.parse(videoUrI),caption);
        }
        sendButton.setOnClickListener(view -> {
            Log.d("userId",user.getUid()+ otherUserId);
            DatabaseReference sMessage_1=database.getReference().child("chats").child(otherUserId).child(user.getUid()).push();
            DatabaseReference sMessage_2=database.getReference().child("chats").child(user.getUid()).child(otherUserId);

            message=newMessage.getText().toString();
            updateToken(FirebaseInstanceId.getInstance().getToken());
            apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);

            if(!message.equals(null)){
                String formattedDate = getDate();
                String formattedTime=getTime();
                messageListModel m=new messageListModel();
                m.setText(message);
                m.setReceiver(otherUserId);
                m.setDate(formattedDate);
                m.setTime(formattedTime);
                m.setType("TEXT");
                sMessage_1.setValue(m);
                String messageKey= sMessage_1.getKey();
                sMessage_2.child(messageKey).setValue(m);
                notify=true;
                if(notify){

                    sendNotification(otherUserId,otherUserName,message);
                }
                newMessage.setText("");
            }

        });

        attachButton.setOnClickListener(view ->{
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent,2);
            textArea.setVisibility(GONE);
            layoutActions.setVisibility(VISIBLE);
            layoutActions.animate().alpha(1.0f).setDuration(1000);
        });

    }

    @Override
    protected void onStop() {

        super.onStop();

    }

    private void sendNotification(String otherUserId, String otherUserName, String message) {
        DatabaseReference tokens=database.getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(otherUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Token token=dataSnapshot.getValue(Token.class);
                    Data data=new Data(user.getUid(),R.mipmap.ic_launcher2,otherUserName+ ":" +message,otherUserId,"New message");
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>(){

                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success==1){
                                            showToast("failed");
                                        }else{
                                            showToast("achieved");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    showToast("failed2");
                                }
                            });
                    notify=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private HashMap<String,Object> updateTypingStatus(int count){
        HashMap<String,Object> typingStatus= new HashMap<>();
        if (count>=1){
            typingStatus.put("Typing",true);
        }else{
            typingStatus.put("Typing",false);
        }
        return typingStatus;
    }

    private void setTypingStatus(){
        DatabaseReference typingRef=database.getReference().child("UserDetails").child(user.getUid());
        newMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                typingRef.updateChildren(updateTypingStatus(count));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                typingRef.updateChildren(updateTypingStatus(count));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void getCurrentWallpaper(){
        String chatWallpaperUrI=settingsSharedPreferences.getString("chatWallpaper",null);
        int blur=settingsSharedPreferences.getInt("chatBlur",0);

        progressBar.setVisibility(VISIBLE);
        if (chatWallpaperUrI!=null){
            if(blur!=0) {
                Glide.with(getApplicationContext())
                        .load(chatWallpaperUrI)
                        .transform(new BlurTransformation(blur))
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

                        .into(backgroundImageView);
            }else{
                Glide.with(getApplicationContext())
                        .load(chatWallpaperUrI)
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

                        .into(backgroundImageView);
            }
            ;
        }else{
            backgroundImageView.setImageResource(R.drawable.whatsapp_wallpaper_121);
        }


//        userViewModel.getFUserInfo().observe(this,user ->{
//            chatWallpaperUrI=user.getChatWallpaper();
//            seekBarProgress=user.getChatBlur();
//            progressBar.setVisibility(View.VISIBLE);
//            if (chatWallpaperUrI!=null){
//                Glide.with(getApplicationContext())
//                        .load(chatWallpaperUrI)
//                        .transform(new BlurTransformation(seekBarProgress))
//                        .addListener(new RequestListener<Drawable>() {
//                            @Override
//                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                progressBar.setVisibility(View.GONE);
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                progressBar.setVisibility(View.GONE);
//                                DatabaseReference myRef = database.getReference().child("UserDetails").child(firebaseUser.getUid());
//                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        userModel user=snapshot.getValue(userModel.class);
//                                        seekBar.setProgress(user.getChatBlur()*4);
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//
//                                return false;
//                            }
//                        })
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .skipMemoryCache(true)
//                        .into(imageView)
//                ;
//            }
//        });

    }
//    private void getCurrentWallpaper(){
//
//        userViewModel.initFUserInfo();
//        userViewModel.getFUserInfo().observe(this,user ->{
//            String chatWallpaperUrI=user.getChatWallpaper();
//            int blur=user.getChatBlur();
//            if(chatWallpaperUrI!=null){
//                progressBar.setVisibility(View.VISIBLE);
//                Glide.with(getApplicationContext())
//                        .load(chatWallpaperUrI)
//                        .transform(new BlurTransformation(blur))
//                        .addListener(new RequestListener<Drawable>() {
//                            @Override
//                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                progressBar.setVisibility(GONE);
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                progressBar.setVisibility(GONE);
//                                return false;
//                            }
//                        })
//                        .into(backgroundImageView)
//                ;
//            }
//        });
//
//    }
    private void getTypingStatus(userModel user){
        if(user.getTyping()!=null){
            if(user.getTyping()){
//                            onlineStatus.setVisibility(View.VISIBLE);
                onlineStatus.setVisibility(GONE);
                typing.setVisibility(VISIBLE);

            }else{
                onlineStatus.setVisibility(VISIBLE);
                typing.setVisibility(View.GONE);
            }

        }

    }
    private void statusCheck(userModel user){
        System.out.println("user"+ user.getShowLastSeenState());
        if(user.getOnline()!=null && user.getShowOnlineState()!=null & user.getShowLastSeenState()!=null){
            if(user.getOnline() && user.getShowOnlineState()){
//                            onlineStatus.setVisibility(View.VISIBLE);
                lastSeen="online";
                profilePic.setBorderColorStart( Color.CYAN);
                profilePic.setBorderColorEnd( Color.MAGENTA);
//                profilePic.setBorderColorStart(context.getColor(R.color.teal_200));
                onlineStatus.setSelected(false);

            }else{
                String lastSeenDate=user.getLastSeenDate();
                String lastSeenTime=user.getLastSeenTime();
                if (lastSeenDate.equals(getDate())){
                    lastSeen= "Last seen today at "+ lastSeenTime;
                }else{
                    lastSeen= "Last seen on " +lastSeenDate +" at "+ lastSeenTime;
                }
                profilePic.setBorderColorStart(Color.WHITE);
                profilePic.setBorderColorEnd( Color.WHITE);
                onlineStatus.setSelected(true);

            }
            profilePic.setBorderColorDirection(CircularImageView.GradientDirection.LEFT_TO_RIGHT);
            profilePic.setBorderWidth(10);
            if(user.getShowLastSeenState()){
                onlineStatus.setText(lastSeen);
            }

        }


    }
    private void loadSharedPreferenceData() {
        SharedPreferences sharedPreferences=getSharedPreferences("sharedPreferences",MODE_PRIVATE);
        otherUserId=sharedPreferences.getString("otherUserId",null);
        profileUrI=sharedPreferences.getString("profileUrI",null);
        otherUserName=sharedPreferences.getString("otherUserName",null);

    }

    private void saveSharedPreferenceData() {
        SharedPreferences sharedPreferences =getSharedPreferences("sharedPreferences",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("otherUserId",otherUserId);
        editor.putString("profileUrI",profileUrI);
        editor.putString("otherUserName",otherUserName);
        editor.apply();
    }

    private void updateToken(String token) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 =new Token(token);
        reference.child(user.getUid()).setValue(token1);
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
        onlineStatus=findViewById(R.id.onlineStatus);
        typing=findViewById(R.id.typingStatus);
        attachButton= findViewById(R.id.attachButton);
        profilePic=findViewById(R.id.image_profile);
        newMessage=findViewById(R.id.message_container);
        settingsSharedPreferences=getSharedPreferences("Settings",MODE_PRIVATE);
        imageSharedPreferences=getSharedPreferences("selectedImagePref",MODE_PRIVATE);
        progressBar=findViewById(R.id.progressBar2);
        backgroundImageView=findViewById(R.id.backgroundView);
        msgGroupDate=findViewById(R.id.msgGroupDateTop);
        otherUserId=getIntent().getStringExtra("userId");
//        otherUserName=getIntent().getStringExtra("userName");
//        profileUrI =getIntent().getStringExtra("profileUrI");

        if(otherUserId!=null){
            saveSharedPreferenceData();
        }
        context=getApplicationContext();
        userName=findViewById(R.id.userName);
        recyclerView=findViewById(R.id.recyclerView1);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(messageList.size()>0){
                    int firstElementPosition = layoutManager.findFirstVisibleItemPosition();

                    try{

                        if(messageList.get(firstElementPosition).getDate().equals(getDate())){
                            msgGroupDate.setVisibility(View.GONE);
                        }else{
                            msgGroupDate.setVisibility(VISIBLE);
                            msgGroupDate.setText(formatDate(messageList.get(Math.abs(firstElementPosition)).getDate()));
                        }
                    }catch(Exception e){

                    }
                }
            }
        });
    }

    private String formatDate(String date){
        String newDate;
        String month=null;
        switch(date.substring(3,5)){
            case "01":
                month="JAN";
                break;
            case "02":
                month="FEB";
                break;
            case "03":
                month="MAR";
                break;
            case "04":
                month="APR";
                break;
            case "05":
                month="MAY";
                break;
            case "06":
                month="JUNE";
                break;
            case "07":
                month="JULY";
                break;
            case "08":
                month="AUG";
                break;
            case "09":
                month="SEPT";
                break;
            case "10":
                month="OCT";
                break;
            case "11":
                month="NOV";
                break;
            case "12":
                month="DEC";
                break;

        }
        newDate=date.substring(0,2)+"-"+ month+ "-"+date.substring(6,10);
        return newDate;
    }

    private void getOtherUserDetails(String otherUserId) {
        DatabaseReference reference=database.getReference().child("UserDetails").child(otherUserId);
        userViewModel.initOtherUserInfo(otherUserId);
        userViewModel.getOtherUserInfo().observe(this,otherUserInfo ->{
            otherUserName=otherUserInfo.getUserName();
            profileUrI=otherUserInfo.getProfileUrI();
            userName.setText(otherUserName);
            statusCheck(otherUserInfo);

            getTypingStatus(otherUserInfo);
            if(profileUrI!=null){
                Glide.with(getApplicationContext()).load(profileUrI).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;

                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }
                }).into(profilePic);
                profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                   Intent intent =new Intent(getApplicationContext(), ViewImageActivity.class);
//                    intent.putExtra("imageUrI",profileUrI);
//                    startActivity(intent);
                    }
                });

            }else{

                profilePic.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
                profilePic.setCircleColor(Color.WHITE);

            }
        });

    }


    private void getMessages(){
        imageUrIList.clear();
        messageViewModel.getMessages().observe(this, messageListLive -> {
            messageList=messageListLive;
            messageListAdapter.notifyDataSetChanged();
            if (messageList.size() >= 1) {
                recyclerView.scrollToPosition(messageList.size()-1);
            }



        });
//        messageRef.addValueEventListener(new ValueEventListener(){
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                messageList.clear();
//                ArrayList<String> mKeys=new ArrayList<>();
//
//                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
//                    try{
//
//                        messageListModel message = snapshot.getValue(messageListModel.class);
//                        String imageUrI=message.getImageUrI();
//                        message.setMessageId(snapshot.getKey());
//                        String receiver = message.getReceiver();
//                        message.setReceiver(receiver);
//                        if(imageUrI!=null){
//                            imageUrIList.add(imageUrI);
//                        }
//
//                        messageList.add(message);
//                        if (messageList.size() >= 1) {
//                            recyclerView.scrollToPosition(messageList.size()-1);
//                        }
//                        mKeys.add(snapshot.getKey());
//                        if(message.getReceiver()!=null){
////                            otherUserMRef.addValueEventListener(new ValueEventListener() {
////                                @Override
////                                public void onDataChange(@NonNull DataSnapshot otherSnapshot) {
////                                    for(DataSnapshot s:otherSnapshot.getChildren()){
////
////                                        if(mKeys.contains(s.getKey())){
////                                            HashMap<String,Object> messageStatus=new HashMap<>();
////                                            messageStatus.put("checked",true);
////                                            otherUserMRef.child(s.getKey()).updateChildren(messageStatus);
////                                        }
////                                    }
////
////                                }
////
////                                @Override
////                                public void onCancelled(@NonNull DatabaseError error) {
////
////                                }
////                            });
//
//                        }
//
//
//
//                    }catch(Exception e){
//                        Log.d("error1",e.getMessage());
//                    }
//
//                    messageListAdapter.notifyDataSetChanged();
//                }
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK && data!=null) {
            selected=data.getData();

            if (requestCode == IMAGEREQUEST) {

                Intent intent = new Intent(getApplicationContext(), sendImage.class)
                        .putExtra("imageUrI", selected.toString())
                        .putExtra("otherUserId", otherUserId)
                        .putExtra("otherUserName", otherUserName);
                startActivity(intent);
//            try{
//                uploadFile(user.getUid(),otherUserId);
//            }catch(Exception e){
//                Log.d("error",e.getLocalizedMessage());
//            }

            } else if (requestCode == VIDEOREQUEST) {
                Intent intent = new Intent(getApplicationContext(), sendVideo.class)
                        .putExtra("videoUrI", selected.toString())
                        .putExtra("otherUserId", otherUserId)
                        .putExtra("otherUserName", otherUserName);
                startActivity(intent);
            } else if (requestCode == AUDIOREQUEST) {
                uploadAudio(user.getUid(),otherUserId,selected);
//                Intent intent = new Intent(getApplicationContext(), sendAudio.class)
//                        .putExtra("audioUrI", selected.toString())
//                        .putExtra("otherUserId", otherUserId)
//                        .putExtra("otherUserName", otherUserName);
//                startActivity(intent);
            }
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    private void uploadAudio(String userId,String otherUserId,Uri uri){
        if(uri!=null){
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setAudioUrI(uri.toString());
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType("AUDIO");
            message.setReceiver(otherUserId);
            String messageKey=fUserChatRef.getKey();
            fUserChatRef.setValue(message);
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis()
                    + getMimeType(context,uri));
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setAudioUrI(downloadUri.toString());
                    messageOtherUser.setTime(getTime());
                    messageOtherUser.setDate(getDate());
                    messageOtherUser.setType("AUDIO");
                    messageOtherUser.setReceiver(otherUserId);
                    DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                    messageRef.child(messageKey).setValue(messageOtherUser);
                }
            });
        }

    }

    private void uploadVideo(String userId,String otherUserId,Uri uri,String caption){
        if (uri != null) {
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setText(caption);
            message.setVideoUrI(uri.toString());
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType("VIDEO");
            message.setReceiver(otherUserId);
            String messageKey=fUserChatRef.getKey();
            fUserChatRef.setValue(message);
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis() + getMimeType(context,uri));
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setVideoUrI(downloadUri.toString());
                    messageOtherUser.setTime(getTime());
                    messageOtherUser.setText(caption);
                    messageOtherUser.setDate(getDate());
                    messageOtherUser.setType("VIDEO");
                    messageOtherUser.setReceiver(otherUserId);

                    try{
                        DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                        messageRef.child(messageKey).setValue(messageOtherUser);


                    }catch(Exception e){
                        Log.d("error",e.getLocalizedMessage());

                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(String userId, String otherUserId,Uri uri,String caption){
        if (uri != null) {
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setText(caption);
            message.setImageUrI(uri.toString());
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType("IMAGE");
            message.setReceiver(otherUserId);
            String messageKey=fUserChatRef.getKey();
            fUserChatRef.setValue(message);
            StorageReference fileReference;
            fileReference = mStorageReference.child(System.currentTimeMillis() +  getMimeType(context,uri));
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setImageUrI(downloadUri.toString());
                    messageOtherUser.setTime(getTime());
                    messageOtherUser.setText(caption);
                    messageOtherUser.setDate(getDate());
                    messageOtherUser.setType("IMAGE");
                    messageOtherUser.setReceiver(otherUserId);
                    try{
                        DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                        messageRef.child(messageKey).setValue(messageOtherUser);
                    }catch(Exception e){
                        Log.d("error",e.getLocalizedMessage());

                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile(String userId, String otherUserId,Uri UrI,String caption,String type) {
        if (UrI != null) {
            DatabaseReference myRef = database.getReference();
            DatabaseReference fUserChatRef= myRef.child("chats").child(userId).child(otherUserId).push();
            messageListModel message=new messageListModel();
            message.setText(caption);
            if(type.equals("IMAGE")){
                message.setImageUrI(UrI.toString());
            }else if(type.equals("VIDEO")){
                message.setVideoUrI(UrI.toString());
            }else{
                message.setAudioUrI(UrI.toString());
            }
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType(type);
            message.setReceiver(otherUserId);
            String messageKey=fUserChatRef.getKey();
            fUserChatRef.setValue(message);
            StorageReference fileReference;
            if(type.equals("IMAGE")){
                fileReference = mStorageReference.child(System.currentTimeMillis()
                        + ".jpg");
            }else if(type.equals("VIDEO")){
                fileReference = mStorageReference.child(System.currentTimeMillis()
                        + ".mp4");
            }else {
                 fileReference = mStorageReference.child(System.currentTimeMillis()
                        + ".mp3");
            }
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    messageListModel messageOtherUser=new messageListModel();
                    messageOtherUser.setImageUrI(downloadUri.toString());
                    if(type.equals("IMAGE")){
                        messageOtherUser.setImageUrI(downloadUri.toString());
                    }else if(type.equals("VIDEO")){
                        messageOtherUser.setVideoUrI(downloadUri.toString());
                    }else if(type.equals("AUDIO")){
                        messageOtherUser.setAudioUrI(downloadUri.toString());
                    }
                    messageOtherUser.setTime(getTime());
                    messageOtherUser.setText(caption);
                    messageOtherUser.setDate(getDate());
                    messageOtherUser.setType(type);
                    messageOtherUser.setReceiver(otherUserId);

                    try{
                        DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                        messageRef.child(messageKey).setValue(messageOtherUser);


                    }catch(Exception e){
                        Log.d("error",e.getLocalizedMessage());

                    }
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                            @Override
//                            public void onGenerated(@Nullable Palette palette) {
//                                if(palette!=null){
//                                    Palette.Swatch vibrantSwatch = palette.getMutedSwatch();
//                                    if(vibrantSwatch != null){
//                                        int backgroundColor= vibrantSwatch.getRgb();
//
//                                    }
//                                }
//                            }
//                        });
//
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
        menuInflater.inflate(R.menu.chat_menu,menu);
        profileDetails=menu.findItem(R.id.profileButton);
        settings=menu.findItem(R.id.settingsButton);
        delete=menu.findItem(R.id.delete);
        profileDetails.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ChatActivity.this , UserProfileActivity.class).putExtra("userName",otherUserName);
                startActivity(intent);

                return false;
            }
        });
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ChatActivity.this , SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });

        return true;

    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

        if(layoutActions.getVisibility()==VISIBLE){
            layoutActions.setVisibility(GONE);
            textArea.setVisibility(VISIBLE);
        }else{
            Intent mainIntent = new Intent(ChatActivity.this , MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            finish();
        }

    }

    @Override
    public void onItemClick(int position) {



    }

    @Override
    public void onLongItemClick(int position) {
//        Intent intent =new Intent(context, ViewImageActivity.class)
//                .putExtra("position",position)
//                .putExtra("imageList", imageUrIList)
//                .putExtra("imageUrI", messageList.get(position).getImageUrI())
//                .putExtra("profileUrI", messageList.get(position).getProfileUrI())
//                .putExtra("userId",otherUserId)
//                .putExtra("userName",otherUserName);
//        Log.d("position",String.valueOf(position));
//
//        if(messageList.get(position).getReceiver()==user.getUid()){
//            intent.putExtra("Direction","from");
//        }else{
//            intent.putExtra("Direction","to");
//        }
//        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatus(){
        DatabaseReference userDetailRef=database.getReference().child("UserDetails").child(user.getUid());
        Map<String ,Object> onlineStatus=new HashMap<>();
        onlineStatus.put("online",true);
        userDetailRef.updateChildren(onlineStatus);

        Map<String ,Object> lastSeenStatus=new HashMap<>();
        lastSeenStatus.put("lastSeenDate",getDate());
        lastSeenStatus.put("lastSeenTime",getTime());
        lastSeenStatus.put("online",false);
        userDetailRef.onDisconnect().updateChildren(lastSeenStatus);
    }



}