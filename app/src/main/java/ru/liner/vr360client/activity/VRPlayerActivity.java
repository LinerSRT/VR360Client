package ru.liner.vr360client.activity;

import static com.asha.vrlib.MDVRLibrary.DISPLAY_MODE_GLASS;
import static com.asha.vrlib.MDVRLibrary.DISPLAY_MODE_NORMAL;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360client.R;
import ru.liner.vr360client.playerlib.VideoPlayer;
import ru.liner.vr360client.tcp.HostRetriever;
import ru.liner.vr360client.tcp.TCPClient;
import ru.liner.vr360client.utils.Constant;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class VRPlayerActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, TCPClient.Callback, VideoPlayer.OnPlayStateChangedListener {
    private MDVRLibrary library;
    private BarrelDistortionConfig distortionConfig;
    private TCPClient tcpClient;
    private HostRetriever hostRetriever;
    private VideoPlayer videoPlayer;
    private boolean syncFinished;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_vrplayer);
        videoPlayer = new VideoPlayer(this);
        videoPlayer.setOnPlaybackEventListener(new VideoPlayer.OnPlaybackEventListener() {
            @Override
            public void onPlaying() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onPositionChanged(int position) {

            }

            @Override
            public void onBufferingUpdate(int buffering) {

            }

            @Override
            public void onSeekComplete() {

            }
        });
        videoPlayer.setOnPlayStateChangedListener(this);
        distortionConfig = new BarrelDistortionConfig();
        distortionConfig.setDefaultEnabled(getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT);
        distortionConfig.setScale(0.95f);
        library = MDVRLibrary.with(this)
                .displayMode(DISPLAY_MODE_GLASS)
                .barrelDistortionConfig(distortionConfig)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)
                .pinchConfig(new MDPinchConfig().setMin(0.7f).setMax(8.0f).setDefaultValue(0.7f))
                .pinchEnabled(true)
                .asVideo(surface -> videoPlayer.setSurface(surface))
                .build((GLSurfaceView) findViewById(R.id.surfaceView));
        library.switchDisplayMode(this, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? DISPLAY_MODE_NORMAL : DISPLAY_MODE_GLASS);

        try {
            hostRetriever = new HostRetriever(Constant.SERVER_IP_PUBLISHER_HOST, Constant.SERVER_MULTICAST_PORT) {
                @Override
                public boolean accept(String host) {
                    if (tcpClient == null)
                        tcpClient = new TCPClient(host, Constant.SERVER_TCP_CONNECTION_PORT);
                    tcpClient.start(VRPlayerActivity.this);
                    return true;
                }
            };
            hostRetriever.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
        getWindow().getDecorView().setSystemUiVisibility(4870);
        //library.onResume(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //library.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        library.onDestroy();
        if (tcpClient != null && tcpClient.isConnected())
            tcpClient.stop();
    }


    public static Uri getUriFromRawFile(Context context, int rawResourceId) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.getPackageName())
                .path(String.valueOf(rawResourceId))
                .build();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & 4) == 0) {
            getWindow().getDecorView().setSystemUiVisibility(4870);
        }
    }

    private void enableDistortionCorrection(boolean enable) {
        distortionConfig.setDefaultEnabled(enable);
        library.switchBarrelDistortionConfig(distortionConfig);
    }


    @Override
    public void onConnected(Socket socket) {
        TCPClient.Callback.super.onConnected(socket);
        runOnUiThread(() -> hostRetriever.stop());
    }

    @Override
    public void onDisconnected(Socket socket) {
        TCPClient.Callback.super.onDisconnected(socket);
        runOnUiThread(() -> hostRetriever.start());
    }


    public String serialize(Object object) {
        if (object == null)
            return "";
        return object.getClass().getSimpleName() + "@" + new Gson().toJson(object);
    }


    @Override
    public void onReceived(Socket socket, String command) {
        TCPClient.Callback.super.onReceived(socket, command);
        if(command.equals("playVideo") && syncFinished) {
            videoPlayer.start();
        } else if(command.equals("stopVideo") && videoPlayer.isPlaying()){
            videoPlayer.stop();
        }  else if(command.contains("requestSync")){
            videoPlayer.setVideoURI(Uri.parse(command.split("@")[1]));
        }
    }

    @Override
    public void onStarted(Socket socket) {
        TCPClient.Callback.super.onStarted(socket);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration) {
        super.onConfigurationChanged(configuration);
        library.onOrientationChanged(this);
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            enableDistortionCorrection(false);
            library.switchDisplayMode(this, DISPLAY_MODE_NORMAL);
        } else {
            enableDistortionCorrection(true);
            library.switchDisplayMode(this, DISPLAY_MODE_GLASS);
        }
    }

    @Override
    public void onPrepared() {
        tcpClient.send("syncFinished");
        syncFinished = true;
    }

    @Override
    public void onError(VideoPlayer.ErrorReason reason) {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onReleased() {
syncFinished = false;
    }
}
