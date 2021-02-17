package com.example.campaign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class image_act extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_act);
        String imageUrl=getIntent().getStringExtra("imageUrI");

        ImageView imageView=findViewById(R.id.imageView2);
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
        ImageView back= findViewById(R.id.back);
        back.setOnClickListener(view ->{
            Intent chatListAct=new Intent(this,chatActivity.class);
            startActivity(chatListAct);
        });
    }
}