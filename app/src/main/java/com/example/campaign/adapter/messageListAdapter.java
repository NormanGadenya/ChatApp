package com.example.campaign.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.example.campaign.Activities.viewImageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.zolad.zoominimageview.ZoomInImageView;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;


public class messageListAdapter extends RecyclerView.Adapter<messageListAdapter.Holder> {
    private List<messageListModel> list;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage= FirebaseStorage.getInstance();
    public Context context;
    private View view;


    private static final int MESSAGE_LEFT=0;
    private static final int MESSAGE_RIGHT=1;
    private FirebaseUser user;
    private String profileUrI;

    private Vibrator vibrator;

    public messageListAdapter(List<messageListModel> list, Context context,String profileUrI) {
        this.list = list;
        this.context = context;
        this.profileUrI=profileUrI;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType ==MESSAGE_LEFT){
            View view =LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new Holder(view);
        }else{
            View view =LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new Holder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.bind(list.get(position));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position){
        user= FirebaseAuth.getInstance().getCurrentUser();
        final messageListModel messageList=list.get(position);

        if(messageList.getReceiver().equals(user.getUid())){

            return MESSAGE_LEFT;
        }
        else {
            return MESSAGE_RIGHT;
        }
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView message,time;
        private CircularImageView profilePic,messageStatus;
        private ZoomInImageView imageView;
        private ProgressBar progressBar;

        public Holder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.imageView);
            profilePic=itemView.findViewById(R.id.image_profile);
            message = itemView.findViewById(R.id.show_message);
            messageStatus=itemView.findViewById(R.id.message_status);
            time=itemView.findViewById(R.id.time);
            progressBar=itemView.findViewById(R.id.progressBar);

        }
        void bind(final messageListModel messageList){
            switch(messageList.getType()){
                case "TEXT":
                    message.setText(messageList.getText());
                    imageView.setVisibility(itemView.GONE);
                    message.setVisibility(itemView.VISIBLE);
                    time.setText(messageList.getTime());
                    progressBar.setVisibility(itemView.GONE);
                    message.setOnLongClickListener(new View.OnLongClickListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public boolean onLongClick(View v) {
                            Toast.makeText(context,"itemDeleted",Toast.LENGTH_LONG).show();
                            DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid()).child(messageList.getReceiver());
                            messageRef.child(messageList.getMessageId()).removeValue();
                            vibrator=(Vibrator)context.getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(50);

                            return false;
                        }
                    });
                    break;
                case "IMAGE":
                    //progressBar.setVisibility(itemView.VISIBLE);
                    imageView.setVisibility(itemView.VISIBLE);
                    message.setVisibility(itemView.GONE);
                    imageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public boolean onLongClick(View v) {
                            vibrator=(Vibrator)context.getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(50);
                            Toast.makeText(context,"itemDeleted",Toast.LENGTH_LONG).show();
                            StorageReference imageRef=storage.getReferenceFromUrl(messageList.getImageUrI());
                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DatabaseReference messageRef=database.getReference().child("chats").child(user.getUid()).child(messageList.getReceiver());
                                    messageRef.child(messageList.getMessageId()).removeValue();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,"Something went wrong",Toast.LENGTH_LONG).show();
                                }
                            });



                            return true;
                        }
                    });
                    time.setText(messageList.getTime());
                    Glide.with(context).load(messageList.getImageUrI()).listener(new RequestListener<Drawable>() {
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
                    }).into(imageView);

                    imageView.setOnClickListener(view->  context.startActivity(new Intent(context, viewImageActivity.class)
                                    .putExtra("imageUrI",messageList.getImageUrI())
                                    .putExtra("profileUrI",messageList.getProfileUrI())
                                    .putExtra("userId",messageList.getReceiver())
                                    .putExtra("userName",messageList.getUserName())

                            )

                    );

                    break;

            }
            if (messageList.getMessageStatus()=="read"){
                messageStatus.setImageResource(R.drawable.circle_read);

            }
            else{
                messageStatus.setImageResource(R.drawable.circle_unread);
            }
        }
    }
}

