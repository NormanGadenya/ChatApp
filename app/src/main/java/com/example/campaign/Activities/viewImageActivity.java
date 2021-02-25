package com.example.campaign.Activities;

import androidx.activity.OnBackPressedCallback;
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
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.campaign.R;

import com.jgabrielfreitas.core.BlurImageView;

import com.zolad.zoominimageview.ZoomInImageView;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class viewImageActivity extends AppCompatActivity {
    private ImageView imageView2;


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

        imageView2=findViewById(R.id.backgroundView);
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
        Glide.with(getApplicationContext()).load(imageUrl).transform(new BlurTransformation(24)).into(imageView2);


    }








}
