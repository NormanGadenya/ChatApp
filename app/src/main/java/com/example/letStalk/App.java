package com.example.letStalk;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class App  extends Application  {

    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
