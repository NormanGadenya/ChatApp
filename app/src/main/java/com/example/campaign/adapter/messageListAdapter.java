package com.example.campaign.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.campaign.Common.Tools;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campaign.Activities.ChatActivity;

import com.example.campaign.Interfaces.RecyclerViewInterface;
import com.example.campaign.Model.ChatViewModel;
import com.example.campaign.Model.messageListModel;
import com.example.campaign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.example.campaign.Common.Tools.MESSAGE_LEFT;
import static com.example.campaign.Common.Tools.MESSAGE_RIGHT;


public class messageListAdapter extends RecyclerView.Adapter<messageListAdapter.Holder>  {
    private List<messageListModel> list;
    public Context context;
    public Map<String,Integer> uploadImageTask=new HashMap<>();
    public Map<String,Integer> uploadVideoTask=new HashMap<>();
    private RecyclerViewInterface recyclerViewInterface;
    private Tools tools = new Tools();
    public static final String TAG="messageListAdapter";
    private FirebaseUser user;
    private String otherUserId;
    private Activity activity;
    private ChatViewModel chatViewModel;
    private MediaPlayer mediaPlayer;
    private boolean isSelected,isEnabled=false;
    private  final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private final ArrayList<messageListModel> selected=new ArrayList<>();
    private final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    private Handler mHandler = new Handler();



    public messageListAdapter(){ }
    public void setMessageList(List<messageListModel> list){
        this.list=list;
    }

    public void setMContext(Context context){
        this.context=context;
    }
    public void setRecyclerViewInterface(RecyclerViewInterface recyclerViewInterface){ this.recyclerViewInterface=recyclerViewInterface; }

    public void setActivity(Activity activity){
        this.activity=activity;
    }

