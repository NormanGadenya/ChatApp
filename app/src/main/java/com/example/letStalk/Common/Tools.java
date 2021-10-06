package com.example.letStalk.Common;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.security.cert.Certificate;
import javax.security.cert.CertificateException;

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
    public static final String KEY_STORE="ANDROID_KEY_STORE";
    public static final String ALIAS ="LETSTALK";

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

    public String encrypt(String message,PublicKey publicKey) throws Exception{
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return  encode(encryptedBytes);
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

    public  String decrypt (String encryptedMessage, PrivateKey privateKey) throws Exception{
        byte [] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte [] decryptedMessage =cipher.doFinal(encryptedBytes);
        return  new String(decryptedMessage,"UTF8");
    }

    public byte[] decode(String encryptedMessage) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            return  Base64.getDecoder().decode(encryptedMessage);
        }else {
            return  android.util.Base64.decode(encryptedMessage,android.util.Base64.DEFAULT);
        }
    }



    public List encryptMessage(String text)  {
        List<Object> array=new ArrayList();
        try{
            Signature sign = Signature.getInstance("SHA256withRSA");

            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

            keyPairGen.initialize(2048);


            //Generate the pair of keys
            KeyPair pair = keyPairGen.generateKeyPair();

            //Getting the public key from the key pair

            PublicKey publicKey = pair.getPublic();

            //Creating a Cipher object
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");


            //Initializing a Cipher object
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            //Add data to the cipher
            byte[] input = text.getBytes();
            cipher.update(input);
            byte[] cipherText = cipher.doFinal();
            array.add(cipherText);
            array.add(pair);
        }catch(Exception e){
            Log.e( "encryptMessage: ",e.getLocalizedMessage() );
        }

        return array;

    }

    public String decryptMessage(List<Object> array){
        byte[] encryptedText=(byte[])array.get(0);
        KeyPair pair=(KeyPair)array.get(1);
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            byte[] decryptedText = cipher.doFinal(encryptedText);
            return new String(decryptedText);
        }catch(Exception e){
            Log.e( "decryptMessage: ",e.getLocalizedMessage() );
        }
        return null;

    }
}
