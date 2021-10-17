package com.neuralBit.letsTalk.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.messageListModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DeleteStorageFile extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String TAG = "DeleteStorageFile";
        FirebaseDatabase database =FirebaseDatabase.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference chatRef=database.getReference().child("chats").child(firebaseUser.getUid());
        DatabaseReference lastMessageRef=database.getReference().child("lastMessage").child(firebaseUser.getUid());
        Bundle b = intent.getExtras();
        Tools tools = new Tools();
        ArrayList<String> deletedChats=b.getStringArrayList("deletedChatsList");
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        for (String otherUserId:deletedChats){
            chatRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                        messageListModel m= dataSnapshot.getValue(messageListModel.class);
                        String reference = null;
                        if (m.getImageUrI() != null) {
                            reference=m.getImageUrI();
                        }else if(m.getVideoUrI()!=null){
                            reference=m.getVideoUrI();
                        }else if(m.getAudioUrI()!=null){
                            reference=m.getAudioUrI();
                        }
                        if(reference != null){
                            try {
                                StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(tools.decryptText(reference));
                                photoRef.delete().addOnSuccessListener(I->{
                                    chatRef.child(otherUserId).child(dataSnapshot.getKey()).removeValue();
                                    lastMessageRef.child(otherUserId).child(dataSnapshot.getKey()).removeValue();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            chatRef.child(otherUserId).child(dataSnapshot.getKey()).removeValue();
                            lastMessageRef.child(otherUserId).child(dataSnapshot.getKey()).removeValue();
                        }




                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        return START_NOT_STICKY;
    }
}
