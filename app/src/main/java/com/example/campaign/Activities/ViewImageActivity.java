package com.example.campaign.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Model.viewImageModel;
import com.example.campaign.R;

import com.example.campaign.adapter.viewImageListAdapter;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class ViewImageActivity extends AppCompatActivity {
    private ImageView imageView2;
    private ProgressBar progressBar;
    private String otherUserName;
    private ZoomInImageView imageView;
    public static final int PERMISSION_WRITE = 0;
    String imageUrl ;
    private RecyclerView recyclerView;
    private viewImageListAdapter viewImageListAdapter;
    private ArrayList<String> list=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        otherUserName=getIntent().getStringExtra("userName");
        ActionBar actionBar=getSupportActionBar();

        list=(ArrayList<String>) getIntent().getSerializableExtra("imageList");
        viewImageListAdapter=new viewImageListAdapter(list,getApplicationContext());
        recyclerView=findViewById(R.id.viewImageRecycler);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(viewImageListAdapter);
        int position=getIntent().getIntExtra("position",2);
        recyclerView.scrollToPosition(position);
        checkPermission();
//        String direction=getIntent().getStringExtra("Direction");
//        if(direction!=null){
//            if(direction.equals("to")){
//                actionBar.setTitle("To "+ otherUserName);
//            }else{
//                actionBar.setTitle("From "+ otherUserName);
//            }
//        }else{
//            actionBar.setTitle("To "+ otherUserName);
//        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


//        imageView =findViewById(R.id.imageView2);
//        progressBar=findViewById(R.id.progressBar);
//        imageUrl= getIntent().getStringExtra("imageUrI");
//        imageView2=findViewById(R.id.backgroundView);



    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private  void saveImage(String uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(uri));

//            File dir = new File(Environment.getExternalStorageDirectory() + "/CHAT/");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String fileUri = dir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
//            String imageName=System.currentTimeMillis()+ ".PNG";
//            File file= new File(dir,imageName);
//
//            Log.d("scscdvf",fileUri);

            File filePath=Environment.getExternalStorageDirectory();
            File dir=new File(filePath.getAbsolutePath() +"/CHAT/");
            if(!dir.exists()){
                dir.mkdir();

            }
            File file =new File (dir,System.currentTimeMillis()+".jpg");

            FileOutputStream outputStream=new FileOutputStream(file);
//            FileOutputStream outputStream = new FileOutputStream(fileUri);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.view_image_menu,menu);
        MenuItem saveItem=menu.findItem(R.id.saveFile);

        saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveImage(imageUrl);
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
            //do somethings
        }
    }
}
