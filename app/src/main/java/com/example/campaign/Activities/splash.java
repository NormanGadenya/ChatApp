package com.example.campaign.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.R;

public class splash extends AppCompatActivity {

    Animation animLeft,animRight;
    ImageView imgLeft,imgRight;
    private ChatViewModel chatViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imgLeft=findViewById(R.id.imageView2);
        imgRight=findViewById(R.id.imageView3);
        animLeft= AnimationUtils.loadAnimation(this,R.anim.splashleft);
        animRight=AnimationUtils.loadAnimation(this,R.anim.splashright);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.initChatsList();
        animLeft.setDuration(1500);
        animRight.setDuration(1500);
        imgLeft.setAnimation(animLeft);
        imgRight.setAnimation(animRight);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(splash.this , SignUpActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 2000);
    }
}