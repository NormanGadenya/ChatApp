package com.example.letStalk.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letStalk.Activities.ChatActivity;
import com.example.letStalk.Model.userModel;
import com.example.campaign.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.Holder> implements FastScrollRecyclerView.SectionedAdapter {
    private final List<userModel> list;
    private final Context context;
    public static final String TAG="userListAdapter";
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
//        holder.profile.setBorderColor(context.getColor(R.color.teal_200));

        if(userList.getOnline()){
            holder.onlineStatus.setVisibility(VISIBLE);

        }else{
            holder.onlineStatus.setVisibility(GONE);
        }

        holder.phoneNumber.setText(userList.getPhoneNumber());
        System.out.println(userList.getProfileUrI());

        try{
            if (userList.getProfileUrI()==null){
                holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);  // set  default image when profile user is null
            } else {
                Glide.with(context).load(userList.getProfileUrI()).into(holder.profile);
            }
        }catch(Exception e){
            Log.e(TAG, "onBindViewHolder: ",e.fillInStackTrace() );
        }


        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, ChatActivity.class)
                .putExtra("userId",userList.getUserId())
                .putExtra("userName",userList.getUserName())
                .putExtra("profileUrI",userList.getProfileUrI())

        ));
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final userModel userList=list.get(position);
        char firstChar=userList.getUserName().charAt(0);
        return Character.toString(firstChar).toUpperCase();
    }



    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView phoneNumber;
        private final CircularImageView profile;
        private final ImageView onlineStatus;
        public Holder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.userName);
            phoneNumber=itemView.findViewById(R.id.phoneNumber);
            profile=itemView.findViewById(R.id.image_profile);
            onlineStatus=itemView.findViewById(R.id.onlineStatus);
        }
    }
}
