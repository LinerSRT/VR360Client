package ru.liner.vr360client.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class Networks {
    @Nullable
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                        return inetAddress.getHostAddress();
                }
            }
            return null;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean isValidHost(String host){
        if(TextUtils.isEmpty(host))
            return false;
        Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher matcher = pattern.matcher(host);
        return matcher.find();
    }

    public static String getHost(InetAddress inetAddress){
        return inetAddress.getHostAddress().replaceFirst("/", "");
    }
}
