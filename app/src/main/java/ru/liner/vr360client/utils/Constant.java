package ru.liner.vr360client.utils;

import android.os.Environment;

import java.io.File;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class Constant {
    public static final File videoDirectory = new File(Environment.getExternalStorageDirectory(), "VR-Videos");
    public static final int PACKET_SIZE = 100000;

    public static final String IP_REQUEST = "232.5.6.8";
    public static final int MULTICAST_PORT = 1234;
    public static final int TCP_CONNECTION_PORT = 9092;
    public static final int TCP_FILES_PORT = 9091;

}
