package com.example.campaign.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.campaign.R;
import com.jgabrielfreitas.core.BlurImageView;
import com.zolad.zoominimageview.ZoomInImageView;

public class viewImageActivity extends AppCompatActivity {


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
        BlurImageView blurImageView=findViewById(R.id.backgroundView);
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
//        Glide.with(getApplicationContext()).load(imageUrl).into(blurImageView);
        blurImageView.setBlur(10);



    }






}
