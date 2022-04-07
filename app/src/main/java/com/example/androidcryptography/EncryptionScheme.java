package com.example.androidcryptography;

// Outline for all implementations of scheme (not an interface because of toString override)

public abstract class EncryptionScheme {
    // Template
    public abstract String schemeName(); // User-readable
    public abstract String encrypt(String raw);
    public abstract String decrypt(String coded);

    @Override
    public String toString() {
        return this.schemeName();
    } // Utilized in Spinner UI to give user-readable string
}
