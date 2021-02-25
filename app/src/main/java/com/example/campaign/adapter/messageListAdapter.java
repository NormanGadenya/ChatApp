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
import com.example.campaign.Activities.viewImageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.zolad.zoominimageview.ZoomInImageView;


import java.util.ArrayList;
import java.util.List;


public class messageListAdapter extends RecyclerView.Adapter<messageListAdapter.Holder> {
    private List<messageListModel> list;
    public Context context;
    private View view;
    private int count=0;
    private static final int MESSAGE_LEFT=0;
    private static final int MESSAGE_RIGHT=1;
    private FirebaseUser user;
    private String profileUrI;

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


        try{
            Glide.with(context).load(profileUrI).into(holder.profilePic);

        }catch(Exception e){
            holder.profilePic.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public List<Integer> getNumbersInRange(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(i);
        }
        return result;
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
        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView);
            profilePic=itemView.findViewById(R.id.image_profile);
            message = itemView.findViewById(R.id.show_message);
            messageStatus=itemView.findViewById(R.id.message_status);
            time=itemView.findViewById(R.id.time);


        }
        void bind(final messageListModel messageList){
            switch(messageList.getType()){
                case "TEXT":
                    message.setText(messageList.getText());
                    imageView.setVisibility(itemView.GONE);
                    message.setVisibility(itemView.VISIBLE);
                    time.setText(messageList.getTime());
                    break;
                case "IMAGE":
                    imageView.setVisibility(itemView.VISIBLE);
                    message.setVisibility(itemView.GONE);
                    time.setText(messageList.getTime());
                    Glide.with(context).load(messageList.getImageUrI()).into(imageView);
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

