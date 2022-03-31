package com.example.androidcryptography;

import java.util.Arrays;
import java.util.Locale;

public class Scytale extends EncryptionScheme {

    public static final int UI_MAX_ROWS = 10;

    public int rows = 4; // TODO: Make adjustable in UI

    @Override
    public String schemeName() { return "Scytale"; }
    @Override
    public String encrypt(String raw) {
        raw = raw.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = raw.toCharArray(); // convert to char array
        if (arr.length == 0) return ""; // return empty

        int cols = (int) Math.ceil((double)raw.length() / (double)rows); // find col
        char[][] grid = new char[cols][rows]; // create grid
        for (char[] a : grid) { Arrays.fill(a, '@'); } // fill with @
        for (int i=0;i<arr.length;i++) { // populate with my text
            grid[i%cols][i/cols] = arr[i];
        }
        char[] enc = new char[rows*cols]; // read downwards
        for (int i=0;i<enc.length;i++) {
            enc[i] = grid[i/rows][i%rows];
        }
        return new String(enc); // return
    }
    @Override
    public String decrypt(String coded) {
        coded = coded.trim(); // trim all whitespace at ends
        char[] arr = coded.toCharArray();
        if (arr.length == 0) return ""; // return empty

        int cols = (int) Math.ceil((double)coded.length() / (double)rows);
        char[][] grid = new char[cols][rows];
        for (char[] a : grid) { Arrays.fill(a, ' '); } // fill with @
        for (int i=0;i<arr.length;i++) { // populate with my text
            grid[i/rows][i%rows] = arr[i];
        }
        char[] dec = new char[rows*cols]; // read downwards
        for (int i=0;i<dec.length;i++) {
            dec[i] = grid[i%cols][i/cols]; // place back into 1 dimensional array
        }
        return new String(dec).replaceAll("[\\s@]", ""); // return trimmed decoded, remove @
    }
}