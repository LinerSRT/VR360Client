package ru.liner.vr360client.utils.hashing;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@StringDef(value = {HashAlgorithm.MD2, HashAlgorithm.MD5, HashAlgorithm.SHA, HashAlgorithm.SHA1, HashAlgorithm.SHA256, HashAlgorithm.SHA384, HashAlgorithm.SHA512})
@Retention(RetentionPolicy.SOURCE)
public @interface HashAlgorithm{
    String MD2 = "MD2";
    String MD5 = "MD5";
    String SHA = "SHA";
    String SHA1 = "SHA-1";
    String SHA256 = "SHA-256";
    String SHA384 = "SHA-384";
    String SHA512 = "SHA-512";
}