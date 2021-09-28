package com.example.campaign.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.R;

import java.util.HashMap;
import java.util.Map;

import static com.example.campaign.Common.Tools.CONTACTS_REQUEST;

public class SplashActivity extends AppCompatActivity {
    private final Map<String, String> namePhoneMap= new HashMap<>();
    private Context context;
    private SharedPreferences contactsSharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context=getApplicationContext();
        ChatViewModel chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.initChatsList();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getPhoneNumbers();
        }else{
            requestContactsPermission();
        }



    }

    private void requestContactsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we need to access your Contacts")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhoneNumbers();

            } else {
                requestContactsPermission();
            }
        }
    }

    public boolean isAlphanumeric2(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
                return false;
        }
        return true;
    }

    private void getPhoneNumbers() {

        // Loop Through All The Numbers
        // Cleanup the phone number
        // Enter Into Hash Map
        //                    phoneNumbers.put(phoneNumber,name);
        Thread getContactsThread = new Thread(() -> {
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            // Loop Through All The Numbers
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                // Cleanup the phone number
                phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

                // Enter Into Hash Map
                namePhoneMap.put(phoneNumber, name);

            }
            contactsSharedPrefs = getSharedPreferences("contactsSharedPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = contactsSharedPrefs.edit();

            for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
                String phoneNumber = entry.getKey();
                String name = entry.getValue();
                if (phoneNumber.contains("+")) {
//                    phoneNumbers.put(phoneNumber,name);
                    editor.putString(phoneNumber, name);
                } else {
                    if (isAlphanumeric2(phoneNumber)) {

                        long i = Long.parseLong(phoneNumber);
                        phoneNumber = "+256" + i;
                        editor.putString(phoneNumber, name);


                    }
                }
            }
            phones.close();
            editor.apply();


            Intent mainIntent = new Intent(SplashActivity.this, SignUpActivity.class);
            startActivity(mainIntent);
            finish();

        });

        getContactsThread.start();

    }


}