package com.example.androidcryptography;

// Plaintext implementation, no encryption.

public class Plaintext extends EncryptionScheme {
    @Override
    public String schemeName() { return "Plaintext"; }

    @Override
    public String encrypt(String raw) {
        return raw;
    }

    @Override
    public String decrypt(String coded) {
        return coded;
    }
}