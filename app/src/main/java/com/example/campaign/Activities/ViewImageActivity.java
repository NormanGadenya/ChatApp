package com.example.campaign.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.R;

import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

import static com.example.campaign.Services.ImageUploadService.getMimeType;


public class ViewImageActivity extends AppCompatActivity {
    private ImageView background;
    private ProgressBar progressBar;
    private String otherUserName,fUserName;
    private ZoomInImageView imageView;
    public static final int PERMISSION_WRITE = 0;
    private String imageUrI,caption,direction ;
    private EmojiconTextView emojiconTextView;
    private View captionBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ActionBar actionBar=getSupportActionBar();
        imageUrI= getIntent().getStringExtra("imageUrI");
        otherUserName=getIntent().getStringExtra("otherUserName");
        caption=getIntent().getStringExtra("caption");
        direction=getIntent().getStringExtra("direction");
        if(direction!=null ){
            if(direction.equals("to")){
                actionBar.setTitle("To "+ otherUserName);
            }else{
                actionBar.setTitle("From "+ otherUserName);
            }
        }else{
            actionBar.setTitle("Profile picture :"+ otherUserName);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        imageView =findViewById(R.id.imageView);
        emojiconTextView=findViewById(R.id.caption);
        captionBox=findViewById(R.id.captionBox);
        progressBar=findViewById(R.id.progressBar);
        background=findViewById(R.id.backgroundView);
        if(caption!=null){
            captionBox.setVisibility(View.VISIBLE);
            emojiconTextView.setText(caption);
        }else{
            captionBox.setVisibility(View.GONE);
        }
        MultiTransformation multi = new MultiTransformation<Bitmap>(
                new BlurTransformation(25),
                new GrayscaleTransformation());
        Glide.with(getApplicationContext()).load(imageUrI).into(imageView);
        Glide.with(getApplicationContext()).load(imageUrI).apply(RequestOptions.bitmapTransform(multi)).
                listener(new RequestListener<Drawable>() {
                             @Override
                             public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                 progressBar.setVisibility(View.GONE);
                                 return false;
                             }

                             @Override
                             public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                 progressBar.setVisibility(View.GONE);
                                 return false;
                             }
                         }
                ).into(background);



    }



//    private  void saveImage(String uri){
//        checkPermission();
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(uri));
//
////            File dir = new File(Environment.getExternalStorageDirectory() + "/CHAT/");
////            if (!dir.exists()) {
////                dir.mkdirs();
////            }
////
////            String fileUri = dir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
////            String imageName=System.currentTimeMillis()+ ".PNG";
////            File file= new File(dir,imageName);
////
////            Log.d("scscdvf",fileUri);
//
//            File filePath=Environment.getExternalStorageDirectory();
//            File dir=new File(filePath.getAbsolutePath() +"/CHAT/");
//            if(!dir.exists()){
//                dir.mkdir();
//
//            }
//            File file =new File (dir,System.currentTimeMillis()+".jpg");
//
//            FileOutputStream outputStream=new FileOutputStream(file);
////            FileOutputStream outputStream = new FileOutputStream(fileUri);
//
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//            outputStream.flush();
//            outputStream.close();
//            Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_LONG).show();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }
    private String saveImage(Bitmap image,Uri uri) {
        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String type=getMimeType(getApplicationContext(),uri);
        String savedImagePath = null;
        String imageFileName = otherUserName + date +"."+ type;
        File storageDir = new File(            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/Lets Talk/Images");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);

                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath);
            Toast.makeText(getApplicationContext(), "IMAGE SAVED", Toast.LENGTH_LONG).show();
        }
        return savedImagePath;
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.view_menu,menu);
        MenuItem saveItem=menu.findItem(R.id.saveFile);
        saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    checkPermission();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageUrI));
                    saveImage(bitmap,Uri.parse(imageUrI));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            saveImage(imageUrI);
        }
    }
}
