package com.example.campaign.adapter;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.Holder> {
    private List<chatListModel> list;
    private Context context;
    private FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerViewInterface recyclerViewInterface;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Vibrator vibrator;

    public chatListAdapter(List<chatListModel> list, Context context,RecyclerViewInterface recyclerViewInterface) {
        this.list = list;
        this.context = context;
        this.recyclerViewInterface=recyclerViewInterface;

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

        getLastMessage(chatlist.getUserId(),holder.tvDesc,holder.tvDate,holder.imageView);
        holder.tvName.setText(chatlist.getUserName());

        if (chatlist.getProfileUrI()==null){
            holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
        } else {
            Glide.with(context).load(chatlist.getProfileUrI()).into(holder.profile);
        }


        holder.itemView.setOnClickListener(v -> {
            recyclerViewInterface.onItemClick(holder.getAdapterPosition());
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

    private String formatLastMessage(String text) {
        if(text!=null){
            if (text.length()>30){
                String i=text.substring(0,30);
                text=i+"...";
            }
        }

        return text;
    }

    private void getLastMessage(String userId,TextView description,TextView dateTime,ImageView imageView){
        DatabaseReference messageRef=database.getReference();

        messageRef.child("chats").child(firebaseUser.getUid()).child(userId).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    messageListModel message=dataSnapshot.getValue(messageListModel.class);
                    String textMessage=message.getText();
                    String imageUrI=message.getImageUrI();
                    if(imageUrI==null){
                        textMessage=formatLastMessage(textMessage);
                        description.setText(textMessage);
                        imageView.setVisibility(View.GONE);
                    }else{
                        description.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    }

                    String time=message.getTime();
                    String date=message.getDate();
                    if (getDate().equals(date)){
                        dateTime.setText(time);
                    }else{
                        dateTime.setText(date);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}
