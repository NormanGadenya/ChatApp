package com.example.campaign.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.campaign.Common.ServiceCheck;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.UserViewModel;
import com.example.campaign.Model.userModel;
import com.example.campaign.R;
import com.example.campaign.Services.updateStatusService;
import com.example.campaign.adapter.chatListAdapter;
import com.example.campaign.adapter.userListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class UserListActivity extends AppCompatActivity {
    public static final String EXTRA_CIRCULAR_REVEAL_X="EXTRA_CIRCULAR_REVEAL_X" ;
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    View rootLayout;
    private int revealX;
    private int revealY;
    private FastScrollRecyclerView recyclerView;
    private List<userModel> list;
    private com.example.campaign.adapter.userListAdapter userListAdapter;
    private Handler handler;
    private ProgressBar progressBar;
    private MenuInflater menuInflater;
    private UserViewModel userViewModel;
    private SharedPreferences contactsSharedPrefs;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        InitializeControllers();
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ServiceCheck serviceCheck=new ServiceCheck(updateStatusService.class,this,manager);
        serviceCheck.checkServiceRunning();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    loadSharedPreferenceData();
                    userViewModel.initUserList(contactsSharedPrefs);
                    list=userViewModel.getAllUsers().getValue();
                    try{
                        userListAdapter=new userListAdapter(list, UserListActivity.this);
                        recyclerView.setAdapter(userListAdapter);
                    }catch(Exception e){
                        Log.e("Exception",e.getMessage());
                    }

                    loadUsers();
                }
            });


        final Intent intent = getIntent();
        openingAnimation(savedInstanceState, intent);

    }

    private void openingAnimation(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState == null && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);
            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);
            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }


    private void InitializeControllers() {
        recyclerView=findViewById(R.id.recyclerViewUserList);
        progressBar=findViewById(R.id.progressBarUserList);
        list=new ArrayList<>();
        handler=new Handler();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Select Contact");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        rootLayout = findViewById(R.id.root_layout);
    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }


    private void loadSharedPreferenceData() {
        contactsSharedPrefs=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
    }





    private void loadUsers(){
        userViewModel.getAllUsers().observe(this, userModels -> {
            userListAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent mainIntent = new Intent(UserListActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private ArrayList<userModel> FilterList(String newText){
        ArrayList<userModel> newList=new ArrayList<>();
        for(userModel user:list){
            if(user.getUserName().toLowerCase().contains(newText.toLowerCase())){
                newList.add(user);
            }
        }
        return newList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.chat_listmenu,menu);
        MenuItem searchItem=menu.findItem(R.id.search);
        MenuItem profileDetails=menu.findItem(R.id.profileButton);
        MenuItem settings=menu.findItem(R.id.settingsButton);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(searchItem);
        profileDetails.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(UserListActivity.this, UserProfileActivity.class);
                startActivity(intent);

                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                ArrayList<userModel> newList=new ArrayList<>();
                newList.clear();

                if (!TextUtils.isEmpty(s.trim())){
                    newList=FilterList(s);
                    userListAdapter=new userListAdapter(newList, UserListActivity.this);

                }else{
                    userListAdapter=new userListAdapter(list, UserListActivity.this);
                }
                userListAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(userListAdapter);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                ArrayList<userModel> newList=new ArrayList<>();
                newList.clear();
                if (newText.length()>0){
                    newList=FilterList(newText);
                    userListAdapter=new userListAdapter(newList, UserListActivity.this);
                }else{
                    userListAdapter=new userListAdapter(list, UserListActivity.this);
                }
                userListAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(userListAdapter);

                return false;
            }
        });
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }
}