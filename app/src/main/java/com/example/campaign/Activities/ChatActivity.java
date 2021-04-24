package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.EditText;
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


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class ChatActivity extends AppCompatActivity implements RecyclerViewInterface {

    private String otherUserId, message, profileUrI,messageStatus,messageId,otherUserName,lastSeen;
    private FirebaseDatabase database;
    private List<messageListModel> messageList = new ArrayList<>();
    private RecyclerView recyclerView ;
    private ImageButton sendButton,attachButton;
    private TextView userName,lastSeenMessage,onlineStatus,typing,msgGroupDate;
    private EditText newMessage;
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
    private ImageView backgroundImageView;
    private SharedPreferences imageSharedPreferences;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> imageUrIList=new ArrayList<>();
    private ArrayList<messageListModel> selectedMessages = new ArrayList<>();
    boolean notify=false;

    MessageViewModel messageViewModel;
    UserViewModel userViewModel;

    MenuItem profileDetails;
    MenuItem settings,delete;

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

        mStorageReference=firebaseStorage.getReference();
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        userViewModel=new ViewModelProvider(this).get(UserViewModel.class);
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


        getCurrentWallpaper();



        try{
            getMessages();
        }catch (Exception e){
            Log.d("Error" ,e.getLocalizedMessage());
        }


        sendButton.setOnClickListener(view -> {
            Log.d("userId",user.getUid()+ otherUserId);
            DatabaseReference sMessage_1=database.getReference().child("chats").child(otherUserId).child(user.getUid()).push();
            DatabaseReference sMessage_2=database.getReference().child("chats").child(user.getUid()).child(otherUserId);

            message=newMessage.getText().toString();
//            updateToken(FirebaseInstanceId.getInstance().getToken());
//            apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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
//                    sendNotification(otherUserId,otherUserName,message);
                }
                newMessage.setText("");
            }

        });

        attachButton.setOnClickListener(view ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        });

    }

