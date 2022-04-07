package com.example.androidcryptography;

import java.util.Arrays;

// Implementation of Scytale, loads all characters in to a grid reading left-right then top-bottom, reads them back
// top-bottom, left-right as an encryption. Process is reversed for decryption

public class Scytale extends EncryptionScheme {

    public static final int UI_MAX_ROWS = 10;

    public int rows = 5;

    @Override
    public String schemeName() { return "Scytale"; }
    @Override
    public String encrypt(String raw) {
        raw = raw.replaceAll("\\s", "").toUpperCase(); // remove all spaces, taken from https://www.geeksforgeeks.org/how-to-remove-all-white-spaces-from-a-string-in-java/
        char[] arr = raw.toCharArray(); // convert to char array
        if (arr.length == 0) return ""; // return empty

        int area = ceilToMultiple(arr.length, rows);
        int cols = area/rows; // find col
        int rem = area - arr.length; // remainder
        int pdgbgn = rows - rem;
        System.out.println(area + ", " + rem + ", " + pdgbgn);
        if (pdgbgn < 0) throw new IllegalArgumentException("ERROR!");
        char[][] grid = new char[cols][rows]; // create grid
        for (char[] a : grid) { Arrays.fill(a, '@'); } // fill with @
        int x=0, y=0;
        for (int i=0;i<arr.length;i++) { // populate with my text
            grid[x][y] = arr[i];
            x++;
            if (x >= (y>=pdgbgn ? cols-1 : cols)) {
                y++;
                x = 0;
            }
            if (y >= rows) break;
        }
        char[] enc = new char[arr.length]; // read downwards
        x = 0;
        y = 0;
        for (int i=0;i<enc.length;i++) {
            enc[i] = grid[x][y];
            y++;
            if (y >= rows) {
                y = 0;
                x++;
            }
//            if (x >= cols) break;
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

    private int ceilToMultiple(int a, int f) {
        int m = a % f;
        return m == 0 ? a : (a + f - m);
    }
}