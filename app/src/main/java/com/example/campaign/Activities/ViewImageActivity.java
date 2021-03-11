package com.example.campaign.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.R;

import com.jgabrielfreitas.core.BlurImageView;

import com.zolad.zoominimageview.ZoomInImageView;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class ViewImageActivity extends AppCompatActivity {
    private ImageView imageView2;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_act);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        String imageUrl = getIntent().getStringExtra("imageUrI");
        ZoomInImageView imageView =findViewById(R.id.imageView2);
        progressBar=findViewById(R.id.progressBar);

        imageView2=findViewById(R.id.backgroundView);
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
        Glide.with(getApplicationContext()).load(imageUrl).
                listener(new RequestListener<Drawable>() {
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
                }
        ).transform(new BlurTransformation(24)).into(imageView2);


    }


}
