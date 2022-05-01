package com.monteroc.googlevr;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.monteroc.googlevr.Rendering.CanvasQuad;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 30.04.2022, суббота
 **/
public class PlayerController {
    @Nullable
    private MediaPlayer mediaPlayer;
    @Nullable
    private CanvasQuad canvasQuad;
    @NonNull
    private Context context;

    public PlayerController(@NonNull Context context) {
        this.context = context;
    }

    @MainThread
    public void setMediaPlayer(@Nullable MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }


    public void togglePlayPause() {
        if (mediaPlayer == null)
            return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer == null)
            return;
        mediaPlayer.pause();
    }

    public void play() {
        if (mediaPlayer == null)
            return;
        //mediaPlayer.start();
    }

    public long getCurrentPosition() {
        return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
    }
}
