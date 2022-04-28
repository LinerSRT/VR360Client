package ru.liner.vr360client.tcp;

import java.net.InetAddress;

public interface ITCPCallback {
    void onConnected(TCPDevice device);
    void onDisconnected(InetAddress inetAddress);
    void onConnectionFailed(InetAddress inetAddress);
    default void onReceived(InetAddress inetAddress, byte[] data){}
    default void onReceived(InetAddress inetAddress, String data){}
}
