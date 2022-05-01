package ru.liner.vr360client.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360client.utils.Worker;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class TCPDevice implements Runnable{
    private SocketCallback socketCallback;
    protected Socket socket;
    protected InetAddress inetAddress;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    private final Timer connectionResolver;
    private boolean connectionAlive;
    private boolean connected;

    public TCPDevice(Socket socket, SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        this.connectionResolver = new Timer();
    }

    public TCPDevice(Socket socket) {
        this(socket, null);
    }

    public void setITCPCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    public void start() {
        connected = true;
        new Thread(this).start();
        connectionAlive = true;
        connectionResolver.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(connectionAlive){
                    send("check_connection");
                    connectionAlive = false;
                } else {
                    connected = false;
                }
            }
        }, 1000, 1000);
    }

    public void stop() {
        connected = false;
        connectionResolver.cancel();
        try {
            socket.shutdownInput();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected && connectionAlive;
    }

    public boolean send(String s) {
        return send(s.getBytes(StandardCharsets.UTF_8));
    }

    public boolean send(byte[] bytes) {
        if (outputStream != null)
            try {
                outputStream.write(bytes);
                outputStream.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }



    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if(socketCallback != null && connected)
            socketCallback.onConnected(this);
        while (connected){
           try {
               int dataLength = inputStream.available();
               if(dataLength > 0){
                   byte[] bytes = new byte[dataLength];
                   if(inputStream.read(bytes) != -1 && socketCallback != null){
                       String stringData = new String(bytes);
                       if (stringData.contains("check_connection")) {
                           send("connection_alive");
                       } else if (stringData.contains("connection_alive")){
                           connectionAlive = true;
                       } else{
                           socketCallback.onReceived(this, bytes);
                           socketCallback.onReceived(this, stringData);
                       }
                   }
               }
           } catch (IOException e){
               e.printStackTrace();
               connected = false;
           }
        }
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            inputStream = null;
            outputStream = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socketCallback != null)
            socketCallback.onDisconnected(this);
    }


    public InetAddress getInetAddress() {
        return inetAddress;
    }
}
