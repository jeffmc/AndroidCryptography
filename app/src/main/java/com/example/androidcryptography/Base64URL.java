package com.example.androidcryptography;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Base64URL extends EncryptionScheme {

    public Charset charset = StandardCharsets.US_ASCII;
    private JeffBase64 jb64 = new JeffBase64();

    @Override
    public String schemeName() { return "Base64URL"; }

    @Override
    public String encrypt(String raw) {
        return jb64.encode(raw.getBytes(charset));
    }

    @Override
    public String decrypt(String coded) {
//        try {
        return new String(jb64.decode(coded), charset);
//        } catch (IllegalArgumentException e) {
//            return e.getMessage();
//        }
    }
}
