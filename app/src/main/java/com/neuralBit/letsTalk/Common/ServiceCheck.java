package com.neuralBit.letsTalk.Common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.neuralBit.letsTalk.Services.updateStatusService;

public class ServiceCheck extends AppCompatActivity {
    public Class<?> serviceClass;
    public Context context;
    public ActivityManager manager;


    public ServiceCheck(Class<?> serviceClass,Context context,ActivityManager manager){
        this.serviceClass=serviceClass;
        this.context=context;
        this.manager=manager;
    }
    public void checkServiceRunning() {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (!serviceClass.getName().equals(service.service.getClassName())) {
                Intent i = new Intent(context, updateStatusService.class);
                context.startService(i);
            }
        }

    }





}