    public void setOtherUserId(String otherUserId){
        this.otherUserId=otherUserId;
    }



    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType ==MESSAGE_LEFT){
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);

        }else if(viewType==MESSAGE_RIGHT){
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);

        }else{
            view =LayoutInflater.from(context).inflate(R.layout.chat_item_date,parent,false);
        }
        chatViewModel= ViewModelProviders.of((FragmentActivity)activity).get(ChatViewModel.class);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        try {
            holder.bind(list.get(position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String previousTs=null;
        if(position>=1){
            previousTs = list.get(position-1).getDate();

        }
        setTimeTextVisibility(list.get(position).getDate(), previousTs, holder.msgGroupDate);

        if(isSelected){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.deepBlueT);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.itemView.setOnClickListener(v -> {
            if(isEnabled){
                clickedItem(holder);
            }

        });

        holder.itemView.setOnLongClickListener(v -> {

            if(!isEnabled){
                ActionMode.Callback callback= new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater menuInflater=mode.getMenuInflater();
                        menuInflater.inflate(R.menu.action_menu,menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        isEnabled=true;
                        clickedItem(holder);
                        chatViewModel.getText().observe((LifecycleOwner)activity, s -> mode.setTitle(String.format("%s selected", s)));
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        int id=item.getItemId();
                        switch (id){
                            case R.id.menu_delete:
                                DatabaseReference messageRef=database.getReference().child("chats").child(firebaseUser.getUid()).child(otherUserId);
                                for(messageListModel c:selected){
                                    list.remove(c);
                                    messageRef.child(c.getMessageId()).removeValue();
                                    notifyDataSetChanged();
                                }
                                mode.finish();
                                break;

                            case R.id.menu_selectAll:
                                if(selected.size() ==list.size()){
                                    isSelected=false;
                                    selected.clear();
                                }else{
                                    isSelected=true;
                                    selected.clear();
                                    selected.addAll(list);
                                }
                                chatViewModel.setText(String.valueOf(selected.size()));
                                notifyDataSetChanged();
                                break;

                        }
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        isEnabled=false;
                        isSelected=false;
                        selected.clear();
                        notifyDataSetChanged();
                    }
                };
                ((AppCompatActivity)v.getContext()).startActionMode(callback);
            }else{
                clickedItem(holder);
            }
            return true;
        });
    }

    private void clickedItem(Holder holder) {
        messageListModel messageListModel=list.get(holder.getAdapterPosition());
        if(holder.checkBox.getVisibility()==View.GONE){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.deepBlueT);
            selected.add(messageListModel);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            selected.remove(messageListModel);
            Log.d("item_count",String.valueOf(selected.size()));
        }
        chatViewModel.setText(String.valueOf(selected.size()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position){
        user= FirebaseAuth.getInstance().getCurrentUser();
        final messageListModel messageList=list.get(position);

        if(messageList.getReceiver().equals(user.getUid())){

            return MESSAGE_LEFT;
        }
        else {
            return MESSAGE_RIGHT;
        }


    }

    @SuppressWarnings("RedundantThrows")
    public class Holder extends RecyclerView.ViewHolder  {
        private final TextView time;
        private final TextView msgGroupDate;
        private final EmojiconTextView message;
        private final ImageView messageStatus;
        private final ZoomInImageView imageView;
        private final ProgressBar progressBar;
        private final ProgressBar audioLoadProgress;
        private final ImageButton videoPlayButton;
        private final ImageButton playButton;
        private final ImageButton pauseButton;
        private final TextView duration;
        private final SeekBar audioSeekBar;
        private final ImageView checkBox;

        public Holder(@NonNull View itemView) {
            super(itemView);
            videoPlayButton=itemView.findViewById(R.id.vidPlayButton);
            imageView=itemView.findViewById(R.id.imageView);
            message = itemView.findViewById(R.id.show_message);
            messageStatus=itemView.findViewById(R.id.message_status);
            time=itemView.findViewById(R.id.time);
            audioLoadProgress=itemView.findViewById(R.id.audioLoadingP);
            progressBar=itemView.findViewById(R.id.progressBar);
            msgGroupDate=itemView.findViewById(R.id.msgGroupDate);
            checkBox=itemView.findViewById(R.id.checkBox);
            audioSeekBar=itemView.findViewById(R.id.music_progress);
            playButton=itemView.findViewById(R.id.playButton);
            pauseButton=itemView.findViewById(R.id.pauseButton);
            duration=itemView.findViewById(R.id.duration);
        }

        @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
        private void bind(final messageListModel messageList) throws IOException {

            if(messageList.getReceiver()!=null){

                switch(messageList.getType()){
                    case "TEXT":
                        videoPlayButton.setVisibility(View.GONE);

                        message.setText(messageList.getText());
                        imageView.setVisibility(View.GONE);
                        audioLoadProgress.setVisibility(View.GONE);
                        message.setVisibility(View.VISIBLE);
                        time.setText(messageList.getTime());
                        playButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.GONE);
                        audioSeekBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        duration.setVisibility(View.GONE);
                        message.setOnLongClickListener(v -> {
                            recyclerViewInterface.onLongItemClick(getAdapterPosition());
                            return false;
                        });
                        break;
                    case "IMAGE":

                        Map<String,Integer> uploadImageData;
                        uploadImageData=((ChatActivity)context).getUploadImageTaskData();
                        videoPlayButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.GONE);
                        audioLoadProgress.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.GONE);
                        audioSeekBar.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setOnClickListener(I-> recyclerViewInterface.onItemClick(getAdapterPosition()));
                        duration.setVisibility(View.GONE);
                        if(messageList.getText()==null){
                            message.setVisibility(View.GONE);
                        }else{
                            message.setText(messageList.getText());
                            message.setVisibility(View.VISIBLE);
                        }

                        imageView.setClipToOutline(true);

                        time.setText(messageList.getTime());
                        if(uploadImageData.containsKey(messageList.getMessageId()) && uploadImageData!=null){
                            progressBar.setVisibility(View.VISIBLE);
                            if(uploadImageData.get(messageList.getMessageId())==1){
                                progressBar.setVisibility(View.GONE);
                            }

                            Glide.with(context).load(messageList.getImageUrI()).transform(new BlurTransformation(uploadImageData.get(messageList.getMessageId()))).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {


                                    return false;
                                }
                            }).into(imageView);
//                            progressBar.setVisibility(View.GONE);
                        }else{
                            Glide.with(context).load(messageList.getImageUrI()).listener(new RequestListener<Drawable>() {
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
                            }).into(imageView);
                        }
                        break;

                    case "VIDEO":
                        Map<String,Integer> uploadVideoData;
                        uploadVideoData=((ChatActivity)context).getUploadVideoTaskData();
                        progressBar.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.GONE);
                        audioSeekBar.setVisibility(View.GONE);
                        duration.setVisibility(View.GONE);
                        if(messageList.getText()==null){
                            message.setVisibility(View.GONE);
                        }else{

                            message.setText(messageList.getText());
                            message.setVisibility(View.VISIBLE);
                        }
                        imageView.setClipToOutline(true);
                        videoPlayButton.setVisibility(View.VISIBLE);
                        videoPlayButton.setOnClickListener(V-> recyclerViewInterface.onItemClick(getAdapterPosition()));
                        time.setText(messageList.getTime());
                        String videoUrI=messageList.getVideoUrI();
                        if(videoUrI!=null){
                            Glide.with(context)
                                    .load(videoUrI)
                                    .placeholder(R.drawable.black)
                                    .into(imageView);
                            if(uploadVideoData.containsKey(messageList.getMessageId()) && uploadVideoData!=null){
                                progressBar.setVisibility(View.VISIBLE);
                                if(uploadVideoData.get(messageList.getMessageId())==100){
                                    progressBar.setVisibility(View.GONE);
                                }

                            }else{
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        break;

                    case "AUDIO":
                        Map<String,Integer> uploadAudioData;
                        uploadAudioData=((ChatActivity)context).getUploadAudioTaskData();
                        playButton.setVisibility(View.VISIBLE);
                        pauseButton.setVisibility(View.GONE);
                        audioSeekBar.setVisibility(View.VISIBLE);
                        duration.setVisibility(View.VISIBLE);
                        message.setVisibility(View.GONE);
                        videoPlayButton.setVisibility(View.GONE);
                        message.setText(messageList.getText());
                        imageView.setVisibility(View.GONE);
                        message.setVisibility(View.VISIBLE);
                        time.setText(messageList.getTime());
                        progressBar.setVisibility(View.GONE);
                        if(uploadAudioData.containsKey(messageList.getMessageId()) && uploadAudioData!=null){
                            audioLoadProgress.setVisibility(View.VISIBLE);
                            playButton.setVisibility(View.GONE);
                            audioSeekBar.setEnabled(false);
                            if(uploadAudioData.get(messageList.getMessageId())==100){
                                audioLoadProgress.setVisibility(View.GONE);
                                playButton.setVisibility(View.VISIBLE);
                                audioSeekBar.setEnabled(true);
                            }

                        }else{
                            progressBar.setVisibility(View.GONE);
                        }
                        audioSeekBar.setMax(100);
                        duration.setText(millisecondsToText(Long.parseLong(messageList.getAudioDuration())));
                        mediaPlayer=new MediaPlayer();
                        recyclerViewInterface.getMediaPlayer(mediaPlayer);
                        playButton.setOnClickListener(I->{
                            if(mediaPlayer!=null){
                                if(!mediaPlayer.isPlaying() ){
                                    try{
                                        playButton.setVisibility(View.GONE);
                                        mediaPlayer.start();
                                        pauseButton.setVisibility(View.VISIBLE);
                                        updateSeekBar();
                                    }catch (Exception e){
                                        Log.e(TAG, "bind: ",e.fillInStackTrace() );
                                    }
                                }
                            }



                        });
                        pauseButton.setOnClickListener(I->{

                                if(mediaPlayer.isPlaying()){
                                    playButton.setVisibility(View.VISIBLE);
                                    pauseButton.setVisibility(View.GONE);
                                    mHandler.removeCallbacks(updater);
                                    mediaPlayer.pause();
                                }

                        });
                        if(!messageList.getReceiver().equals(user.getUid())){

                            prepareMediaPlayer(messageList.getAudioUrI());
                        }else{
                            prepareMediaPlayerOther(messageList.getAudioUrI());
                        }
                        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> audioSeekBar.setSecondaryProgress(percent));
                        mediaPlayer.setOnCompletionListener(mp -> {

                            audioSeekBar.setProgress(0);
                            duration.setText("0:00");
                            pauseButton.setVisibility(View.GONE);
                            playButton.setVisibility(View.VISIBLE);
                            mediaPlayer.reset();
                            mediaPlayer.release();
                            if(!messageList.getReceiver().equals(user.getUid())){
                                prepareMediaPlayer(messageList.getAudioUrI());
                            }else{
                                prepareMediaPlayerOther(messageList.getAudioUrI());

                            }
                        });
                        audioSeekBar.setOnTouchListener((v, event) -> {
                            SeekBar seekBar=(SeekBar)v;
                            int playPosition=(mediaPlayer.getDuration()/100)*seekBar.getProgress();
                            mediaPlayer.seekTo(playPosition);
                            duration.setText(millisecondsToText(mediaPlayer.getCurrentPosition()));
                            return false;
                        });

                        break;
                }
                if (messageList.isChecked()){
                    messageStatus.setImageResource(R.drawable.ic_baseline_done_all_24);


                }
                else{
                    messageStatus.setImageResource(R.drawable.ic_baseline_done_24);
                }

            }
        }

        void updateSeekBar(){

            if(mediaPlayer != null){
                mHandler=new Handler();
                try{
                    if(mediaPlayer.isPlaying()){
                        int progress=(int) (((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100);
                        audioSeekBar.setProgress(progress);
                        mHandler.postDelayed(updater,1000);

                    }
                }catch(Exception e){
                    Log.e(TAG, "updateSeekBar: ",e.fillInStackTrace() );
                }

            }
        }

        private final Runnable updater=new Runnable() {
            @Override
            public void run() {
                updateSeekBar();

               if(mediaPlayer != null){
                   try {
                       long currentDuration= mediaPlayer.getCurrentPosition();
                       duration.setText(millisecondsToText(currentDuration));
                   }catch(Exception e){
                       Log.e(TAG, "run: ",e.fillInStackTrace() );
                   }

               }
            }
        };

        private void prepareMediaPlayer(String uri){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                try {
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );
                    mediaPlayer.setDataSource(context, Uri.parse(uri));
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    Log.e("messageList", e.getLocalizedMessage() + 'k');
                }
            });
            }

        private void prepareMediaPlayerOther(String uri){
            Handler handler =new Handler(Looper.getMainLooper());
            handler.post(() -> {
                try{
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );
                    mediaPlayer.setDataSource(uri);
                    mediaPlayer.prepare();
                }catch(Exception e){
                    Log.e("messageList",e.getLocalizedMessage()+ 'k');
                }
            });

        }
    }


    private String millisecondsToText(long duration){
        String timerString ="";
        String secondsString;
        int hours=(int) (duration/(1000*3600));
        int minutes=(int) (duration % (1000 *60 * 60))/(1000*60);
        int seconds=(int) ((duration % (1000 *60 * 60))% (1000*60)/1000);

        if (hours > 0) {
            timerString = hours +":";
        }
        if (seconds<10){
            secondsString = "0" + seconds;
        }else{
            secondsString = ""+ seconds ;
        }
        timerString= timerString + minutes+":"+ secondsString;
        return timerString;
    }


    private void setTimeTextVisibility(String ts1, String ts2, TextView timeText){
        String date=new Tools().getDate();
        if(ts2==null){
            timeText.setVisibility(View.VISIBLE);
            if(ts1.equals(date)){
                timeText.setText(R.string.today);
            }else{
                timeText.setText(formatDate(ts1));
            }


        }else {
            if(ts1.length()==10 || ts2.length()==10){
                String y1=ts1.substring(6,10);
                String y2=ts2.substring(6,10);
                String m1=ts1.substring(3,5);
                String m2=ts2.substring(3,5);
                String d1=ts1.substring(0,2);
                String d2=ts2.substring(0,2);
                boolean sameMonth = y1.equals(y2) &&
                        m1.equals(m2) && d1.equals(d2);

                if(sameMonth){
                    timeText.setVisibility(View.GONE);
                    timeText.setText("");
                }else {
                    timeText.setVisibility(View.VISIBLE);
                    if(ts1.equals(date)){
                        timeText.setText(R.string.today);
                    }else if(ts1.substring(6,10).equals(date.substring(6,10)) && ts1.substring(3,5).equals(date.substring(3,5)) && Integer.parseInt(ts1.substring(0,2))+1==Integer.parseInt(date.substring(0,2))){
                        timeText.setText(R.string.yesterday);
                    }else{
                        timeText.setText(formatDate(ts1));
                    }


                }
            }


        }
    }

    private String formatDate(String date){
        String newDate;
        String month=null;
        switch(date.substring(3,5)){
            case "01":
                month="JAN";
                break;
            case "02":
                month="FEB";
                break;
            case "03":
                month="MAR";
                break;
            case "04":
                month="APR";
                break;
            case "05":
                month="MAY";
                break;
            case "06":
                month="JUNE";
                break;
            case "07":
                month="JULY";
                break;
            case "08":
                month="AUG";
                break;
            case "09":
                month="SEPT";
                break;
            case "10":
                month="OCT";
                break;
            case "11":
                month="NOV";
                break;
            case "12":
                month="DEC";
                break;

        }
        newDate=date.substring(0,2)+"-"+ month+ "-"+date.substring(6,10);
        return newDate;
    }

}

