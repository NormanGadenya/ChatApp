package com.example.campaign.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Activities.chatActivity;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;


public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.Holder> {
    private List<chatListModel> list;
    private Context context;

    public chatListAdapter(List<chatListModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final chatListModel chatlist = list.get(position);

        holder.tvName.setText(chatlist.getUserName());
        holder.tvDesc.setText(chatlist.getDescription());
        holder.tvDate.setText(chatlist.getDate());

        if (chatlist.getProfileUrI()==null){
            holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
        } else {
            Glide.with(context).load(chatlist.getProfileUrI()).into(holder.profile);
        }


        holder.itemView.setOnClickListener(v -> {context.startActivity(new Intent(context, chatActivity.class)
                .putExtra("userId",chatlist.getUserId())
                .putExtra("userName",chatlist.getUserName())
                .putExtra("profileUrI",chatlist.getProfileUrI())

        );


        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate;
        private CircularImageView profile;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.desc);
            tvName = itemView.findViewById(R.id.userName);
            profile = itemView.findViewById(R.id.image_profile);
        }
    }


}
