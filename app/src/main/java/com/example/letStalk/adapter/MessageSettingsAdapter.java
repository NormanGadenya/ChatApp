package com.example.letStalk.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letStalk.Model.messageListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageSettingsAdapter extends RecyclerView.Adapter<MessageSettingsAdapter.Holder> {
    private final List<messageListModel> list;
    private final Context context;
    private static final int MESSAGE_LEFT=0;
    private static final int MESSAGE_RIGHT=1;
    private final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    public int viewBackColor;

    public MessageSettingsAdapter(List<messageListModel> list, Context context){
        this.context = context;
        this.list=list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType ==MESSAGE_LEFT){
            view =LayoutInflater.from(context).inflate(R.layout.settings_chat_left,parent,false);

        }else if(viewType==MESSAGE_RIGHT){
            view =LayoutInflater.from(context).inflate(R.layout.settings_chat_right,parent,false);

        }else{
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_date,parent,false);
        }

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if( viewBackColor!=0){
            GradientDrawable gradientDrawable = (GradientDrawable) holder.backgroundView.getBackground() .mutate();
            gradientDrawable.setColor(viewBackColor);
        }



    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public int getItemViewType(int position) {

        final messageListModel messageList = list.get(position);

        if (messageList.getReceiver().equals(firebaseUser.getUid())) {

            return MESSAGE_LEFT;
        } else {
            return MESSAGE_RIGHT;
        }


    }

    public static class Holder extends RecyclerView.ViewHolder {
        private View backgroundView;
        public Holder(@NonNull View itemView) {
            super(itemView);
            backgroundView=itemView.findViewById(R.id.background);

        }
    }
}
