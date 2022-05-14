package ru.liner.vr360client.playerlib;

import static ru.liner.vr360client.Core.getContext;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.CheckResult;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.io.IOException;

import ru.liner.vr360client.Core;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 14.05.2022, суббота
 **/
public class Player {

    private static final String TAG = "VideoPlayer";

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;

    private MediaPlayer player;
    private int curState = STATE_IDLE;

    private PlayerCallback callback;
    private int currentBufferPercentage;
    private Uri uri;
    private Surface surface;

    public void setCallback(PlayerCallback PlayerCallback) {
        this.callback = PlayerCallback;
    }

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            setCurrentState(STATE_ERROR);
            if (callback != null) {
                callback.onError(player, framework_err, impl_err);
            }
            return true;
        }
    };

    public Player() {
        setCurrentState(STATE_IDLE);
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        openVideo();
    }

    public Uri getUri() {
        return uri;
    }

    public void openVideo() {
        if (uri == null || surface == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        reset();

        try {
            player = new MediaPlayer();
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    currentBufferPercentage = percent;
                    if (callback != null) callback.onBufferingUpdate(mp, percent);
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    setCurrentState(STATE_PLAYBACK_COMPLETED);
                    if (callback != null) {
                        callback.onCompletion(mp);
                    }
                }
            });
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (callback != null) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            callback.onLoadingChanged(true);
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            callback.onLoadingChanged(false);
                        }
                    }
                    return false;
                }
            });
            player.setOnErrorListener(mErrorListener);
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (callback != null) callback.onVideoSizeChanged(mp, width, height);
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setCurrentState(STATE_PREPARED);
                    if (callback != null) {
                        callback.onPrepared(mp);
                    }
                }
            });
            currentBufferPercentage = 0;
            player.setDataSource(Core.getContext(), uri);
            player.setSurface(surface);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);
            player.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            setCurrentState(STATE_PREPARING);
        } catch (IOException | IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + uri.toString(), ex);
            setCurrentState(STATE_ERROR);
            mErrorListener.onError(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void start() {
        Log.i("DDD", "start");
        if (isInPlaybackState()) {
            player.start();
            setCurrentState(STATE_PLAYING);
        }
    }

    public void restart() {
        Log.i("DDD", "restart");
        openVideo();
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                setCurrentState(STATE_PAUSED);
            }
        }
    }

    public void reset() {
        if (player != null) {
            player.reset();
            player.release();
            setCurrentState(STATE_IDLE);
        }
    }

    private void setCurrentState(int state) {
        curState = state;
        if (callback != null) {
            callback.onStateChanged(curState);
            switch (state) {
                case STATE_IDLE:
                case STATE_ERROR:
                case STATE_PREPARED:
                    callback.onLoadingChanged(false);
                    break;
                case STATE_PREPARING:
                    callback.onLoadingChanged(true);
                    break;
            }
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
            player.release();
            // TODO: 2017/6/19 = null ?
            player = null;
            surface = null;
            setCurrentState(STATE_IDLE);
        }
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return player.getDuration();
        }

        return -1;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int progress) {
        if (isInPlaybackState()) {
            player.seekTo(progress);
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && player.isPlaying();
    }

    public int getBufferPercentage() {
        if (player != null) {
            return currentBufferPercentage;
        }
        return 0;
    }

    public boolean isInPlaybackState() {
        return (player != null &&
                curState != STATE_ERROR &&
                curState != STATE_IDLE &&
                curState != STATE_PREPARING);
    }

}
