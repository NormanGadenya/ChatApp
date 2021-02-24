package Repository;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.example.campaign.Model.userModel;
import com.example.campaign.Model.chatListModel;

import com.example.campaign.Model.messageListModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;



public class Repo {
    static Repo instance;
    private ArrayList<chatListModel> chats_List_Model=new ArrayList<>();
    private ArrayList<userModel> users_List_Model=new ArrayList<>();

    private FirebaseDatabase database;
    private List<String> chatUserIds,chatUserNames;
    private String lastMessage,date,time;
    private chatListModel chatListModelObj;

    private MutableLiveData<ArrayList<chatListModel>> chatList=new MutableLiveData<>();
    private MutableLiveData<ArrayList<userModel>> usersList=new MutableLiveData<>();


    public static Repo getInstance() {
        if(instance == null){
            instance= new Repo();
        }

        return instance;
    }
    public MutableLiveData<ArrayList<chatListModel>> getChatList(){
        if (chats_List_Model!=null) {
            loadChatList();
            chats_List_Model.clear();
        }
        chatList.setValue(chats_List_Model);
        return chatList;
    }

    public MutableLiveData<ArrayList<userModel>> getUsersList(List<String> contacts){
        if (users_List_Model!=null) {
            loadUsers(contacts);
            users_List_Model.clear();

        }
        usersList.setValue(users_List_Model);

        return usersList;
    }



    private void loadChatList(){
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        chatUserIds=new ArrayList<>();
        chatUserNames=new ArrayList<>();
        chatUserIds.clear();
        DatabaseReference chatUserIdRef=database.getReference().child("chats").child(user.getUid());
        DatabaseReference chatUserNameRef=database.getReference().child("UserDetails");
        chatUserIdRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    String userId= Objects.requireNonNull(snapshot.getKey());
                    chatUserIdRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                try{
                                    messageListModel m=dataSnapshot.getValue(messageListModel.class);
                                    String text=m.getText();
                                    String imageUrI=m.getImageUrI();
                                    if(text!=null){
                                        if (text.length()>30){
                                            String i=text.substring(0,30);
                                            lastMessage=i+"...";
                                        } else{
                                            lastMessage=text;
                                        }
                                    }
                                    if(imageUrI!=null){
                                        lastMessage="photo";
                                    }
                                    date=m.getDate();
                                    time=m.getTime();
                                }catch(Exception e){
                                    Log.d("error",e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    if(chatUserIds.contains(userId)){
                    }
                    else{
                        chatUserIds.add(userId);
                        chatUserNameRef.child(userId).addValueEventListener(new ValueEventListener(){
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                chatListModel user=dataSnapshot.getValue(chatListModel.class);
                                String userName=user.getUserName();
                                String profileUrl=user.getProfileUrI();
                                Log.d("profilePic",profileUrl +userName);
                                chatUserNames.add(userName);
                                int i= Collections.frequency(chatUserNames,userName);
                                if(i<=1){
                                    try{
                                        LocalDateTime myDateObj = LocalDateTime.now();
                                        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                        String formattedDate = myDateObj.format(dateObj);
                                        if(formattedDate.equals(date)){
                                            chatListModelObj =new chatListModel(userId,userName,lastMessage,time,profileUrl);
                                        }else{
                                            chatListModelObj =new chatListModel(userId,userName,lastMessage,date,profileUrl);

                                        }

                                    }catch(Exception e){
                                        Log.d("error",e.getLocalizedMessage());
                                    }

                                }

                                chats_List_Model.add(chatListModelObj);
                                chatList.postValue(chats_List_Model);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUsers(List<String> contacts){
        DatabaseReference userDetails=database.getReference().child("UserDetails");
        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List <String> phoneNumbersList=new ArrayList<>();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    userModel userListObj=new userModel();
                    String id=dataSnapshot.getKey();
                    userModel user=dataSnapshot.getValue(userModel.class);
                    String phoneNumber=user.getPhoneNumber();
                    phoneNumbersList.add(phoneNumber);
                    int i= Collections.frequency(phoneNumbersList,phoneNumber);
                    if (contacts!=null) {
                        if (i <=1 && contacts.contains(phoneNumber)){
                            userListObj.setUserName(user.getUserName());
                            userListObj.setPhoneNumber(user.getPhoneNumber());
                            userListObj.setProfileUrI(user.getProfileUrI());
                            userListObj.setUserId(id);
                            users_List_Model.add(userListObj);
                            usersList.postValue(users_List_Model);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
