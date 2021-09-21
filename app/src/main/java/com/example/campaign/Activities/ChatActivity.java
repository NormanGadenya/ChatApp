package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.view.MenuInflater;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Common.Tools;
import com.example.campaign.Common.ServiceCheck;
import com.example.campaign.Interfaces.APIService;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.MessageViewModel;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.Notifications.Client;
import com.example.campaign.Notifications.Data;
import com.example.campaign.Notifications.MyResponse;
import com.example.campaign.Notifications.Sender;
import com.example.campaign.Notifications.Token;
import com.example.campaign.R;
import com.example.campaign.Services.AudioUploadService;
import com.example.campaign.Services.ImageUploadService;
import com.example.campaign.Services.VideoUploadService;
import com.example.campaign.Services.updateStatusService;
import com.example.campaign.adapter.messageListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChatActivity extends AppCompatActivity implements RecyclerViewInterface {

    private String otherUserId, message, profileUrI,otherUserName,lastSeen,fUserName;
    private FirebaseDatabase database;
    private ArrayList<messageListModel> messageList = new ArrayList<>();
    private RecyclerView recyclerView ;
    private ImageButton sendButton,attachButton,emojiButton;
    private TextView userName,onlineStatus,typing,msgGroupDate;
    private EmojiconEditText newMessage;
    private CircularImageView profilePic;
    private FirebaseUser user ;
    private FirebaseStorage firebaseStorage;
    private Uri selected;
    private MenuInflater menuInflater;
    private ProgressBar progressBar;
    private messageListAdapter messageListAdapter;
    private ImageView backgroundImageView,onlineStatusView;
    private SharedPreferences settingsSharedPreferences;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> imageUrIList=new ArrayList<>();
    boolean notify=false;
    private FloatingActionButton imageButton,videoButton,audioButton;
    private MessageViewModel messageViewModel;
    private UserViewModel userViewModel;
    final int IMAGEREQUEST =2;
    final int AUDIOREQUEST=3;
    final int VIDEOREQUEST=4;
    MenuItem profileDetails,settings,delete;
    View rootView,layoutActions,textArea;
    private APIService apiService;
    private MediaPlayer mediaPlayer;
    private String uploadImageTId,uploadVideoTId,uploadAudioTId;
    private int uploadImageTP,uploadVideoTP,uploadAudioTP;
    private  ActionBar actionBar;
    private Map<String ,Integer> uploadImageData=new HashMap<>();
    private Map<String ,Integer> uploadVideoData=new HashMap<>();
    private Map<String ,Integer> uploadAudioData=new HashMap<>();
    private String date,time;
    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user= FirebaseAuth.getInstance().getCurrentUser();
        tools = new Tools();
        date=tools.getDate();
        time=tools.getTime();
        tools.context=getApplicationContext();
        InitialiseControllers();
        loadUserDetails();
        setTypingStatus();
        layoutManager= new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        messageViewModel.initChats(otherUserId);
        userViewModel.initFUserInfo();
        userViewModel.getFUserInfo().observe(this,user->{
            fUserName=user.getUserName();
        });
        serviceCheck();
        messageList=messageViewModel.getMessages().getValue();
        loadAdapter();

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
            startActivityForResult(intent,AUDIOREQUEST);

        });
        newMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0){
                    attachButton.setVisibility(GONE);
                }else{
                    attachButton.setVisibility(VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getCurrentWallpaper();
        try{
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    getMessages();
                }
            });

        }catch (Exception e){
            Log.d("Error" ,e.getLocalizedMessage());
        }
        String imageUrI=getIntent().getStringExtra("imageUrI");
        String caption=getIntent().getStringExtra("caption");
        String videoUrI=getIntent().getStringExtra("videoUrI");
        if(imageUrI!=null){
            if(tools.checkInternetConnection()){
                uploadImage(imageUrI,caption);
            }else{
                Toast.makeText(this,"No internet connection",Toast.LENGTH_SHORT).show();
            }

        }else if(videoUrI!=null){
            if(tools.checkInternetConnection()){
                uploadVideo(videoUrI,caption);
            }else{
                Toast.makeText(this,"No internet connection",Toast.LENGTH_SHORT).show();
            }

        }
        sendButton.setOnClickListener(view -> {
            if(tools.checkInternetConnection()){
                uploadTextMessage();
            }else{
                Toast.makeText(this,"No internet connection",Toast.LENGTH_SHORT).show();
            }
        });



        attachButton.setOnClickListener(view ->{
            textArea.setVisibility(GONE);
            layoutActions.setVisibility(VISIBLE);
            layoutActions.animate().alpha(1.0f).setDuration(1000);
        });


    }
    private void loadAdapter(){
        if(messageList!=null){
            messageListAdapter=new messageListAdapter();
            messageListAdapter.setMContext(ChatActivity.this);
            messageListAdapter.setMessageList(messageList);
            messageListAdapter.setActivity(this);
            messageListAdapter.setRecyclerViewInterface(this);
            messageListAdapter.setOtherUserId(otherUserId);
            messageListAdapter.uploadImageTask=uploadImageData;
            messageListAdapter.uploadVideoTask=uploadVideoData;
            recyclerView.setAdapter(messageListAdapter);
        }

    }

    private void loadUserDetails(){
        if(otherUserId==null){
            loadSharedPreferenceData();
        }
        if(otherUserId!=null){
            getOtherUserDetails(otherUserId);
        }
    }

    private void uploadVideo(String videoUrI,String caption){
        Intent intent =new Intent(this, VideoUploadService.class);
        ResultReceiver myResultReceiver=new MyReceiver(null);
        intent.putExtra("fUserName",fUserName)
                .putExtra("uri",videoUrI)
                .putExtra("userId",user.getUid())
                .putExtra("otherUserId",otherUserId)
                .putExtra("caption",caption)
                .putExtra("receiver",myResultReceiver);
        startService(intent);
    }

    private void uploadImage(String imageUrI,String caption){
        Intent intent =new Intent(this, ImageUploadService.class);
        ResultReceiver myResultReceiver=new MyReceiver(null);
        intent.putExtra("fUserName",fUserName)
                .putExtra("uri",imageUrI)
                .putExtra("userId",user.getUid())
                .putExtra("otherUserId",otherUserId)
                .putExtra("caption",caption)
                .putExtra("receiver",myResultReceiver);

        startService(intent);
    }

    private void uploadTextMessage(){
        DatabaseReference sMessage_1=database.getReference().child("chats").child(otherUserId).child(user.getUid()).push();
        DatabaseReference sMessage_2=database.getReference().child("chats").child(user.getUid()).child(otherUserId);
        message=newMessage.getText().toString();
        updateToken(FirebaseInstanceId.getInstance().getToken());
        apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);
        if(!message.equals(null)){
            String formattedDate = date;
            String formattedTime=time;
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
                sendNotification(otherUserId,fUserName,message);
            }
            newMessage.setText("");
        }
    }

    private void serviceCheck(){

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        ServiceCheck serviceCheck=new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();
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

                                        }else{

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

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

   }

    private void getTypingStatus(userModel user){
        if(user.getTyping()!=null){
            if(user.getTyping()){
                onlineStatus.setVisibility(GONE);
                typing.setVisibility(VISIBLE);

            }else{
                onlineStatus.setVisibility(VISIBLE);
                typing.setVisibility(View.GONE);
            }

        }

    }

    private void statusCheck(userModel user){

        if(user.getOnline()!=null && user.getShowOnlineState()!=null & user.getShowLastSeenState()!=null){
            if(user.getOnline() && user.getShowOnlineState()){
//                            onlineStatus.setVisibility(View.VISIBLE);
                lastSeen="online";
                onlineStatusView.setVisibility(VISIBLE);
//                profilePic.setBorderColorStart( Color.CYAN);
//                profilePic.setBorderColorEnd( Color.MAGENTA);
//                profilePic.setBorderColorStart(context.getColor(R.color.teal_200));
                onlineStatus.setSelected(false);

            }else{
                onlineStatusView.setVisibility(GONE);
                String lastSeenDate=user.getLastSeenDate();
                String lastSeenTime=user.getLastSeenTime();
                if (lastSeenDate.equals(date)){
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
        actionBar=getSupportActionBar();
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
        progressBar=findViewById(R.id.progressBar2);
        backgroundImageView=findViewById(R.id.backgroundView);
        msgGroupDate=findViewById(R.id.msgGroupDateTop);
        onlineStatusView=findViewById(R.id.onlineStatusView);
        otherUserId=getIntent().getStringExtra("userId");
        if(otherUserId!=null){
            saveSharedPreferenceData();
        }
        userName=findViewById(R.id.userName);
        recyclerView=findViewById(R.id.recyclerView1);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(messageList.size()>0){
                    int firstElementPosition = layoutManager.findFirstVisibleItemPosition();
                    try{

                        if(messageList.get(firstElementPosition).getDate().equals(new Tools().getDate())){
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
        database = FirebaseDatabase.getInstance();
        firebaseStorage= FirebaseStorage.getInstance();
        emojiButton=findViewById(R.id.emoji_button);
        rootView=findViewById(R.id.constraint_layout2);
        layoutActions=findViewById(R.id.layout_actions);
        imageButton=findViewById(R.id.att_image);
        videoButton=findViewById(R.id.att_vid);
        audioButton=findViewById(R.id.att_audio);
        textArea=findViewById(R.id.constraint_layout2);
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        userViewModel=new ViewModelProvider(this).get(UserViewModel.class);
        EmojIconActions emojIcon=new EmojIconActions(getApplicationContext(),rootView,newMessage,emojiButton,"#495C66","#DCE1E2","#0B1830");
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojIcon.ShowEmojIcon();
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
                   Intent intent =new Intent(getApplicationContext(), OtherUserActivity.class);
                    intent.putExtra("otherUserId",otherUserId);
                    startActivity(intent);
                    }
                });

            }else{
                progressBar.setVisibility(GONE);
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
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK && data!=null) {
            selected=data.getData();
            try{
                if(tools.isFileLessThan2MB(selected)) {
                    if (requestCode == IMAGEREQUEST) {
                        Intent intent = new Intent(getApplicationContext(), sendImage.class)
                                .putExtra("imageUrI", selected.toString())
                                .putExtra("otherUserId", otherUserId)
                                .putExtra("otherUserName", otherUserName);
                        startActivity(intent);
                    } else if (requestCode == VIDEOREQUEST) {
                        Intent intent = new Intent(getApplicationContext(), sendVideo.class)
                                .putExtra("videoUrI", selected.toString())
                                .putExtra("otherUserId", otherUserId)
                                .putExtra("otherUserName", otherUserName);
                        startActivity(intent);
                    } else if (requestCode == AUDIOREQUEST) {
                        Intent intent = new Intent(this, AudioUploadService.class);
                        String uri = selected.toString();
                        ResultReceiver myResultReceiver = new MyReceiver(null);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(getApplicationContext(), selected);
                        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        intent.putExtra("fUserName", fUserName)
                                .putExtra("uri", uri)
                                .putExtra("receiver", myResultReceiver)
                                .putExtra("userId", user.getUid())
                                .putExtra("otherUserId", otherUserId)
                                .putExtra("audioDuration", duration);
                        startService(intent);
                    }
                }else{
                    Toast.makeText(this,"file size too large",Toast.LENGTH_SHORT).show();
                    rootView.setVisibility(View.GONE);
                }
            }catch(Exception e){

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Intent intent = new Intent(ChatActivity.this , UserProfileActivity.class)
                        .putExtra("userName",otherUserName);
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
        Intent i= new Intent();
        if(messageList.get(position).getImageUrI()!=null){
            i=new Intent(this,ViewImageActivity.class);
            i.putExtra("imageUrI",messageList.get(position).getImageUrI());
        }
        else if ((messageList.get(position).getVideoUrI())!=null){
            i=new Intent(this,ViewVideoActivity.class);
            i.putExtra("videoUrI",messageList.get(position).getVideoUrI());
        }
        i.putExtra("otherUserName",otherUserName);
        i.putExtra("caption",messageList.get(position).getText());

        if(messageList.get(position).getReceiver().equals(user.getUid())){
            i.putExtra("direction","from");
        }else{
            i.putExtra("direction","to");
        }
        if(messageList.get(position).getImageUrI()!=null || messageList.get(position).getVideoUrI()!=null && otherUserName!=null){
            startActivity(i);
        }
    }

    @Override
    public void onLongItemClick(int position) {}

    @Override
    public void getMediaPlayer(MediaPlayer mp) {
        mediaPlayer=mp;

    }

    public class MyReceiver extends ResultReceiver{
        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public MyReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

                switch (resultCode){
                    case 100:
                        uploadImageTId=resultData.getString("uploadImageTId");
                        uploadImageTP=resultData.getInt("uploadImagePercentage");
                        uploadImageData.remove(null);
                        uploadImageData.put(uploadImageTId,uploadImageTP);
                        messageListAdapter.notifyDataSetChanged();

                        break;
                    case 200:
                        uploadVideoTId=resultData.getString("uploadVideoTId");
                        uploadVideoTP=resultData.getInt("uploadVideoPercentage");
                        uploadVideoData.remove(null);
                        uploadVideoData.put(uploadVideoTId,uploadVideoTP);
                        messageListAdapter.notifyDataSetChanged();
                        break;
                    case 300:
                        uploadAudioTId=resultData.getString("uploadAudioTId");
                        uploadAudioTP=resultData.getInt("uploadAudioPercentage");
                        uploadAudioData.remove(null);
                        uploadAudioData.put(uploadAudioTId,uploadAudioTP);
                        messageListAdapter.notifyDataSetChanged();
                }

        }
    }

    public Map<String,Integer> getUploadImageTaskData(){

        uploadImageData.put(uploadImageTId,uploadImageTP);

        return uploadImageData;
    }
    public Map<String,Integer> getUploadVideoTaskData(){

        uploadVideoData.put(uploadVideoTId,uploadVideoTP);

        return uploadVideoData;
    }
    public Map<String,Integer> getUploadAudioTaskData(){

        uploadAudioData.put(uploadAudioTId,uploadAudioTP);

        return uploadAudioData;
    }


}