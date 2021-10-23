package com.neuralBit.letsTalk.Interfaces;

import android.media.MediaPlayer;

public interface RecyclerViewInterface {
    void onItemClick(int position);
    void onLongItemClick(int position);
    void getMediaPlayer(MediaPlayer mediaPlayer);
}