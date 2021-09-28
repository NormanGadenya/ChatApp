package com.example.campaign.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campaign.Activities.ChatActivity;
import com.example.campaign.Common.Tools;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.Holder> {
    private final List<userModel> list;
    private final Context context;
    private final FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final Activity activity;
    private ChatViewModel chatViewModel;
    boolean isSelected,isEnabled=false;
    private final ArrayList<userModel> selected=new ArrayList<>();
    private final ViewModelStoreOwner viewModelStoreOwner;
    private final LifecycleOwner lifecycleOwner;
    private  SharedPreferences contactsSharedPrefs;


    public chatListAdapter(List<userModel> list, Context context, Activity activity,ViewModelStoreOwner viewModelStoreOwner ,LifecycleOwner lifecycleOwner) {
        this.list = list;
        this.context = context;
        this.lifecycleOwner=lifecycleOwner;
        this.activity = activity;
        this.viewModelStoreOwner=viewModelStoreOwner;


    }

    public void setContactsSharedPrefs(SharedPreferences contactsSharedPrefs){
        this.contactsSharedPrefs=contactsSharedPrefs;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list,parent,false);
        chatViewModel= ViewModelProviders.of((FragmentActivity)activity).get(ChatViewModel.class);

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        @SuppressLint("Recycle") TypedArray typedArray = activity.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        view.setBackgroundResource(backgroundResource);
        return new Holder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final userModel chatlist = list.get(position);


        getLastMessage(chatlist.getUserId(),holder.tvDesc,holder.tvDate,holder.imageView,holder.messageStatus);
        String userName=contactsSharedPrefs.getString(chatlist.getPhoneNumber(),null);
        if(userName!=null){
            holder.tvName.setText(userName);
        }else{
            holder.tvName.setText(chatlist.getUserName());
        }


        if (chatlist.getProfileUrI()==null){
            holder.profile.setImageResource(R.drawable.ic_male_avatar_svgrepo_com);
        } else {
            Glide.with(context).load(chatlist.getProfileUrI()).into(holder.profile);
        }

        if(chatlist.getOnline()){

            holder.onlineStatus.setVisibility( VISIBLE);

        }else{
            holder.onlineStatus.setVisibility( GONE);


        }

        if(chatlist.getTyping()!=null){
            if(chatlist.getTyping()){
                holder.tvTyping.setVisibility(VISIBLE);
                holder.tvDesc.setVisibility(GONE);
                holder.messageStatus.setVisibility(GONE);

            }else{
                holder.tvTyping.setVisibility(GONE);
                holder.tvDesc.setVisibility(VISIBLE);
                holder.messageStatus.setVisibility(VISIBLE);
            }
        }


        holder.itemView.setOnClickListener(v -> {
            if(isEnabled){
                clickedItem(holder);

            }else{
                if(list!=null){
                    String name;
                    if(userName==null){
                        name=list.get(position).getUserName();
                    }else{
                        name=userName;
                    }
                    Intent intent =new Intent(context, ChatActivity.class)
                            .putExtra("userId",list.get(position).getUserId())
                            .putExtra("userName",name)
                            .putExtra("profileUrI",list.get(position).getProfileUrI());

                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);


                }
            }

        });

        if(isSelected){
            holder.checkBox.setVisibility(VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }else{
            holder.checkBox.setVisibility(GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnLongClickListener(v -> {
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

                        chatViewModel.getText().observe((LifecycleOwner)activity, s -> mode.setTitle(String.format("%s selected",s)));

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
        });


    }

    private void clickedItem(Holder holder) {
        userModel chatListModel=list.get(holder.getAdapterPosition());
        if(holder.checkBox.getVisibility()== GONE){
            holder.checkBox.setVisibility(VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            selected.add(chatListModel);
        }else{
            holder.checkBox.setVisibility(GONE);
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
        private final TextView tvName;
        private final TextView tvDesc;
        private final TextView tvDate;
        private final TextView tvTyping;
        private final CircularImageView profile;
        private final ImageView imageView;
        private final ImageView checkBox;
        private final ImageView messageStatus;
        private final View onlineStatus;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.iconView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.desc);
            tvName = itemView.findViewById(R.id.userName);
            profile = itemView.findViewById(R.id.image_profile);
            tvTyping=itemView.findViewById(R.id.tv_typing);
            checkBox=itemView.findViewById(R.id.checkBox);
            onlineStatus=itemView.findViewById(R.id.onlineStatus);
            messageStatus=itemView.findViewById(R.id.messageStatus);
        }
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void getLastMessage(String userId, TextView description, TextView dateTime, ImageView imageView, ImageView messageStatus){
        String localDate=new Tools().getDate();
        chatViewModel = new ViewModelProvider(viewModelStoreOwner).get(ChatViewModel.class);
        chatViewModel.initLastMessage(userId);
        chatViewModel.getLastMessage().observe(lifecycleOwner, lastMessage -> {
            if(lastMessage.containsKey(userId)){
                Boolean messageChecked=lastMessage.get(userId).isChecked();
                Tools tools=new Tools();
                String textMessage=lastMessage.get(userId).getText();
                String imageUrI=lastMessage.get(userId).getImageUrI();
                String videoUrI=lastMessage.get(userId).getVideoUrI();
                String audioUrI=lastMessage.get(userId).getAudioUrI();
                String time=lastMessage.get(userId).getTime();
                String date=lastMessage.get(userId).getDate();
                if (localDate.equals(date)){
                    dateTime.setText(time);
                }else if(date.substring(6,10).equals(localDate.substring(6,10)) && date.substring(3,5).equals(localDate.substring(3,5)) && Integer.parseInt(date.substring(0,2))+1==Integer.parseInt(localDate.substring(0,2))){
                    dateTime.setText(R.string.yesterday);
                }else{
                    dateTime.setText(date);
                }
                if (messageChecked != null) {


                    if (messageChecked) {

                        messageStatus.setImageResource(R.drawable.ic_baseline_done_all_24);
                    } else {
                        messageStatus.setImageResource(R.drawable.ic_baseline_done_24);
                    }
                }

                if(imageUrI==null && videoUrI ==null && audioUrI==null){
                    textMessage=formatLastMessage(textMessage);
                    description.setText(textMessage);
                    imageView.setVisibility(GONE);

                }else {
                    description.setVisibility(GONE);
                    imageView.setVisibility(VISIBLE);

                    if(imageUrI!=null){
                        imageView.setImageDrawable(context.getDrawable(R.drawable.gallery));
                    }else if(videoUrI!=null){
                        imageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_videocam_24));
                    }else{
                        imageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_music_note_24));

                    }
                }
                chatViewModel.setLastMessage(null);
            }

        });

    }



}
