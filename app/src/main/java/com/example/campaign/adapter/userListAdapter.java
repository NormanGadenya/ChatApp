package com.example.campaign.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Model.Users;
import com.example.campaign.R;
import com.example.campaign.chatActivity;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.Holder> {
    private List<Users> list;
    private Context context;

    public userListAdapter(List<Users> list,Context context){
        this.context = context;
        this.list=list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Users userList=list.get(position);
        holder.userName.setText(userList.getName());
        holder.phoneNumber.setText(userList.getPhoneNumber());
        System.out.println(userList.getProfileUrl());

        try{
            if (userList.getProfileUrl().equals("")){
                holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);  // set  default image when profile user is null
            } else {
                Glide.with(context).load(userList.getProfileUrl()).into(holder.profile);
            }
        }catch(Exception e){

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, chatActivity.class)
                        .putExtra("userID",userList.getUserId())
                        .putExtra("userName",userList.getName())
                        .putExtra("userProfile",userList.getProfileUrl())

                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView userName, phoneNumber;
        private CircularImageView profile;
        public Holder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.userName);
            phoneNumber=itemView.findViewById(R.id.phoneNumber);
            profile=itemView.findViewById(R.id.image_profile);
        }
    }
}
