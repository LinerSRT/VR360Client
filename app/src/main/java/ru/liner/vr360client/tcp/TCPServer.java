package ru.liner.vr360client.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class TCPServer implements Runnable {
    private ITCPCallback itcpCallback;
    private final int port;
    private boolean connected;
    private final List<TCPDevice> tcpDeviceList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
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
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (connected) {
                try {
                    startClient(server.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(itcpCallback != null)
                        itcpCallback.onConnectionFailed(server.getInetAddress());
                }
            }
            tcpDeviceList.forEach(TCPDevice::stop);
            tcpDeviceList.clear();
            server.close();
            if(itcpCallback != null)
                itcpCallback.onDisconnected(server.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startClient(Socket socket) {
        TCPDevice tcpDevice = new TCPDevice(socket);
        ITCPCallback callback = new ITCPCallback() {
            @Override
            public void onConnected(TCPDevice device) {
                if(itcpCallback != null)
                itcpCallback.onConnected(device);
            }

            @Override
            public void onDisconnected(InetAddress inetAddress) {
                tcpDeviceList.remove(tcpDevice);
                if(itcpCallback != null)
                itcpCallback.onDisconnected(inetAddress);
            }

            @Override
            public void onConnectionFailed(InetAddress inetAddress) {
                if(itcpCallback != null)
                itcpCallback.onConnectionFailed(inetAddress);
            }

            @Override
            public void onReceived(InetAddress inetAddress, byte[] data) {
                if(itcpCallback != null)
                itcpCallback.onReceived(inetAddress, data);
            }

            @Override
            public void onReceived(InetAddress inetAddress, String data) {
                if(itcpCallback != null)
                itcpCallback.onReceived(inetAddress, data);
            }
        };
        tcpDevice.setITCPCallback(callback);
        tcpDevice.start();
        tcpDeviceList.add(tcpDevice);
        if(itcpCallback != null)
            itcpCallback.onConnected(tcpDevice);
    }
}
