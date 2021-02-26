package com.example.campaign.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.campaign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import com.example.campaign.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {
    public int CONTACTS_REQUEST;
    private String permGranted="false";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
//        requestContactsPermission();

        Bundle bundle = new Bundle();
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            permGranted="true";
//        } else {
//            requestContactsPermission();
//        }
        bundle.putString("perGranted",permGranted);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
//    private void requestContactsPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.READ_CONTACTS)) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Permission needed")
//                    .setMessage("This permission is needed because we require access to your contacts")
//                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions(MainActivity.this,
//                                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
//                        }
//                    })
//                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .create().show();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == CONTACTS_REQUEST)  {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                permGranted="true";
//            } else {
//                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}