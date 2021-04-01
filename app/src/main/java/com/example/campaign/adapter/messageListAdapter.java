package com.example.campaign.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.zolad.zoominimageview.ZoomInImageView;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class messageListAdapter extends RecyclerView.Adapter<messageListAdapter.Holder> {
    private List<messageListModel> list;
    public Context context;
    private RecyclerViewInterface recyclerViewInterface;
    private static final int MESSAGE_LEFT=0;
    private static final int MESSAGE_RIGHT=1;
    public static final int DATE=3;
    private FirebaseUser user;
    private String profileUrI,otherUserId;
    private Activity activity;
    private ChatViewModel chatViewModel;
    boolean isSelected,isEnabled=false;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    ArrayList<messageListModel> selected=new ArrayList<>();
    private FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();



    public messageListAdapter(List<messageListModel> list, Context context, String profileUrI, RecyclerViewInterface recyclerViewInterface,Activity activity,String otherUserId) {
        this.list = list;
        this.context = context;
        this.profileUrI=profileUrI;
        this.recyclerViewInterface=recyclerViewInterface;
        this.activity=activity;
        this.otherUserId=otherUserId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType ==MESSAGE_LEFT){
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);

        }else if(viewType==MESSAGE_RIGHT){
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);

        }else{
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_date,parent,false);
        }
        chatViewModel= ViewModelProviders.of((FragmentActivity)activity).get(ChatViewModel.class);
        return new Holder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.bind(list.get(position));

        String previousTs=null;
        if(position>=1){
            previousTs = list.get(position-1).getDate();

        }
        setTimeTextVisibility(list.get(position).getDate(), previousTs, holder.msgGroupDate);

        if(isSelected){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled){
                    clickedItem(holder);
                }

            }
        });

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
                                    DatabaseReference messageRef=database.getReference().child("chats").child(firebaseUser.getUid()).child(otherUserId);
                                    for(messageListModel c:selected){
                                        list.remove(c);
                                        messageRef.child(c.getMessageId()).removeValue();
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
        messageListModel messageListModel=list.get(holder.getAdapterPosition());
        if(holder.checkBox.getVisibility()==View.GONE){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.deepBlueT);
            selected.add(messageListModel);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            selected.remove(messageListModel);
            Log.d("item_count",String.valueOf(selected.size()));
        }
        chatViewModel.setText(String.valueOf(selected.size()));
    }

    @Override
    public int getItemCount() {
        return list.size();
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
        private TextView message,time,msgGroupDate;
        private CircularImageView profilePic;
        private ImageView messageStatus;
        private ZoomInImageView imageView;
        private ProgressBar progressBar;
        private ImageButton delete;
        private TextView date;
        private ImageView checkBox;
        private View layout;


        public Holder(@NonNull View itemView) {
            super(itemView);
            delete=itemView.findViewById(R.id.delete);
            imageView=itemView.findViewById(R.id.imageView);
            profilePic=itemView.findViewById(R.id.image_profile);
            message = itemView.findViewById(R.id.show_message);
            messageStatus=itemView.findViewById(R.id.message_status);
            time=itemView.findViewById(R.id.time);
            progressBar=itemView.findViewById(R.id.progressBar);
            date=itemView.findViewById(R.id.date);
            msgGroupDate=itemView.findViewById(R.id.msgGroupDate);
            checkBox=itemView.findViewById(R.id.checkBox);
            layout=itemView.findViewById(R.id.const2);


        }
        void bind(final messageListModel messageList){
            if(messageList.getReceiver()!=null){

                switch(messageList.getType()){
                    case "TEXT":
                        message.setText(messageList.getText());
                        imageView.setVisibility(itemView.GONE);
                        message.setVisibility(itemView.VISIBLE);
                        time.setText(messageList.getTime());
                        progressBar.setVisibility(itemView.GONE);
                        message.setOnLongClickListener(new View.OnLongClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public boolean onLongClick(View v) {
                                recyclerViewInterface.onLongItemClick(getAdapterPosition());
                                return false;
                            }
                        });
                        break;
                    case "IMAGE":
                        progressBar.setVisibility(itemView.VISIBLE);
                        imageView.setVisibility(itemView.VISIBLE);
                        if(messageList.getText()==null){
                            message.setVisibility(itemView.GONE);
                        }else{
                            message.setText(messageList.getText());
                            message.setVisibility(itemView.VISIBLE);
                        }

//                    imageView.setBackgroundColor(messageList.getBackgroundColor());
                        imageView.setClipToOutline(true);
                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public boolean onLongClick(View v) {
                              recyclerViewInterface.onItemClick(getAdapterPosition());
                              return true;
                            }
                        });
                        time.setText(messageList.getTime());
                        Glide.with(context).load(messageList.getImageUrI()).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);

                                return false;
                            }
                        }).into(imageView);





                        break;

                }
                if (messageList.getMessageStatus()=="read"){
                    messageStatus.setImageResource(R.drawable.ic_baseline_done_all_24);

                }
                else{
                    messageStatus.setImageResource(R.drawable.ic_baseline_done_24);
                }

            }else{
//                date.setText(messageList.getDate());
            }


        }


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setTimeTextVisibility(String ts1, String ts2, TextView timeText){

        if(ts2==null){
            timeText.setVisibility(View.VISIBLE);
            if(ts1.equals(getDate())){
                timeText.setText("Today");
            }else{
                timeText.setText(ts1);
            }


        }else {
            if(ts1.length()==10 || ts2.length()==10){
                String y1=ts1.substring(6,10);
                String y2=ts2.substring(6,10);
                String m1=ts1.substring(3,5);
                String m2=ts2.substring(3,5);
                String d1=ts1.substring(0,2);
                String d2=ts2.substring(0,2);
                Log.d("pos",String.valueOf(ts1.substring(0,2)));
                boolean sameMonth = y1.equals(y2) &&
                        m1.equals(m2) && d1.equals(d2);

                if(sameMonth){
                    timeText.setVisibility(View.GONE);
                    timeText.setText("");
                }else {
                    timeText.setVisibility(View.VISIBLE);
                    if(ts1.equals(getDate())){
                        timeText.setText("Today");
                    }else{
                        timeText.setText(ts1);
                    }


                }
            }


        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter timeObj = DateTimeFormatter.ofPattern("HH:mm");
        return myDateObj.format(timeObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter dateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(dateObj);
    }





}

