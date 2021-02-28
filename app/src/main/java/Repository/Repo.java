package Repository;

import android.os.Build;
import android.os.Handler;
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
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database;

    private List<String> chatUserIds;
    private String lastMessage,date,time;
    private chatListModel chatListModelObj;
    private Handler handler=new Handler();

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
        chatUserIds=new ArrayList<>();
        chatUserIds.clear();



        DatabaseReference chatUserIdRef=database.getReference().child("chats").child(firebaseUser.getUid());

        chatUserIdRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatUserIds.clear();

                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    chats_List_Model.clear();
                    String userId= Objects.requireNonNull(snapshot.getKey());
                    chatUserIds.add(userId);

                }
                fetchUserDetails();

//                for (String userId: chatUserIds){
//                    getLastMessage(userId);
//                    fetchUserDetails(userId);
//                    chatList.postValue(chats_List_Model);
//
//                    System.out.println("clecscddddss");
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void  fetchUserDetails() {
        DatabaseReference userDetails = database.getReference().child("UserDetails");


        handler.post(new Runnable() {
            @Override
            public void run() {
                chats_List_Model.clear();
                for (String userId:chatUserIds){

                    userDetails.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            chatListModel user = snapshot.getValue(chatListModel.class);
                            String userName = user.getUserName();
                            String profileUrl = user.getProfileUrI();
                            getLastMessage(userId,userName,profileUrl);



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


    }

    private void getLastMessage(String userId,String userName,String profileUrI) {
        DatabaseReference messageRef=database.getReference().child("chats").child(firebaseUser.getUid());
        handler.post(new Runnable() {
            @Override
            public void run() {
                messageRef.child(userId).addValueEventListener(new ValueEventListener(){
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
            }
        });



    }

    private void loadUsers(List<String> contacts){
        DatabaseReference userDetails=database.getReference().child("UserDetails");

        userDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List <String> phoneNumbersList=new ArrayList<>();
                users_List_Model.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                    userModel userListObj=new userModel();
                    String id=dataSnapshot.getKey();
                    userModel user=dataSnapshot.getValue(userModel.class);
                    String phoneNumber=user.getPhoneNumber();
                    phoneNumbersList.add(phoneNumber);

                    int i= Collections.frequency(phoneNumbersList,phoneNumber);
                    if (contacts!=null && !firebaseUser.getPhoneNumber().equals(phoneNumber)) {
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


