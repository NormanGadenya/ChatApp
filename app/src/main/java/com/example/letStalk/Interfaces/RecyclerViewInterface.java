package com.example.letStalk.Interfaces;

import android.media.MediaPlayer;
import android.os.Handler;

public interface RecyclerViewInterface {
    void onItemClick(int position);
    void onLongItemClick(int position);
    void getMediaPlayer(MediaPlayer mediaPlayer);
}