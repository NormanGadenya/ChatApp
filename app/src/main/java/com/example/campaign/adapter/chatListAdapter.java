package com.example.campaign.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Activities.ChatActivity;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.Repository.Repo;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.Holder> {
    private List<userModel> list;
    private Context context;
    private FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerViewInterface recyclerViewInterface;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Activity activity;
    private ChatViewModel chatViewModel;
    boolean isSelected,isEnabled=false;
    ArrayList<userModel> selected=new ArrayList<>();
    private ViewModelStoreOwner viewModelStoreOwner;
    private LifecycleOwner lifecycleOwner;



    public chatListAdapter(List<userModel> list, Context context, Activity activity,ViewModelStoreOwner viewModelStoreOwner ,LifecycleOwner lifecycleOwner) {
        this.list = list;
        this.context = context;
        this.lifecycleOwner=lifecycleOwner;
        this.activity = activity;
        this.viewModelStoreOwner=viewModelStoreOwner;


    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list,parent,false);
        chatViewModel= ViewModelProviders.of((FragmentActivity)activity).get(ChatViewModel.class);

        return new Holder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final userModel chatlist = list.get(position);

        getLastMessage(chatlist.getUserId(),holder.tvDesc,holder.tvDate,holder.imageView);
        holder.tvName.setText(chatlist.getUserName());

        if (chatlist.getProfileUrI()==null){
            holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
        } else {
            Glide.with(context).load(chatlist.getProfileUrI()).into(holder.profile);
        }

        if(chatlist.getOnline()){
            holder.profile.setBorderColorStart( Color.CYAN);
            holder.profile.setBorderColorEnd( Color.MAGENTA);

        }else{
            holder.profile.setBorderColorStart(context.getColor(R.color.white));
            holder.profile.setBorderColorEnd( Color.WHITE);

        }
        holder.profile.setBorderColorDirection(CircularImageView.GradientDirection.LEFT_TO_RIGHT);
        holder.profile.setBorderWidth(10);
        if(chatlist.getTyping()!=null){
            if(chatlist.getTyping()){
                holder.tvTyping.setVisibility(View.VISIBLE);
                holder.tvDesc.setVisibility(View.GONE);
//                holder.tvDate.setVisibility(View.GONE);

            }else{
                holder.tvTyping.setVisibility(View.GONE);
                holder.tvDesc.setVisibility(View.VISIBLE);
//                holder.tvDate.setVisibility(View.VISIBLE);
            }
        }


        holder.itemView.setOnClickListener(v -> {
//            recyclerViewInterface.onItemClick(holder.getAdapterPosition());
            if(isEnabled){
                clickedItem(holder);

            }else{
                if(list!=null){
                    Intent intent =new Intent(context, ChatActivity.class)
                            .putExtra("userId",list.get(position).getUserId())
                            .putExtra("userName",list.get(position).getUserName())
                            .putExtra("profileUrI",list.get(position).getProfileUrI());

                    activity.startActivity(intent);

                }
            }

        });

        if(isSelected){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isEnabled){
                    ActionMode.Callback callback= new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MenuInflater menuInflater=mode.getMenuInflater();
                            menuInflater.inflate(R.menu.action_menu,menu);
                            return true;
                        }
                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            isEnabled=true;
                            clickedItem(holder);

                            chatViewModel.getText().observe((LifecycleOwner)activity,new Observer<String>(){

                                @Override
                                public void onChanged(String s) {
                                    mode.setTitle(String.format("%s selected",s));
                                }
                            });

                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            int id=item.getItemId();
                            switch (id){
                                case R.id.menu_delete:
                                    DatabaseReference chatRef=database.getReference().child("chats").child(firebaseUser.getUid());
                                    for(userModel c:selected){

                                        chatRef.child(c.getUserId()).removeValue();
                                        list.remove(c);
                                        Log.d("the list",String.valueOf(list.size()));
                                        notifyDataSetChanged();

                                    }


                                    mode.finish();
                                    break;

                                case R.id.menu_selectAll:
                                    if(selected.size() ==list.size()){
                                        isSelected=false;
                                        selected.clear();
                                    }else{
                                        isSelected=true;
                                        selected.clear();
                                        selected.addAll(list);
                                    }
                                    chatViewModel.setText(String.valueOf(selected.size()));
                                    notifyDataSetChanged();
                                    break;

                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            isEnabled=false;
                            isSelected=false;
                            selected.clear();
                            notifyDataSetChanged();
                        }
                    };
                    ((AppCompatActivity)v.getContext()).startActionMode(callback);

                }else{
                    clickedItem(holder);

                }
                return true;
            }

        });


    }

    private void clickedItem(Holder holder) {
        userModel chatListModel=list.get(holder.getAdapterPosition());
        if(holder.checkBox.getVisibility()==View.GONE){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            selected.add(chatListModel);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            selected.remove(chatListModel);
            Log.d("item_count",String.valueOf(selected.size()));
        }
        chatViewModel.setText(String.valueOf(selected.size()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate,tvTyping;
        private CircularImageView profile;
        private ImageView imageView;
        private ImageView checkBox;


        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.iconView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.desc);
            tvName = itemView.findViewById(R.id.userName);
            profile = itemView.findViewById(R.id.image_profile);
            tvTyping=itemView.findViewById(R.id.tv_typing);
            checkBox=itemView.findViewById(R.id.checkBox);


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

    private String formatDate(String date){
        String newDate;
        String month=null;
        switch(date.substring(3,5)){
            case "01":
                month="JAN";
                break;
            case "02":
                month="FEB";
                break;
            case "03":
                month="MAR";
                break;
            case "04":
                month="APR";
                break;
            case "05":
                month="MAY";
                break;
            case "06":
                month="JUNE";
                break;
            case "07":
                month="JULY";
                break;
            case "08":
                month="AUG";
                break;
            case "09":
                month="SEPT";
                break;
            case "10":
                month="OCT";
                break;
            case "11":
                month="NOV";
                break;
            case "12":
                month="DEC";
                break;

        }
        newDate=date.substring(0,2)+"-"+ month+ "-"+date.substring(6,10);
        return newDate;
    }
    private void getLastMessage(String userId,TextView description,TextView dateTime,ImageView imageView){
        DatabaseReference messageRef=database.getReference();
        chatViewModel = new ViewModelProvider(viewModelStoreOwner).get(ChatViewModel.class);
        chatViewModel.initLastMessage(userId);


        chatViewModel.getLastMessage().observe(lifecycleOwner, lastMessage -> {
            if(lastMessage.containsKey(userId)){
                String textMessage=lastMessage.get(userId).getText();
                String imageUrI=lastMessage.get(userId).getImageUrI();
                String time=lastMessage.get(userId).getTime();
                String date=lastMessage.get(userId).getDate();
                if (getDate().equals(date)){
                    dateTime.setText(time);
                }else if(date.substring(6,10).equals(getDate().substring(6,10)) && date.substring(3,5).equals(getDate().substring(3,5)) && Integer.parseInt(date.substring(0,2))+1==Integer.parseInt(getDate().substring(0,2))){
                    dateTime.setText("Yesterday");
                }else{
                    dateTime.setText(date);
                }
                if(imageUrI==null){
                    textMessage=formatLastMessage(textMessage);
                    description.setText(textMessage);
                    imageView.setVisibility(View.GONE);
                }else{
                    description.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                }
                chatViewModel.setLastMessage(null);
            }

        });
//        messageRef.child("chats").child(firebaseUser.getUid()).child(userId).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String textMessage=null;
//                String imageUrI=null;
//                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
//                    messageListModel message=dataSnapshot.getValue(messageListModel.class);
//                    textMessage=message.getText();
//                    imageUrI=message.getImageUrI();
//
//
//                    String time=message.getTime();
//                    String date=message.getDate();
//                    if (getDate().equals(date)){
//                        dateTime.setText(time);
//                    }else{
//                        dateTime.setText(date);
//                    }
//
//                }
//                if(imageUrI==null){
//                    textMessage=formatLastMessage(textMessage);
//                    description.setText(textMessage);
//                    imageView.setVisibility(View.GONE);
//                }else{
//                    description.setVisibility(View.GONE);
//                    imageView.setVisibility(View.VISIBLE);
//                }
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }



}
