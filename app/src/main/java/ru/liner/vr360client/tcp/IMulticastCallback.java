package ru.liner.vr360client.tcp;

public interface IMulticastCallback {
    default void onStarted(UDPMulticast multicast){}
    default void onStopped(UDPMulticast multicast){}
    default void onReceived(UDPMulticast multicast, byte[] data){}
    default void onReceived(UDPMulticast multicast, String data){}
}
