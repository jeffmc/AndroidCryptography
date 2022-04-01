package com.example.androidcryptography;

public class Caesar extends EncryptionScheme {

    public static final int UI_MAX_SHIFT = 26;

    public int shift = 3;

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String schemeName() { return "Caesar"; }

    @Override
    public String encrypt(String raw) {
        raw = raw.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = raw.toCharArray();

        char[] enc = new char[arr.length];
        for (int i=0;i<enc.length;i++) {
            int idx = ALPHABET.indexOf(arr[i]);

            enc[i] = idx >= 0 ? ALPHABET.charAt((idx+shift)%ALPHABET.length()) : arr[i];
        }

        return new String(enc);
    }

    @Override
    public String decrypt(String coded) {
        coded = coded.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = coded.toCharArray();

        char[] enc = new char[arr.length];
        for (int i=0;i<enc.length;i++) {
            int idx = ALPHABET.indexOf(arr[i]);

            int shi = (idx - shift) % ALPHABET.length();
            int shifted = shi < 0 ? ALPHABET.length() + shi : shi;

            enc[i] = idx >= 0 ? ALPHABET.charAt(shifted) : arr[i];
        }

        return new String(enc);
    }
}