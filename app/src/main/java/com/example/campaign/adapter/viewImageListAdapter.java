package com.example.campaign.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.Model.viewImageModel;
import com.example.campaign.R;
import com.zolad.zoominimageview.ZoomInImageView;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class viewImageListAdapter extends RecyclerView.Adapter<viewImageListAdapter.Holder> {
    private ArrayList<messageListModel> list;
    private Context context;

    public viewImageListAdapter(ArrayList<messageListModel> list, Context context){
        this.list=list;
        this.context=context;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.view_image_layout,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Glide.with(context).load(list.get(position).getImageUrI()).into(holder.zoomInImageView);
        Glide.with(context).load(list.get(position).getImageUrI()).
                listener(new RequestListener<Drawable>() {
                             @Override
                             public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                 holder.progressBar.setVisibility(View.GONE);
                                 return false;
                             }

                             @Override
                             public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                 holder.progressBar.setVisibility(View.GONE);
                                 return false;
                             }
                         }
                ).transform(new BlurTransformation(24)).into(holder.backgroundImageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private ImageView backgroundImageView;
        private ZoomInImageView zoomInImageView;
        private ProgressBar progressBar;
        public Holder(@NonNull View itemView) {
            super(itemView);
            backgroundImageView=itemView.findViewById(R.id.backgroundView);
            zoomInImageView=itemView.findViewById(R.id.imageView);
            progressBar=itemView.findViewById(R.id.progressBar);
        }
    }
}
