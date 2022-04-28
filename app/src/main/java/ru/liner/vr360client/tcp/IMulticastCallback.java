package ru.liner.vr360client.tcp;

public interface IMulticastCallback {
    default void onStarted(){}
    default void onStopped(){}
    default void onReceived(byte[] data){}
    default void onReceived(String data){}
}
