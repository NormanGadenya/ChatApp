package com.example.campaign;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {
    public int x=123;
    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
