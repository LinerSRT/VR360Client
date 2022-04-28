package ru.liner.vr360client.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends CoreActivity implements ITCPCallback {
    private TextView logView;
    private TCPClient tcpClient;
    private UDPMulticast udpMulticast;

    private void setupSocketConnection() throws IOException {
        udpMulticast = new UDPMulticast(Constant.IP_REQUEST, Constant.MULTICAST_PORT);
        udpMulticast.setMulticastCallback(new IMulticastCallback() {
            @Override
            public void onReceived(String data) {
                if (Networks.isValidHost(data) && tcpClient == null) {
                    tcpClient = new TCPClient(data, Constant.TCP_CONNECTION_PORT);
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
        try {
            setupSocketConnection();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot create stable connection! Try again later.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!udpMulticast.isRunning())
            udpMulticast.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(udpMulticast.isRunning())
            udpMulticast.stop();
    }

    @Override
    public void onConnected(TCPDevice device) {
        udpMulticast.stop();
    }

    @Override
    public void onDisconnected(InetAddress inetAddress) {
        udpMulticast.start();
    }

    @Override
    public void onConnectionFailed(InetAddress inetAddress) {
        Toast.makeText(this, "Connection with http:/"+inetAddress.toString()+" failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceived(InetAddress inetAddress, byte[] data) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, String data) {

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
