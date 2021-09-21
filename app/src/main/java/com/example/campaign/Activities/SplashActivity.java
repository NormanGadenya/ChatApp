package com.example.campaign.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.R;

public class SplashActivity extends AppCompatActivity {


    private ChatViewModel chatViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.initChatsList();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this , SignUpActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 2000);
    }
}