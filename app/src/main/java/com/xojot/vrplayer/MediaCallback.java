package com.xojot.vrplayer;

import android.media.MediaPlayer;

public interface MediaCallback {
    default void onMediaLoaded(MediaPlayer mediaPlayer){}
}
