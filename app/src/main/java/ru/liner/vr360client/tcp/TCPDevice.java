package ru.liner.vr360client.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class TCPDevice implements Runnable{
    private ITCPCallback itcpCallback;
    protected Socket socket;
    protected InetAddress inetAddress;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    private boolean connected;

    public TCPDevice(Socket socket, ITCPCallback itcpCallback) {
        this.itcpCallback = itcpCallback;
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
    }
    public TCPDevice(Socket socket) {
        this(socket, null);
    }

    public void setITCPCallback(ITCPCallback itcpCallback) {
        this.itcpCallback = itcpCallback;
    }

    public void start() {
        connected = true;
        new Thread(this).start();
    }

    public void stop() {
        connected = false;
        try {
            socket.shutdownInput();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
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
        if(itcpCallback != null && connected)
            itcpCallback.onConnected(this);
        while (connected){
           try {
               int dataLength = inputStream.available();
               if(dataLength > 0){
                   byte[] bytes = new byte[dataLength];
                   if(inputStream.read(bytes) != -1 && itcpCallback != null){
                       itcpCallback.onReceived(inetAddress, bytes);
                       itcpCallback.onReceived(inetAddress, new String(bytes));
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
        if(itcpCallback != null)
            itcpCallback.onDisconnected(inetAddress);
    }


    public InetAddress getInetAddress() {
        return inetAddress;
    }
}
