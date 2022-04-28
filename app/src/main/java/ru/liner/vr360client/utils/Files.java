package ru.liner.vr360client.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 21.04.2022, четверг
 **/
public class Files {
    private static final byte[] SECURE_SEQUENCE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    public static void encodeFile(@NonNull File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        byte[] firstBytes = new byte[100];
        randomAccessFile.read(firstBytes, 0, 100);
        byte[] encodedBytes = new byte[100];
        for (int i = 0; i < 100; i++) {
            encodedBytes[i] = (byte) (firstBytes[i] ^ SECURE_SEQUENCE[i % 10]);
        }
        randomAccessFile.seek(0);
        randomAccessFile.write(encodedBytes, 0, 100);
        randomAccessFile.close();
    }

    public static void decodeFile(@NonNull File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        byte[] encodedBytes = new byte[100];
        randomAccessFile.read(encodedBytes, 0, 100);
        byte[] decodedBytes = new byte[100];
        for (int i = 0; i < 100; i++) {
            decodedBytes[i] = (byte) (encodedBytes[i] ^ SECURE_SEQUENCE[i % 10]);
        }
        randomAccessFile.seek(0);
        randomAccessFile.write(decodedBytes, 0, 100);
        randomAccessFile.close();
    }



    private static String getMIMEType(@NonNull File file) {
        String type = "*/*";
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0)
            return type;
        String end = fileName.substring(dotIndex);
        if (TextUtils.isEmpty(end) && end.length() < 2)
            return type;
        end = end.substring(1).toLowerCase();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        type = mimeTypeMap.getMimeTypeFromExtension(end);
        return type;
    }

    @Nullable
    public static File getFileFromUri(Uri uri) {
        if (uri == null)
            return null;
        if ((uri.getScheme().equals("file")) && (!TextUtils.isEmpty(uri.getPath())))
            return new File(uri.getPath());
        return null;
    }

    public static void copyFile(@NonNull File source, @NonNull File destination) throws IOException {
        try (InputStream inputStream = new FileInputStream(source); OutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, length);
        }
    }

    public static void downloadFile(@NonNull File saveFile, @NonNull String downloadLink) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(downloadLink).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            byte[] data = new byte[1024];
            int read;
            while ((read = bufferedInputStream.read(data, 0, 1024)) != -1)
                fileOutputStream.write(data, 0, read);
            fileOutputStream.close();
            bufferedInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static String readFile(@NonNull File file) {
        try {
            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                stringBuffer.append(String.valueOf(buffer, 0, read));
            }
            reader.close();
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(@NonNull File file, @NonNull String content) {
        if (file.exists())
            if (!file.delete()) {
            }
        try {
            if (!file.createNewFile()) {
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(content.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean ensureDirectory(@NonNull File directory) {
        if (!directory.exists()) {
            return directory.mkdir();
        }
        return true;
    }


    @Nullable
    public static String getRealPathFromURI(@NonNull Context context, @NonNull Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
