package com.example.campaign.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.example.campaign.chatActivity;
import com.example.campaign.image_act;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;


public class messageListAdapter extends RecyclerView.Adapter<messageListAdapter.Holder> {
    private List<messageListModel> list;
    private Context context;
    private View view;
    private int count=0;
    private FirebaseUser user;
    private String profileUrI;
    private List<String> images=new ArrayList<>();;

    public messageListAdapter(List<messageListModel> list, Context context,String profileUrI) {
        this.list = list;
        this.context = context;
        this.profileUrI=profileUrI;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final messageListModel messageList=list.get(count);
        user= FirebaseAuth.getInstance().getCurrentUser();

        if(messageList.getReceiver().equals(user.getUid())){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
        }
        count++;
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final messageListModel messageList = list.get(position);
        images.add(messageList.getImageUrI());
        if(messageList.getText()!=null){
            holder.message.setText(messageList.getText());
            Log.d("text",messageList.getText());
        }
        holder.time.setText(messageList.getTime());
        if (messageList.getMessageStatus()=="read"){
            holder.messageStatus.setImageResource(R.drawable.circle_read);

        }
        else{
            holder.messageStatus.setImageResource(R.drawable.circle_unread);
        }
        try{
            Glide.with(context).load(profileUrI).into(holder.profilePic);
            if (messageList.getImageUrI()!=null  ){
                if(images.contains(messageList.getImageUrI()))
                //Picasso.with(context).load(messageList.getImageUrI()).into(holder.imageView);
                Glide.with(context).load(messageList.getImageUrI()).into(holder.imageView);
                holder.imageView.setVisibility(view.VISIBLE);
                holder.message.setVisibility(view.GONE);
                holder.itemView.setOnClickListener(view->  context.startActivity(new Intent(context, image_act.class)
                        .putExtra("imageUrI",messageList.getImageUrI())
                        )
                );
                Log.d("text",messageList.getImageUrI());

            }

        }catch(Exception e){
            holder.profilePic.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView message,time;
        private CircularImageView profilePic,messageStatus,imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView);
            profilePic=itemView.findViewById(R.id.image_profile);
            message = itemView.findViewById(R.id.show_message);
            messageStatus=itemView.findViewById(R.id.message_status);
            time=itemView.findViewById(R.id.time);
        }
    }
}