//    private void sendNotification(String otherUserId, String otherUserName, String message) {
//        DatabaseReference tokens=database.getReference("Tokens");
//        Query query=tokens.orderByKey().equalTo(otherUserId);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
//                    Token token=dataSnapshot.getValue(Token.class);
//                    Data data=new Data(user.getUid(),R.drawable.background_icon,otherUserName+ ":" +message,otherUserId,"New message");
//                    Sender sender = new Sender(data,token.getToken());
//                    apiService.sendNotification(sender)
//                            .enqueue(new Callback<MyResponse>(){
//
//                                @Override
//                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                                    if(response.code()==200){
//                                        if(response.body().success==1){
//                                            showToast("failed");
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<MyResponse> call, Throwable t) {
//
//                                }
//                            });
//                    notify=false;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void setTypingStatus(){
        DatabaseReference typingRef=database.getReference().child("UserDetails").child(user.getUid());
        HashMap<String,Object> typingStatus= new HashMap<>();
        newMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (count>=1){
                    typingStatus.put("Typing",true);

                }else{
                    typingStatus.put("Typing",false);

                }
                typingRef.updateChildren(typingStatus);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count>=1){
                    typingStatus.put("Typing",true);

                }else{
                    typingStatus.put("Typing",false);

                }
                typingRef.updateChildren(typingStatus);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void getCurrentWallpaper(){

        userViewModel.initFUserInfo();
        userViewModel.getFUserInfo().observe(this,user ->{
            String chatWallpaperUrI=user.getChatWallpaper();
            int blur=user.getChatBlur();
            if(chatWallpaperUrI!=null){
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext())
                        .load(chatWallpaperUrI)
                        .transform(new BlurTransformation(blur))
                        .addListener(new RequestListener<Drawable>() {
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
                        })
                        .into(backgroundImageView)
                ;
            }
        });

    }
    private void getTypingStatus(userModel user){
        if(user.getTyping()!=null){
            if(user.getTyping()){
//                            onlineStatus.setVisibility(View.VISIBLE);
                onlineStatus.setVisibility(GONE);
                typing.setVisibility(View.VISIBLE);

            }else{
                onlineStatus.setVisibility(View.VISIBLE);
                typing.setVisibility(View.GONE);
            }

        }

    }
    private void statusCheck(userModel user){
        if(user.getOnline()!=null){
            if(user.getOnline()){
//                            onlineStatus.setVisibility(View.VISIBLE);
                lastSeen="online";
                profilePic.setBorderColorStart( Color.CYAN);
                profilePic.setBorderColorEnd( Color.MAGENTA);
                onlineStatus.setSelected(false);

            }else{
                String lastSeenDate=user.getLastSeenDate();
                String lastSeenTime=user.getLastSeenTime();
                if (lastSeenDate.equals(getDate())){
                    lastSeen= "Last seen today at "+ lastSeenTime;
                }else{
                    lastSeen= "Last seen on " +lastSeenDate +" at "+ lastSeenTime;
                }
                profilePic.setBorderColorStart(context.getColor(R.color.white));
                profilePic.setBorderColorEnd( Color.WHITE);
                onlineStatus.setSelected(true);



            }
            profilePic.setBorderColorDirection(CircularImageView.GradientDirection.LEFT_TO_RIGHT);
            profilePic.setBorderWidth(10);
            onlineStatus.setText(lastSeen);
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
                            msgGroupDate.setVisibility(View.VISIBLE);
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
        if(requestCode==2 && resultCode== Activity.RESULT_OK && data!=null){
            selected=data.getData();
            Intent intent=new Intent(getApplicationContext(),sendImage.class)
                    .putExtra("imageUrI",selected.toString())
                    .putExtra("otherUserId",otherUserId)
                    .putExtra("otherUserName",otherUserName);
            startActivity(intent);
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
        selected=Uri.parse(imageSharedPreferences.getString("imageUrI",null));
        String caption=imageSharedPreferences.getString("caption",null);
        Log.d("HSCSHCD","done");
        if (selected != null) {
            DatabaseReference myRef = database.getReference();
            messageListModel message=new messageListModel();
            message.setText(caption);
            message.setImageUrI(selected.toString());
            message.setTime(getTime());
            message.setDate(getDate());
            message.setType("IMAGE");
            message.setReceiver(otherUserId);
            DatabaseReference messageRef= myRef.child("chats").child(userId).child(otherUserId).push();
            String messageKey=messageRef.getKey();
            messageRef.setValue(message);
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + ".jpg");
            UploadTask uploadTask =fileReference.putFile(selected);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@Nullable Palette palette) {
                                if(palette!=null){
                                    Palette.Swatch vibrantSwatch = palette.getMutedSwatch();
                                    if(vibrantSwatch != null){
                                        int backgroundColor= vibrantSwatch.getRgb();
                                        DatabaseReference myRef = database.getReference();
                                        messageListModel message=new messageListModel();

                                        assert downloadUri != null;
                                        message.setBackgroundColor(backgroundColor);
                                        message.setImageUrI(downloadUri.toString());
                                        message.setTime(getTime());
                                        if(caption!=null){
                                            message.setText(caption);
                                        }
                                        message.setDate(getDate());
                                        message.setType("IMAGE");
                                        message.setReceiver(otherUserId);

                                        try{
                                            myRef.child("chats").child(userId).child(otherUserId).push().setValue(message);
                                           DatabaseReference messageRef= myRef.child("chats").child(otherUserId).child(userId);
                                           messageRef.child(messageKey).setValue(message);

                                        }catch(Exception e){
                                            Log.d("error",e.getLocalizedMessage());
                                            progressBar.setVisibility(GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                imageSharedPreferences.edit().clear();
                SharedPreferences.Editor editor = imageSharedPreferences.edit();
                editor.clear();
                editor.commit();




            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress=(100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());

                }
            });
//        selected=null;
        imageSharedPreferences.edit().clear();
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
        Intent mainIntent = new Intent(ChatActivity.this , MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
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