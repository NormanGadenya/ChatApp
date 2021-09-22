package com.example.campaign.Common;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Tools {
    public Context context;

    public String getTime(){
        Date dateTime = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
        String time = DATE_FORMAT.format(dateTime);
        return time;
    }


    public String getDate(){

        Date dateTime = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        String date = DATE_FORMAT.format(dateTime);
        return date;
    }

    public boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean isFileLessThan2MB(Uri uri) throws FileNotFoundException {
        AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri ,"r");
        long fileSize = fileDescriptor.getLength();

        long maxFileSize = 2 * 1024 * 1024;


        return fileSize <= maxFileSize;
    }
}
