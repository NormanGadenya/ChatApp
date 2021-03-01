package com.example.campaign.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Activities.chatActivity;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;


public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.Holder> {
    private List<chatListModel> list;
    private Context context;
    private FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    ;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Vibrator vibrator;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final chatListModel chatlist = list.get(position);


        holder.tvName.setText(chatlist.getUserName());
        if(chatlist.getDescription()=="issa_photo"){
            holder.imageView.setVisibility(View.VISIBLE);
            holder.tvDesc.setVisibility(View.GONE);
        }else{
            holder.tvDesc.setText(chatlist.getDescription());
            holder.imageView.setVisibility(View.GONE);
        }

        if (getDate().equals(chatlist.getDate())){
            holder.tvDate.setText(chatlist.getTime());
        }else{
            holder.tvDate.setText(chatlist.getDate());
        }


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
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrator=(Vibrator)context.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(50);
                DatabaseReference chatRef=database.getReference().child("chats").child(firebaseUser.getUid()).child(chatlist.getUserId());
                chatRef.removeValue();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate;
        private CircularImageView profile;
        private ImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.iconView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.desc);
            tvName = itemView.findViewById(R.id.userName);
            profile = itemView.findViewById(R.id.image_profile);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(dateObj);
    }



}
