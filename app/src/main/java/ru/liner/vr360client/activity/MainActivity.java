package ru.liner.vr360client.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.download.library.DownloadImpl;
import com.xojot.vrplayer.Media;
import com.xojot.vrplayer.VrView;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import ru.liner.vr360client.R;
import ru.liner.vr360client.tcp.HostRetriever;
import ru.liner.vr360client.tcp.TCPClient;
import ru.liner.vr360client.utils.Constant;
import ru.liner.vr360client.utils.Networks;
import ru.liner.vr360client.utils.VrModeChanger;
import ru.liner.vr360client.views.VrDialog;


@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, TCPClient.Callback, VrModeChanger.Callback {
    private VrView vrView;
    private VrDialog vrDialog;
    private boolean isInVRMode;
    private int pausePosition;
    private String host;
    private TCPClient client;
    private HostRetriever hostRetriever;

    private File videoFile;
    private boolean freshVideo;
    private boolean connected;
    private VrModeChanger vrModeChanger;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle bundle) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        vrView = findViewById(R.id.vrView);
        vrDialog = findViewById(R.id.vrDialog);
        vrModeChanger = new VrModeChanger(this, vrView);
        vrModeChanger.setCallback(this);
        try {
            hostRetriever = new HostRetriever(Constant.SERVER_IP_PUBLISHER_HOST, Constant.SERVER_MULTICAST_PORT) {
                @Override
                public boolean accept(String host) {
                    if (client == null)
                        client = new TCPClient(host, Constant.SERVER_TCP_CONNECTION_PORT);
                    client.start(MainActivity.this);
                    return true;
                }
            };
            hostRetriever.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onVRPrepared(VrView vrView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                vrModeChanger.stop();
                client.send("preparing_finished");
            }
        });
    }

    private void setContent(Uri uri) {
        vrModeChanger.start();
        vrView.setDataSource(uri);
        vrView.setAutoPanning(false);
        vrView.setCameraReset(false);
    }


    @Override
    public void onResume() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
        getWindow().getDecorView().setSystemUiVisibility(4870);
        super.onResume();
        vrView.onResume();
        vrView.seekTo(pausePosition);
        if (client != null && !client.isConnected())
            client.start(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        vrView.pause();
        pausePosition = vrView.getCurrentPosition();
        vrView.onPause();
        if (client != null && client.isConnected())
            client.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vrView.pause();
        pausePosition = vrView.getCurrentPosition();
        vrView.onPause();
        if (client != null && client.isConnected())
            client.stop();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if ((i & 4) == 0) {
            getWindow().getDecorView().setSystemUiVisibility(4870);
        }
    }



    @Override
    public void onStarted(Socket socket) {
        TCPClient.Callback.super.onStarted(socket);

        new VrDialog.Builder(vrDialog)
                .setIconRes(R.drawable.info_icon)
                .setTitle("Searching host")
                .setText("Please wait while client search streaming host")
                .setIndeterminate(true)
                .show();
    }

    @Override
    public void onConnected(Socket socket) {
        TCPClient.Callback.super.onConnected(socket);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hostRetriever.stop();
                new VrDialog.Builder(vrDialog)
                        .setIconRes(R.drawable.info_icon)
                        .setTitle("Connected")
                        .setText("Successfully connected to host")
                        .setIndeterminate(true)
                        .show(2000);
            }
        });
    }

    @Override
    public void onDisconnected(Socket socket) {
        TCPClient.Callback.super.onDisconnected(socket);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hostRetriever.start();
                vrView.pause();
                pausePosition = vrView.getCurrentPosition();
                vrView.onPause();
                DownloadImpl.getInstance(getApplicationContext()).cancelAll();
                new VrDialog.Builder(vrDialog)
                        .setIconRes(R.drawable.warning_icon)
                        .setTitle("Disconnected")
                        .setText("Client has been disconnected from host")
                        .setIndeterminate(true)
                        .show(1000);
            }
        });

    }

    @Override
    public void onReceived(Socket socket, String string) {
        TCPClient.Callback.super.onReceived(socket, string);
        if (string.contains("prepare_stream")) {
            freshVideo = true;
            String url = string.split("@")[1];
            String name = string.split("@")[2];
            File target = new File(getFilesDir(), name);
            if(target.exists())
                target.delete();
            Networks.saveUrl(target, url, new Networks.DownloadCallback() {
                @Override
                public void onDownloaded(File file) {
                    setContent(Uri.fromFile(file));
                }

                @Override
                public void onFailed() {
                    client.send("preparing_failed");
                }

                @Override
                public void onDownload(int progress, int downloadedBytes, int totalBytes, int bytesPerSec) {
                    client.send(String.format("preparing_progress@%s@%s@%s@%s", progress, downloadedBytes, totalBytes, bytesPerSec));
                }
            });
        } else if (string.contains("play_stream")) {
            vrView.start();
        } else if (string.contains("disconnect")) {
            client.stop();
            finish();
        } else if (string.contains("pause_stream")) {
            vrView.pause();
            pausePosition = vrView.getCurrentPosition();
        } else if (string.contains("check_ping")) {
            client.send("answer_ping");
        }
    }

    @Override
    public void onStopped(Socket socket) {
        TCPClient.Callback.super.onStopped(socket);
    }


}
