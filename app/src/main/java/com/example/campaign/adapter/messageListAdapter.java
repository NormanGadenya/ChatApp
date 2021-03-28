package com.example.campaign.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String profileUrI;



    public messageListAdapter(List<messageListModel> list, Context context, String profileUrI, RecyclerViewInterface recyclerViewInterface) {
        this.list = list;
        this.context = context;
        this.profileUrI=profileUrI;
        this.recyclerViewInterface=recyclerViewInterface;
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


//        holder.itemView.setBackgroundColor(m.isChecked() ? R.color.dark_purple :R.color.transparent);
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                m.setChecked(!m.isChecked());
//                holder.itemView.setBackgroundColor(m.isChecked() ? R.color.dark_purple :R.color.transparent);
//            }
//        });
//        if(m.isChecked()){
//            recyclerViewInterface.onLongItemClick(position);
//            holder.itemView.setBackgroundColor(R.color.red);
//
//        }else {
//
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewInterface.onItemClick(position);
                list.get(position).setChecked(!list.get(position).isChecked());
                if(list.get(position).isChecked()){
                    holder.itemView.setBackgroundResource(R.color.deepBlueT);
                }else{
                    holder.itemView.setBackgroundResource(R.color.transparent);

                }


            }
        });




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



    public ArrayList<messageListModel> getSelected() {
        ArrayList<messageListModel> selected = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isChecked()) {
                selected.add(list.get(i));
            }
        }
        return selected;
    }

}

