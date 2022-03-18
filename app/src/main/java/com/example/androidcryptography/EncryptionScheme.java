package com.example.androidcryptography;

public abstract class EncryptionScheme {
    // Template
    public abstract String schemeName();
    public abstract String encrypt(String raw);
    public abstract String decrypt(String coded);

    @Override
    public String toString() {
        return this.schemeName();
    }

    // Schemes
}
