package ru.liner.vr360client.tcp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.CallSuper;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class TCPClient {
    private static final String TAG = TCPClient.class.getSimpleName();
    private Callback callback;
    private String host;
    private int port;
    private Socket socket;
    private boolean connected;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start(Callback callback) {
        this.callback = callback;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    callback.onConnected(socket);
                    connected = true;
                    try {
                        InputStream inputStream = socket.getInputStream();
                        byte[] content = new byte[2048];
                        if (inputStream != null) {
                            while (true) {
                                try {
                                    int bytesRead = inputStream.read(content);
                                    if (bytesRead == -1)
                                        break;
                                    callback.onReceived(socket, Arrays.copyOfRange(content, 0, bytesRead));
                                    callback.onReceived(socket, new String(Arrays.copyOfRange(content, 0, bytesRead)));
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                        callback.onDisconnected(socket);
                        connected = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
            callback.onStarted(socket);
        } catch (Exception e) {

        }
    }

    public void stop() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                callback.onStopped(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }
        }
    }

    public void send(byte[] bytes) {
        if (bytes != null && socket != null && socket.isConnected()) {
            try {
                socket.getOutputStream().write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String string) {
        send(string.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isConnected() {
        return connected;
    }


    public interface Callback {
        @CallSuper
        default void onStarted(Socket socket) {
            Log.d(TAG, "onStarted: "+socket.toString());
            
        }

        @CallSuper
        default void onConnected(Socket socket) {
            Log.d(TAG, "onConnected: "+socket.toString());

        }

        @CallSuper
        default void onReceived(Socket socket, byte[] bytes) {
        }

        @CallSuper
        default void onReceived(Socket socket, String string) {
            if(!string.equals("check_ping"))
            Log.d(TAG, "onReceived: "+socket.getInetAddress().toString()+" | "+string);

        }

        @CallSuper
        default void onDisconnected(Socket socket) {
            Log.d(TAG, "onDisconnected: "+socket.toString());

        }

        @CallSuper
        default void onStopped(Socket socket) {
            Log.d(TAG, "onStopped: "+socket.toString());
        }
    }
}
