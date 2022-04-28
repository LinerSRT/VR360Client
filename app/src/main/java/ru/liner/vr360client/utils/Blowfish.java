package ru.liner.vr360client.utils;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 23.04.2022, суббота
 **/
public class Blowfish {
    private static final String BLOWFISH_ALGORITHM = "Blowfish";

    @NonNull
    public static String encode(@NonNull String content, @NonNull String key) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), BLOWFISH_ALGORITHM);
        Cipher cipher = Cipher.getInstance(BLOWFISH_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    @NonNull
    public static String decode(@NonNull String data, @NonNull String key) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), BLOWFISH_ALGORITHM);
        Cipher cipher = Cipher.getInstance(BLOWFISH_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
