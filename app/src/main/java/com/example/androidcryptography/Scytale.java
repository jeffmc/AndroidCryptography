package com.example.androidcryptography;

import java.util.Arrays;

public class Scytale extends EncryptionScheme {

    public int rows = 3; // TODO: Make adjustable in UI

    @Override
    public String schemeName() { return "Scytale"; }
    @Override
    public String encrypt(String raw) {
        raw = raw.trim(); // trim all whitespace at start and end
        char[] arr = raw.toCharArray(); // convert to char array
        if (arr.length == 0) return ""; // return empty

        int cols = ( raw.length() / rows ) + 1; // find col
        char[][] grid = new char[cols][rows]; // create grid
        for (int i=0;i<grid.length;i++) { Arrays.fill(grid[i], '@'); } // fill with @
        for (int i=0;i<arr.length;i++) { // populate with my text
            grid[i%cols][i/cols] = arr[i];
        }
        for (int y=0;y<rows;y++) {
            for (int x=0;x<cols;x++) {
                System.out.print(grid[x][y]);
            }
            System.out.println();
        }
        char[] enc = new char[rows*cols]; // read downwards
        for (int i=0;i<enc.length;i++) {
            enc[i] = grid[i/rows][i%rows];
        }
        return new String(enc); // return
    }
    @Override
    public String decrypt(String coded) {
        return ""; // TODO: Add decryption
    }
}