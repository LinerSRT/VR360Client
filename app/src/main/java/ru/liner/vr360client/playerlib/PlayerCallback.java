package ru.liner.vr360client.playerlib;

import android.media.MediaPlayer;

public interface PlayerCallback {
    void onPrepared(MediaPlayer mp);
    void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    void onBufferingUpdate(MediaPlayer mp, int percent);
    void onCompletion(MediaPlayer mp);
    void onError(MediaPlayer mp, int what, int extra);
    void onLoadingChanged(boolean isShow);
    void onStateChanged(int curState);
}
