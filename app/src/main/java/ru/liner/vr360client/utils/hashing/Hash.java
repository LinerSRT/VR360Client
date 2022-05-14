package ru.liner.vr360client.utils.hashing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import ru.liner.vr360client.utils.InputStreams;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class Hash {
    public static String get(@NonNull File file, @HashAlgorithm String hashAlgorithm){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            InputStream inputStream = InputStreams.get(file);
            byte[] bytes = new byte[1024];
            int read;
            while ((read = inputStream.read(bytes)) != -1)
                messageDigest.update(bytes, 0, read);
            byte[] digestBytes = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte digestByte : digestBytes)
                stringBuilder.append(Integer.toString((digestByte & 0xff) + 0x100, 16).substring(1));
            return stringBuilder.toString().toUpperCase(Locale.getDefault());
        } catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
            return "";
        }
    }
}
