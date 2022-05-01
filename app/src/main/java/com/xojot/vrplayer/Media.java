package com.xojot.vrplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public class Media implements MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnVideoSizeChangedListener{
    private final Context context;
    private MediaPlayer mediaPlayer;
    private ProjectionType projectionType;
    private StereoType stereoType;
    private Uri currentUri;
    private float aspectRatio;
    private float dFov;
    private float hFov;
    private float vFov;
    private float focalDistance;
    private int width;
    private int height;
    private boolean isPrepared;
    private boolean isPortrait;
    private int mediaBufferPercent;
    private int mediaDuration;
    private int mediaPosition;

    private MediaCallback mediaCallback;


    public Media(Context context) {
        this.context = context;
        this.projectionType = ProjectionType.RECTILINEAR;
        this.stereoType = StereoType.MONO;
    }

    public void setContentUri(Uri uri) {
        this.currentUri = uri;
        initMedia();
    }


    public void setVerticalFov(float vFov) {
        this.vFov = vFov;
        double d = width * 0.5d;
        double d2 = height * 0.5d;
        double distance = Math.sqrt(d * d + d2 * d2);
        this.focalDistance = (float) (d2 / Math.tan(vFov * 0.5d));
        this.dFov = (float) (Math.atan2(distance, focalDistance) * 2.0d);
        this.hFov = (float) (Math.atan2(d, focalDistance) * 2.0d);
    }

    public float getVerticalFov() {
        return vFov;
    }

    public void setHorizontalFov(float hFov) {
        this.hFov = hFov;
        double d = width * 0.5d;
        double d2 = height * 0.5d;
        double distance = Math.sqrt(d * d + d2 * d2);
        this.focalDistance = (float) (d / Math.tan(hFov * 0.5d));
        this.dFov = (float) (Math.atan2(distance, focalDistance) * 2.0d);
        this.vFov = (float) (Math.atan2(d2, focalDistance) * 2.0d);
    }

    public float getHorizontalFov() {
        return hFov;
    }

    public void setDistanceFov(float dFov) {
        this.dFov = dFov;
        double d = width * 0.5d;
        double d2 = height * 0.5d;
        this.focalDistance = (float) (Math.sqrt(d * d + d2 * d2) / Math.tan(dFov * 0.5));
        this.hFov = (float) (Math.atan2(d, focalDistance) * 2.0d);
        this.vFov = (float) (Math.atan2(d2, focalDistance) * 2.0d);
    }

    public float getDistanceFov() {
        return dFov;
    }


    public void setMediaCallback(MediaCallback mediaCallback) {
        this.mediaCallback = mediaCallback;
    }

    private void initMedia() {
        isPrepared = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        try {
            mediaPlayer.setDataSource(context, currentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }



    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.mediaBufferPercent = percent;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        this.mediaPlayer = mp;
        this.mediaDuration = mediaPlayer.getDuration();
        this.width = mediaPlayer.getVideoWidth();
        this.height = mediaPlayer.getVideoHeight();
        this.aspectRatio = (float) width / height;
        this.isPortrait = width < height;
        this.mediaPlayer.seekTo(mediaPosition);
        this.mediaPlayer.setLooping(true);
        if (mediaCallback != null)
            mediaCallback.onMediaLoaded(mediaPlayer);
        this.isPrepared = true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void start() {
        if ( mediaPlayer != null)
            mediaPlayer.start();
    }

    @Override
    public void pause() {
        if ( mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void stop() {
        if ( mediaPlayer != null)
            mediaPlayer.stop();
    }

    public void reset() {
        if ( mediaPlayer != null)
            mediaPlayer.reset();
    }

    @Override
    public int getDuration() {
        return (mediaPlayer == null) ? 0 : mediaDuration;
    }

    @Override
    public int getCurrentPosition() {
        return (mediaPlayer == null) ? 0 : mediaPosition;
    }

    @Override
    public void seekTo(int pos) {
        if( mediaPlayer != null){
            mediaPosition = pos;
            mediaPlayer.seekTo(pos);
        }
    }

    public void setLooping(boolean looping) {
        if( mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        }
    }

    @Override
    public boolean isPlaying() {
        return  mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isLooping() {
        return  mediaPlayer != null && mediaPlayer.isLooping();
    }


    public boolean isPrepared() {
        return isPrepared;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
        if(isPortrait){
            setVerticalFov(projectionType.fov);
        } else {
            setHorizontalFov(projectionType.fov);
        }
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setStereoType(StereoType stereoType) {
        this.stereoType = stereoType;

    }

    public StereoType getStereoType() {
        return stereoType;
    }

    @Override
    public int getBufferPercentage() {
        return ( mediaPlayer == null) ? 0 : mediaBufferPercent;
    }

    @Override
    public boolean canPause() {
        return  mediaPlayer != null;
    }

    @Override
    public boolean canSeekBackward() {
        return mediaPlayer != null;
    }

    @Override
    public boolean canSeekForward() {
        return  mediaPlayer != null;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public enum ProjectionType {
        EQUIRECTANGULAR((float) Math.toRadians(0.0)),
        RECTILINEAR((float) Math.toRadians(60.0)),
        EQUIDISTANT((float) Math.toRadians(130.0)),
        STEREOGRAPHIC((float) Math.toRadians(130.0)),
        ORTHOGRAPHIC((float) Math.toRadians(130.0)),
        EQUISOLID((float) Math.toRadians(130.0));
        public float fov;

        ProjectionType(float fov) {
            this.fov = fov;
        }
    }

    public enum StereoType {
        MONO,
        SIDE_BY_SIDE,
        OVER_UNDER
    }
}
