package ru.liner.vr360client.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hengyi.fastvideoplayer.library.FastVideoPlayer;

import java.io.IOException;
import java.net.InetAddress;

import ru.liner.vr360client.CoreActivity;
import ru.liner.vr360client.R;
import ru.liner.vr360client.tcp.IMulticastCallback;
import ru.liner.vr360client.tcp.ITCPCallback;
import ru.liner.vr360client.tcp.TCPClient;
import ru.liner.vr360client.tcp.TCPDevice;
import ru.liner.vr360client.tcp.UDPMulticast;
import ru.liner.vr360client.utils.Constant;
import ru.liner.vr360client.utils.Networks;
import ru.liner.vr360client.views.LImageButton;


public class MainActivity extends CoreActivity implements ITCPCallback {
    private FastVideoPlayer videoPlayer;
    private TextView logView;
    private TCPClient tcpClient;
    private UDPMulticast udpMulticast;
    private LImageButton syncDevicesButton;

    private void setupSocketConnection() throws IOException {
        udpMulticast = new UDPMulticast(Constant.SERVER_IP_PUBLISHER_HOST, Constant.SERVER_MULTICAST_PORT);
        udpMulticast.setMulticastCallback(new IMulticastCallback() {
            @Override
            public void onReceived(String data) {
                if (Networks.isValidHost(data) && tcpClient == null) {
                    tcpClient = new TCPClient(data, Constant.SERVER_TCP_CONNECTION_PORT);
                    tcpClient.connect(MainActivity.this);
                }
            }
        });
        udpMulticast.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logView = findViewById(R.id.logView);
        videoPlayer = findViewById(R.id.videoPlayer);
        syncDevicesButton = findViewById(R.id.syncDevicesButton);
        videoPlayer.setLive(true);
        videoPlayer.setScaleType(FastVideoPlayer.SCALETYPE_16_9);
        videoPlayer.setTitle("");
        videoPlayer.setHideControl(true);


        try {
            setupSocketConnection();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot create stable connection! Try again later.", Toast.LENGTH_SHORT).show();
            finish();
        }
        syncDevicesButton.setClickCallback(new LImageButton.Callback() {
            @Override
            public void onClick(LImageButton button) {
                try {
                    setupSocketConnection();
                    if(tcpClient != null)
                        tcpClient.connect(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!udpMulticast.isRunning())
            udpMulticast.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (udpMulticast.isRunning())
            udpMulticast.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tcpClient.isConnected())
            tcpClient.getDevice().send("disconnect");
    }

    @Override
    public void onConnected(TCPDevice device) {
        udpMulticast.stop();
        appendLog("Connection with http:/" + device.getInetAddress().toString() + " successful!");
    }

    @Override
    public void onDisconnected(InetAddress inetAddress) {
        udpMulticast.start();
    }

    @Override
    public void onConnectionFailed(InetAddress inetAddress) {
        Toast.makeText(this, "Connection with http:/" + inetAddress.toString() + " failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceived(InetAddress inetAddress, byte[] data) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, String data) {
        switch (data) {
            case "play":
                appendLog(data + " http://" + Networks.getHost(inetAddress) + ":" + Constant.SERVER_STREAM_VIDEO_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(videoPlayer.getUrl()))
                            videoPlayer.setUrl("http://" + Networks.getHost(inetAddress) + ":" + Constant.SERVER_STREAM_VIDEO_PORT);
                        videoPlayer.play();
                    }
                });
                break;
            case "pause":
                appendLog(data + " http://" + Networks.getHost(inetAddress) + ":" + Constant.SERVER_STREAM_VIDEO_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoPlayer.pause();
                    }
                });
                break;
            case "stop":
                appendLog(data + " http://" + Networks.getHost(inetAddress) + ":" + Constant.SERVER_STREAM_VIDEO_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoPlayer.stop();
                    }
                });
                break;
            case "disconnect":
                appendLog("Disconnected from: http:/" + inetAddress.toString() + " successful!");
                tcpClient.getDevice().send("disconnect");
                videoPlayer.stop();
                videoPlayer.hide(true);
                break;
        }
    }


    private void appendLog(String text) {
        Log.d("ClientVR", text);
        logView.post(new Runnable() {
            @Override
            public void run() {
                logView.setText(String.format("%s%s\n", logView.getText().toString(), text));
            }
        });
    }
}
