package com.neuralBit.letsTalk.Common;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Tools {
    public Context context;
    public static final int IMAGEREQUEST = 2;
    public static final int  AUDIOREQUEST = 3;
    public static final int  VIDEOREQUEST = 4;
    public static final int  CAMERA_REQUEST = 1888;
    public static final int  GALLERY_REQUEST = 100;
    public static final int  CONTACTS_REQUEST = 200;
    public static final String  EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String  EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    public static final int  MESSAGE_LEFT = 0;
    public static final int  MESSAGE_RIGHT = 1;

    public static final String ALIAS="letsTalk";
    private static final String PASS ="Bar12345Bar12345";
    private static Cipher cipher ;
    public  Boolean fpTimeout=false;
    public String getTime(){
        Date dateTime = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
        String time = DATE_FORMAT.format(dateTime);
        return time;
    }


    public String getDate(){

        Date dateTime = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
        String date = DATE_FORMAT.format(dateTime);
        return date;
    }

    public boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public boolean isFileLessThan2MB(Uri uri) throws FileNotFoundException {
        AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri ,"r");
        long fileSize = fileDescriptor.getLength();

        long maxFileSize = 2 * 1024 * 1024;


        return fileSize <= maxFileSize;
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public Bitmap getBitmap(int id){
        Drawable defWallpaper = context.getResources().getDrawable(id);
        return ((BitmapDrawable) defWallpaper).getBitmap();
    }

    public String encryptText(String text) throws Exception  {
        Key aesKey =  new SecretKeySpec(PASS.getBytes(), "AES");
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return encode(encrypted);
    }

    public CountDownTimer setUpFPTime(){
        SharedPreferences settingsSharedPreferences=context.getSharedPreferences("Settings",MODE_PRIVATE);
        CountDownTimer ct;
        String timeOut = settingsSharedPreferences.getString("fpTimeOut",null);
        long time;
        if(timeOut.equals("2 minutes")){
            time = 120000;
        }else if (timeOut.equals("5 minutes")){
            time = 300000;
        }else {
            time =0;
        }
        ct=new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                fpTimeout=true;
            }
        };
        if(time!=0){

            ct.start();
        }else{
            fpTimeout=true;
        }

        return ct;

    }

    public Boolean checkBiometricSupport(){

        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (!fingerprintManager.isHardwareDetected()) {
            return false;
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            return false;
        } else {
            return true;
        }

    }


    public String decryptText(String encrypted) throws Exception{
        Key aesKey =  new SecretKeySpec(PASS.getBytes(), "AES");
        byte [] encryptedBytes = decode(encrypted);

        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,aesKey);
        byte [] decryptedMessage =cipher.doFinal(encryptedBytes);
        return  new String(decryptedMessage,"UTF8");


    }

    public byte[] decode(String encryptedMessage) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            return  Base64.getMimeDecoder().decode(encryptedMessage);
        }else {
            return  android.util.Base64.decode(encryptedMessage,android.util.Base64.DEFAULT);
        }
    }

    public String encode (byte [] data ){
        String encodedString;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            encodedString = Base64.getEncoder().encodeToString(data);
        }else{
            encodedString=android.util.Base64.encodeToString(data,android.util.Base64.DEFAULT);
        }
        return encodedString;

    }


}
