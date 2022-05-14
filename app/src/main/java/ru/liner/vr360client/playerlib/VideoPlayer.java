package ru.liner.vr360client.playerlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 14.05.2022, суббота
 **/
public class VideoPlayer {
    public static final int STATE_PLAYING = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_RELEASED = 3;
    private boolean startImmediately = false;
    private Context context;
    private Uri uri;
    private MediaPlayer player;
    private Surface surface;
    private PlaybackEventListenerImpl playbackEventListenerImpl;
    private PlayStateChangedListenerImpl playStateChangedListenerImpl;


    private PositionHandler positionHandler = new PositionHandler();
    private OnPlaybackEventListener onPlaybackEventListener;
    private OnPlayStateChangedListener onPlayStateChangedListener;

    public VideoPlayer(Context context) {
        this.context = context;
    }

    private void initPlayer() {
        playbackEventListenerImpl = new PlaybackEventListenerImpl();
        playStateChangedListenerImpl = new PlayStateChangedListenerImpl();
        player = new MediaPlayer();
        player.setOnPreparedListener(playStateChangedListenerImpl);
        player.setOnErrorListener(playStateChangedListenerImpl);
        player.setOnCompletionListener(playStateChangedListenerImpl);
        player.setOnBufferingUpdateListener(playbackEventListenerImpl);
        player.setOnSeekCompleteListener(playbackEventListenerImpl);
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, false);
    }

    public void setVideoURI(Uri uri, boolean startImmediately) {
        this.uri = uri;
        this.startImmediately = startImmediately;
        synchronized (VideoPlayer.class) {
            if (player == null) {
                initPlayer();
            }
        }
        try {
            player.setDataSource(context, uri);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
        synchronized (VideoPlayer.class) {
            if (player == null) {
                initPlayer();
                if (uri != null) {
                    setVideoURI(uri, startImmediately);
                }
            }
        }
        player.setSurface(surface);
    }



    public int getVideoWidth() {
        return isReleased() ? 0 : player.getVideoWidth();
    }

    public int getVideoHeight() {
        return isReleased() ? 0 : player.getVideoHeight();
    }

    public void start() {
        if (isReleased()) {
            throw new IllegalStateException("VideoPlayerView is released. Set video uri again.");
        }

        if (uri == null) {
            return;
        }

        player.start();
        positionHandler.sendMessage(positionHandler.obtainMessage(STATE_PLAYING));
        playbackEventListenerImpl.onPlaying();
    }

    public void pause() {
        if (isReleased()) {
            throw new IllegalStateException("VideoPlayerView is released. Set video uri again.");
        }

        if (uri == null) {
            return;
        }

        player.pause();
        positionHandler.sendMessage(positionHandler.obtainMessage(STATE_PAUSE));
        playbackEventListenerImpl.onPaused();
    }

    public void stop() {
        if (isReleased()) {
            throw new IllegalStateException("VideoPlayerView is released. Set video uri again.");
        }
        if (uri == null) {
            return;
        }

        player.stop();
        positionHandler.sendMessage(positionHandler.obtainMessage(STATE_STOPPED));
        playbackEventListenerImpl.onStopped();
    }

    public void release() {
        if (player == null) {
            return;
        }
        positionHandler.sendMessage(positionHandler.obtainMessage(STATE_STOPPED));
        player.release();
        player = null;
        uri = null;
        startImmediately = false;
        if (playStateChangedListenerImpl != null) {
            playStateChangedListenerImpl.onReleased();
        }
    }

    public boolean isReleased() {
        return player == null;
    }

    public boolean isPlaying() {
        return player != null && uri != null && player.isPlaying();
    }

    public int getCurrentPosition() {
        return player == null ? 0 : player.getCurrentPosition();
    }

    public int getDuration() {
        return player == null ? 0 : player.getDuration();
    }

    public void seekTo(int msec) {
        if (player == null) {
            throw new IllegalStateException("VideoPlayerView is released. Set video uri again.");
        }

        player.seekTo(msec);
    }

    public void setOnPlaybackEventListener(OnPlaybackEventListener listener) {
        this.onPlaybackEventListener = listener;
    }

    public void setOnPlayStateChangedListener(OnPlayStateChangedListener listener) {
        this.onPlayStateChangedListener = listener;
    }

    public enum ErrorReason {
        NETWORK
    }


    public interface OnPlayStateChangedListener {
        void onPrepared();

        void onError(ErrorReason reason);

        void onCompletion();

        void onReleased();
    }

    public interface OnPlaybackEventListener {
        void onPlaying();

        void onPaused();

        void onStopped();

        void onPositionChanged(int position);

        void onBufferingUpdate(int buffering);

        void onSeekComplete();

    }

    private class PlaybackEventListenerImpl implements OnPlaybackEventListener, MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnSeekCompleteListener {

        private int buffering = 0;

        @Override
        public void onPlaying() {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onPlaying();
            }
        }

        @Override
        public void onPaused() {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onPaused();
            }
        }

        @Override
        public void onStopped() {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onStopped();
            }
        }

        @Override
        public void onPositionChanged(int position) {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onPositionChanged(position);
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (buffering == percent) {
                return;
            }

            buffering = percent;
            onBufferingUpdate(buffering);
        }

        @Override
        public void onBufferingUpdate(int buffering) {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onBufferingUpdate(buffering);
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            onSeekComplete();
        }

        @Override
        public void onSeekComplete() {
            if (onPlaybackEventListener != null) {
                onPlaybackEventListener.onSeekComplete();
            }
        }
    }

    private class PlayStateChangedListenerImpl implements OnPlayStateChangedListener, MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            onPrepared();

            if (startImmediately) {
                start();
            }
        }

        @Override
        public void onPrepared() {
            if (onPlayStateChangedListener != null) {
                onPlayStateChangedListener.onPrepared();
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            onError(ErrorReason.NETWORK);
            return false;
        }

        @Override
        public void onError(ErrorReason reason) {
            if (onPlayStateChangedListener != null) {
                onPlayStateChangedListener.onError(reason);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onCompletion();
        }

        @Override
        public void onCompletion() {
            if (onPlayStateChangedListener != null) {
                onPlayStateChangedListener.onCompletion();
            }
        }

        @Override
        public void onReleased() {
            if (onPlayStateChangedListener != null) {
                onPlayStateChangedListener.onReleased();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class PositionHandler extends Handler {
        private static final int INTERVAL_TIME = 128;

        private int previousPosition = 0;

        PositionHandler() {
            super(Objects.requireNonNull(Looper.myLooper()));
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case STATE_PLAYING:
                    schedulePlaying();
                    break;
                case STATE_STOPPED:
                case STATE_RELEASED:
                case STATE_PAUSE:
                    removeMessages(STATE_PLAYING);

            }
        }

        private void schedulePlaying() {
            removeMessages(STATE_PLAYING);
            if (player == null || isReleased()) {
                return;
            }
            if (!player.isPlaying()) {
                start();
                return;
            }
            Message msg = obtainMessage();
            msg.what = STATE_PLAYING;
            sendMessageDelayed(msg, INTERVAL_TIME);

            int offset = player.getCurrentPosition();
            if (this.previousPosition == offset) {
                return;
            }

            this.previousPosition = offset;
            playbackEventListenerImpl.onPositionChanged(offset);
        }
    }
}