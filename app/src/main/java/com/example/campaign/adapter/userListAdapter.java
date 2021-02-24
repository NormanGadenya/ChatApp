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
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.Activities.chatActivity;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.Holder> {
    private List<userModel> list;
    private Context context;

    public userListAdapter(List<userModel> list, Context context){
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
        final userModel userList=list.get(position);
        holder.userName.setText(userList.getUserName());
        holder.phoneNumber.setText(userList.getPhoneNumber());
        System.out.println(userList.getProfileUrI());

        try{
            if (userList.getProfileUrI().equals("")){
                holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);  // set  default image when profile user is null
            } else {
                Glide.with(context).load(userList.getProfileUrI()).into(holder.profile);
            }
        }catch(Exception e){

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, chatActivity.class)
                        .putExtra("userID",userList.getUserId())
                        .putExtra("userName",userList.getUserName())
                        .putExtra("userProfile",userList.getProfileUrI())

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
