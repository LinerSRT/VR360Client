package ru.liner.vr360client.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
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

    public static boolean isValidHost(String host) {
        if (TextUtils.isEmpty(host))
            return false;
        Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher matcher = pattern.matcher(host);
        return matcher.find();
    }

    public static String getHost(InetAddress inetAddress) {
        return inetAddress.getHostAddress().replaceFirst("/", "");
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null)
                isConnected = activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }

    public static void saveUrl(File destination, String urlString, DownloadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int contentLength = connection.getContentLength();
                    InputStream inputStream = url.openStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(destination);
                    final byte[] data = new byte[1024];
                    int downloaded = 0;
                    int downloadPercent = 0;
                    int count;
                    long startTime = System.currentTimeMillis();
                    while ((count = bufferedInputStream.read(data, 0, 1024)) != -1) {
                        fileOutputStream.write(data, 0, count);
                        downloaded += count;
                        int currentPercent = Math.round(((float) downloaded / contentLength) * 100f);
                        if (currentPercent != downloadPercent) {
                            downloadPercent = currentPercent;
                            long elapsedTime = System.currentTimeMillis() - startTime + 1;
                            long bps = downloaded / elapsedTime;

                            callback.onDownload(downloadPercent, downloaded, contentLength, Math.abs((int) bps * 1024));

                        }
                    }
                    callback.onDownloaded(destination);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailed();
                }
            }
        }).start();

    }

    public interface DownloadCallback {
        void onDownloaded(File file);

        void onFailed();

        void onDownload(int progress, int downloadedBytes, int totalBytes, int bytesPerSec);
    }

}
