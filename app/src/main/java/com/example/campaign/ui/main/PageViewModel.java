package com.example.campaign.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campaign.Model.Users;
import com.example.campaign.Model.chatList;

import java.util.ArrayList;
import java.util.List;

import Repository.Repo;

public class PageViewModel extends ViewModel {
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private MutableLiveData<ArrayList<chatList>> chatsList;
    private MutableLiveData<ArrayList<Users>> usersList;

    public void initChats(){
        if (chatsList!=null){
            return;
        }
        chatsList= Repo.getInstance().getChatList();
    }
    public void initContacts(List<String> contacts){
        if (usersList!=null){
            return;
        }
        usersList= Repo.getInstance().getUsersList(contacts);
    }
    public LiveData<ArrayList<chatList>> getChatData(){
        return chatsList;

    }
    public LiveData<ArrayList<Users>> getUsersData(){
        return usersList;

    }


    public void setIndex(int index) {
        mIndex.setValue(index);
    }



//    public LiveData<chatList> getChatListObj(){
//        getChatList();
//        return chat_List;
//    }
//    private void getChatList(){
//        database = FirebaseDatabase.getInstance();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        chatUserIds=new ArrayList<>();
//        chatUserNames=new ArrayList<>();
//        chatUserIds.clear();
//        DatabaseReference chatUserIdRef=database.getReference().child("chats").child(user.getUid());
//        DatabaseReference chatUserNameRef=database.getReference().child("UserDetails");
//        chatUserIdRef.addValueEventListener(new ValueEventListener(){
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                    String userId= Objects.requireNonNull(snapshot.getKey());
//                    chatUserIdRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener(){
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                                try{
//                                    messageListModel m=dataSnapshot.getValue(messageListModel.class);
//                                    String text=m.getText();
//                                    String imageUrI=m.getImageUrI();
//                                    if(text!=null){
//                                        if (text.length()>30){
//                                            String i=text.substring(0,30);
//                                            lastMessage=i+"...";
//                                        } else{
//                                            lastMessage=text;
//                                        }
//                                    }
//                                    if(imageUrI!=null){
//                                        lastMessage="photo";
//                                    }
//                                    date=m.getDate();
//                                    time=m.getTime();
//                                }catch(Exception e){
//                                    Log.d("error",e.getMessage());
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//
//                    if(chatUserIds.contains(userId)){
//                    }
//                    else{
//                        chatUserIds.add(userId);
//                        chatUserNameRef.child(userId).addValueEventListener(new ValueEventListener(){
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                chatsListModel user=dataSnapshot.getValue(chatsListModel.class);
//                                String userName=user.getName();
//                                String profileUrl=user.getProfileUrI();
//                                chatUserNames.add(userName);
//                                int i= Collections.frequency(chatUserNames,userName);
//                                if(i<=1){
//                                    try{
//                                        LocalDateTime myDateObj = LocalDateTime.now();
//                                        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                                        String formattedDate = myDateObj.format(dateObj);
//                                        if(formattedDate.equals(date)){
//                                            chatListObj =new chatList(userId,userName,lastMessage,time,profileUrl);
//                                        }else{
//                                            chatListObj =new chatList(userId,userName,lastMessage,date,profileUrl);
//                                            System.out.println("before");
//                                        }
//
//                                    }catch(Exception e){
//                                        Log.d("error",e.getLocalizedMessage());
//                                    }
//
//                                }
//                                chat_List=Transformations.map(mIndex, new Function<Integer,chatList>(){
//
//                                    @Override
//                                    public chatList  apply(Integer input) {
//                                        getChatList();
//                                        return chatListObj;
//
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
}