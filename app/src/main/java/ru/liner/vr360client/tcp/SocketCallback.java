package ru.liner.vr360client.tcp;

import java.net.InetAddress;
import java.util.List;

public interface SocketCallback {
    default void onConnected(TCPDevice device){}
    default void onDisconnected(TCPDevice device){}
    default void onConnectionFailed(TCPDevice device){}
    default void onReceived(TCPDevice device, byte[] data){}
    default void onReceived(TCPDevice device, String data){}
}
