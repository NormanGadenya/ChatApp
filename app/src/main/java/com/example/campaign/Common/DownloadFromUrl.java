package com.example.campaign.Common;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.campaign.Common.Tools.getMimeType;


public class DownloadFromUrl extends AsyncTask<String, String, String> {
    @SuppressLint("StaticFieldLeak")
    public ProgressBar progressBar;
    public String otherUserName;
    @SuppressLint("StaticFieldLeak")
    private  Context context;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(VISIBLE);


    }

    @Override
    protected String doInBackground(String... f_url) {
        return saveVideo(Uri.parse(f_url[0]),otherUserName);
    }

    private String saveVideo( Uri uri,String otherUserName) {
        int count;
        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String type=getMimeType(context,uri);
        Log.d("type",type);
        String savedVideoPath = null;
        String videoFileName = otherUserName + date + ".mp4";
        File storageDir = new File(     Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/Lets Talk/Videos");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File videoFile = new File(storageDir, videoFileName  );
            savedVideoPath = videoFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(videoFile);
                URL url = new URL(String.valueOf(uri));
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                byte[] data = new byte[1024];
                long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;

                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                fOut.write(data, 0, count);
            }
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            galleryAddVideo(savedVideoPath);

        }
        return savedVideoPath;
    }

    private void galleryAddVideo(String videoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(videoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }




    @Override
    protected void onPostExecute(String file_url) {
        if (progressBar!=null) {
            progressBar.setVisibility(GONE);
        }

    }

    public void setContext(Context context) {
        this.context = context;
    }
}