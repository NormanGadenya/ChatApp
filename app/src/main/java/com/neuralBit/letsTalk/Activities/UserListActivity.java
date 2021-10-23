package com.neuralBit.letsTalk.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import android.animation.Animator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.neuralBit.letsTalk.Common.ServiceCheck;
import com.neuralBit.letsTalk.Common.Tools;
import com.neuralBit.letsTalk.Model.UserViewModel;
import com.neuralBit.letsTalk.Model.userModel;
import com.example.campaign.R;
import com.neuralBit.letsTalk.Services.updateStatusService;
import com.neuralBit.letsTalk.adapter.userListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.neuralBit.letsTalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_X;
import static com.neuralBit.letsTalk.Common.Tools.EXTRA_CIRCULAR_REVEAL_Y;

public class UserListActivity extends AppCompatActivity {

    private View rootLayout;
    private int revealX;
    private int revealY;
    private FastScrollRecyclerView recyclerView;
    private List<userModel> list;
    private com.neuralBit.letsTalk.adapter.userListAdapter userListAdapter;
    private ProgressBar progressBar;
    private UserViewModel userViewModel;
    private SharedPreferences contactsSharedPrefs;
    public static final String TAG = "UserListActivity";
    private Tools tools;
    private SharedPreferences settingsSharedPreferences;
    private CountDownTimer ct;



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
        loadSharedPreferenceData();
        settingsSharedPreferences=getSharedPreferences("Settings",MODE_PRIVATE);

        userViewModel.initUserList(contactsSharedPrefs);
        tools = new Tools();
        list=userViewModel.getAllUsers().getValue();



        userViewModel.getAllUsers().observe(this, userModels -> {

                userListAdapter=new userListAdapter(list, UserListActivity.this);
                recyclerView.setAdapter(userListAdapter);

            userListAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);

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


    @SuppressWarnings("deprecation")
    private void InitializeControllers() {
        recyclerView=findViewById(R.id.recyclerViewUserList);
        progressBar=findViewById(R.id.progressBarUserList);
        list=new ArrayList<>();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Select Contact");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        rootLayout = findViewById(R.id.root_layout);
    }

    protected void revealActivity(int x, int y) {
        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
        circularReveal.setDuration(400);
        circularReveal.setInterpolator(new AccelerateInterpolator());

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }


    private void loadSharedPreferenceData() {
        contactsSharedPrefs=getSharedPreferences("contactsSharedPreferences",MODE_PRIVATE);
    }


    @Override
    protected void onPause() {
        fpCountDown();
        super.onPause();
    }

    private void fpCountDown(){
        if(settingsSharedPreferences.getBoolean("setFingerprint",false)){
            tools.context=getApplicationContext();
            ct=tools.setUpFPTime();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(tools.fpTimeout){
            if(settingsSharedPreferences.getBoolean("setFingerprint",false)){
                Intent intent = new Intent(UserListActivity.this,FingerprintActivity.class);
                intent.putExtra("ActivityName",getClass().getCanonicalName());
                startActivity(intent);
            }
        }else{
            if(ct!=null){
                ct.cancel();
            }

        }

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_listmenu,menu);
        MenuItem searchItem=menu.findItem(R.id.search);
        MenuItem profileDetails=menu.findItem(R.id.profileButton);
        MenuItem settings=menu.findItem(R.id.settingsButton);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(searchItem);
        profileDetails.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(UserListActivity.this, UserProfileActivity.class);
            startActivity(intent);

            return false;
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
        settings.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
            startActivity(intent);
            return false;
        });
        return true;
    }
}