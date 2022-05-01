package ru.liner.vr360client.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.xojot.vrplayer.VrView;

import java.io.File;
import java.io.IOException;

import ru.liner.vr360client.R;
import ru.liner.vr360client.tcp.HostRetriever;
import ru.liner.vr360client.tcp.SocketCallback;
import ru.liner.vr360client.tcp.TCPClient;
import ru.liner.vr360client.tcp.TCPDevice;
import ru.liner.vr360client.utils.Constant;
import ru.liner.vr360client.utils.Download;
import ru.liner.vr360client.utils.VrModeChanger;


@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, SocketCallback, VrModeChanger.Callback {
    private VrView vrView;
    private boolean isInVRMode;
    private int pausePosition;
    private HostRetriever hostRetriever;
    private String host;
    private TCPClient client;
    private VrModeChanger vrModeChanger;

    private Download download;
    private File videoFile;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        vrView = findViewById(R.id.vrView);
        download = new Download(this);
        vrModeChanger = new VrModeChanger(this, vrView);
        vrModeChanger.setCallback(this);
        try {
            hostRetriever = new HostRetriever(Constant.SERVER_IP_PUBLISHER_HOST, Constant.SERVER_MULTICAST_PORT) {
                @Override
                public boolean accept(String host) {
                    if (client == null) {
                        MainActivity.this.host = host;
                        client = new TCPClient(host, Constant.SERVER_TCP_CONNECTION_PORT);
                        client.connect(MainActivity.this);
                        return true;
                    }
                    return false;
                }
            };
            hostRetriever.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContent(Uri uri) {
        vrModeChanger.start();
        vrView.setDataSource(uri);
        vrView.setAutoPanning(false);
        vrView.setCameraReset(false);
        vrModeChanger.start();
    }


    @Override
    public void onResume() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
        getWindow().getDecorView().setSystemUiVisibility(4870);
        super.onResume();
        vrView.onResume();
        vrView.seekTo(pausePosition);
    }


    @Override
    public void onPause() {
        super.onPause();
        vrView.pause();
        pausePosition = vrView.getCurrentPosition();
        vrView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if ((i & 4) == 0) {
            getWindow().getDecorView().setSystemUiVisibility(4870);
        }
    }

    @Override
    public void onConnected(TCPDevice device) {

    }

    @Override
    public void onDisconnected(TCPDevice device) {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        hostRetriever.start();
    }

    @Override
    public void onReceived(TCPDevice device, String data) {
        if (data.contains("prepare_stream")) {
            File downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "vr-videos/" + data.split("@")[2]);
            download.startDownload(data.split("@")[1], downloadFile, new Download.Callback() {
                @Override
                public void onStarted() {
                    client.getDevice().send("preparing_started");
                }

                @Override
                public void onCanceled() {

                }

                @Override
                public void onFinished(File file) {
                    if (file != null && file.exists()) {
                        client.getDevice().send("preparing_finished");
                        videoFile = file;
                    }
                }

                @Override
                public void onDownloading(int progress, int downloadedBytes, int totalBytes) {
                    client.getDevice().send(String.format("preparing_progress@%s@%s@%s", progress, downloadedBytes, totalBytes));
                }

                @Override
                public void onFailed(Exception e) {
                    client.getDevice().send("preparing_failed");
                }

                @Override
                public void onNetworkUnavailable() {

                }
            });
        } else if (data.contains("play_stream")) {
            if (videoFile != null && videoFile.exists())
                setContent(Uri.fromFile(videoFile));
        }
    }


    @Override
    public void onVRPrepared(VrView vrView) {
        vrView.start();
    }
}
