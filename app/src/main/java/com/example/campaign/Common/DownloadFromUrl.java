package com.example.campaign.Common;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import static com.example.campaign.Services.ImageUploadService.getMimeType;

public class DownloadFromUrl extends AsyncTask<String, String, String> {
    public ProgressBar progressBar;
    public String otherUserName;
    String pathFolder = "";
    public String pathFile = "";
    public Context context;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(VISIBLE);


    }

    @Override
    protected String doInBackground(String... f_url) {
        String filePath=saveVideo(Uri.parse(f_url[0]),otherUserName);
//        int count;
//        String type=getMimeType(context,Uri.parse(f_url[0]));
//        String savedImagePath = null;
//        String imageFileName = otherUserName + date + type;

//        try {
//            pathFolder = Environment.getExternalStorageDirectory() + "/Lets talk";
//            pathFile = pathFolder + "/letsTalk.mp4";
//            File futureStudioIconFile = new File(pathFile,pathFolder);
//            if(!futureStudioIconFile.exists()){
//                futureStudioIconFile.mkdirs();
//            }
//            URL url = new URL(f_url[0]);
//            URLConnection connection = url.openConnection();
//            connection.connect();
//
//            // this will be useful so that you can show a tipical 0-100 %
//            // progress bar
//            int lengthOfFile = connection.getContentLength();
//            Log.d("length of file",String.valueOf(lengthOfFile));
//            // download the file
//            InputStream input = new BufferedInputStream(url.openStream());
//            FileOutputStream output = new FileOutputStream(pathFile);
//            FileOutputStream e=context.openFileOutput("norman.mp4",Context.MODE_WORLD_READABLE);
//
//
//            byte data[] = new byte[1024]; //anybody know what 1024 means ?
//            long total = 0;
//            while ((count = input.read(data)) != -1) {
//                total += count;
//                // publishing the progress....
//                // After this onProgressUpdate will be called
//                publishProgress("" + (int) ((total * 100) / lengthOfFile));
//
//                // writing data to file
//                output.write(data, 0, count);
//            }
//
//            // flushing output
//            output.flush();
//
//            // closing streams
//            output.close();
//            input.close();
//
//
//        } catch (Exception e) {
//            Log.e("Error: ", e.getMessage());
//        }
//        try {
//            String rootPath = Environment.getExternalStorageDirectory()
//                    .getAbsolutePath() + "/Let's Talk/";
//            File root = new File(rootPath);
//            if (!root.exists()) {
//                root.mkdirs();
//            }
//
//            File f = new File(rootPath + "mttext.txt");
//            if (f.exists()) {
//                f.delete();
//            }
//            f.createNewFile();
//
//            FileOutputStream out = new FileOutputStream(f);
//
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return pathFile;
        return filePath;
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
                byte data[] = new byte[1024]; //anybody know what 1024 means ?
                long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                fOut.write(data, 0, count);
            }
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddVideo(savedVideoPath);
//            Toast.makeText(context, "VIDEO SAVED", Toast.LENGTH_LONG).show();
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


    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
//        pd.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String file_url) {
        if (progressBar!=null) {
            progressBar.setVisibility(GONE);
        }
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        Intent i = new Intent(Intent.ACTION_VIEW);
//
//        i.setDataAndType(Uri.fromFile(new File(file_url)), "application/vnd.android.package-archive" );
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        context.startActivity(i);
    }

}