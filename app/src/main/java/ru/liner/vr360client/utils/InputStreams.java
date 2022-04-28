package ru.liner.vr360client.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class InputStreams {
    private static final int BYTE_BUFFER_SIZE = 1024;
    private static final int BYTE_READ_ERROR = -1;

    public static void  writeBytes(@NonNull OutputStream outputStream, byte[] bytes){
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[BYTE_READ_ERROR];
            int readStatus;
            while ((readStatus = byteArrayInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, readStatus);
            }
            byteArrayInputStream.close();
            bufferedOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static byte[] toBytes(@NonNull InputStream inputStream){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int readStatus;
            byte[] bytes = new byte[BYTE_BUFFER_SIZE];
            while ((readStatus = inputStream.read(bytes, 0, bytes.length)) != BYTE_READ_ERROR) {
                byteArrayOutputStream.write(bytes, 0, readStatus);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e){
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static InputStream get(Context context, String path, boolean external) throws IOException {
        return (external)?get(path):get(context, path);
    }

    public static InputStream get(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public static InputStream get(String path) throws FileNotFoundException {
        return get(new File(path));
    }

    public static InputStream get(Context context, String path) throws IOException {
        return context.getAssets().open(path);
    }

    public static String toString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line).append("\n");
        reader.close();
        return sb.toString();
    }
}
