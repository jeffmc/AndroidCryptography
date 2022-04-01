package com.example.androidcryptography;

import android.text.Editable;
import android.text.TextWatcher;

public class Vigenere extends EncryptionScheme {

    public String key = "java";

    @Override
    public String schemeName() { return "Vigenere"; }

    public static int[] getKeyShiftArray(String str) {
        str = str.toUpperCase().replaceAll("[^A-Z]", ""); // Remove all non-letters, make uppercase.
        int[] shifts = new int[str.length()];
        if (shifts.length < 1) return new int[]{ 0 };
        for (int i=0;i<shifts.length;i++) {
            int idx = Caesar.ALPHABET.indexOf(str.charAt(i));
            if (idx < 0) throw new IllegalArgumentException("THIS SHOULDN'T HAPPEN!");
            shifts[i] = idx;
        }
        return shifts;
    }

    @Override
    public String encrypt(String raw) {
        raw = raw.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = raw.toCharArray();

        int[] shifts = getKeyShiftArray(key);
        char[] enc = new char[arr.length];
        for (int i=0;i<enc.length;i++) {
            int idx = Caesar.ALPHABET.indexOf(arr[i]);

            int shift = shifts[i%shifts.length];

            enc[i] = idx >= 0 ? Caesar.ALPHABET.charAt((idx+shift)%Caesar.ALPHABET.length()) : arr[i];
        }

        return new String(enc);
    }

    @Override
    public String decrypt(String coded) {
        coded = coded.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = coded.toCharArray();

        int[] shifts = getKeyShiftArray(key);
        char[] enc = new char[arr.length];
        for (int i=0;i<enc.length;i++) {
            int idx = Caesar.ALPHABET.indexOf(arr[i]);

            int shift = shifts[i%shifts.length];

            int shi = (idx - shift) % Caesar.ALPHABET.length();
            int shifted = shi < 0 ? Caesar.ALPHABET.length() + shi : shi;

            enc[i] = idx >= 0 ? Caesar.ALPHABET.charAt(shifted) : arr[i];
        }

        return new String(enc);
    }
}