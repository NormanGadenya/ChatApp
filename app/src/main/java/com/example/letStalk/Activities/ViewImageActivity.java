package com.example.letStalk.Activities;

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

import com.example.letStalk.Common.Tools;
import com.zolad.zoominimageview.ZoomInImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

import static com.example.letStalk.Common.Tools.getMimeType;


public class ViewImageActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private String otherUserName;
    public static final int PERMISSION_WRITE = 0;
    private String imageUrI;
    private Bitmap bitmap;




    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ActionBar actionBar=getSupportActionBar();
        imageUrI= getIntent().getStringExtra("imageUrI");
        otherUserName=getIntent().getStringExtra("otherUserName");
        Tools tools = new Tools();
        String caption = getIntent().getStringExtra("caption");
        try {
            caption = tools.decryptText(caption);
            imageUrI=tools.decryptText(imageUrI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String direction = getIntent().getStringExtra("direction");
        if(direction !=null ){
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
        ZoomInImageView imageView = findViewById(R.id.imageView);
        EmojiconTextView emojiconTextView = findViewById(R.id.caption);
        View captionBox = findViewById(R.id.captionBox);
        progressBar=findViewById(R.id.progressBar);
        ImageView background = findViewById(R.id.backgroundView);
        if(caption !=null){
            captionBox.setVisibility(View.VISIBLE);
            emojiconTextView.setText(caption);
        }else{
            captionBox.setVisibility(View.GONE);
        }
        MultiTransformation multi = new MultiTransformation<>(
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



    private void saveImage(Bitmap image, Uri uri) {
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
        saveItem.setOnMenuItemClickListener(item -> {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageUrI));
                checkPermission();

                saveImage(bitmap,Uri.parse(imageUrI));
            } catch (IOException e) {
                e.printStackTrace();
            }


            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_WRITE);

        }

    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage(bitmap,Uri.parse(imageUrI));
        }
    }
}
