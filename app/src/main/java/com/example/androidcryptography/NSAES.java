package com.example.androidcryptography;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

// NSAES (Not-So-Advanced Encryption Standard) derived from AES https://en.wikipedia.org/wiki/Advanced_Encryption_Standard
public class NSAES extends EncryptionScheme {

    public String strKey = "defaultSecureKey"; // Limited to B64URL characters
    private byte[] key; // Actively derived from strKey in UI impl

    public static final int ROUNDS = 4;
    public static final int BLOCK_SIZE = 4;
    public static final int BLOCK_AREA = BLOCK_SIZE*BLOCK_SIZE;

    public Charset charset = StandardCharsets.UTF_8;

    private static JeffBase64 jb64 = new JeffBase64();

    public static byte[] randomKey(long seed) {
        Random r = new Random(seed);
        byte[] key = new byte[ROUNDS*BLOCK_AREA];
        r.nextBytes(key);
        return key;
    }

    @Override
    public String schemeName() { return "AES"; }

    @Override
    public String encrypt(String raw) {
        ByteBuffer rawBuffer = ByteBuffer.allocate(raw.length() + BLOCK_AREA - raw.length() % BLOCK_AREA); // round up by block area
        rawBuffer.put(raw.getBytes(charset));
        rawBuffer.position(0);

        ByteBuffer codedBuffer = ByteBuffer.allocate(rawBuffer.capacity());
        byte[] arr = new byte[BLOCK_AREA];
        while (rawBuffer.hasRemaining()) {
            rawBuffer.get(arr);
            encryptBlock(arr);
            codedBuffer.put(arr);
        }
        return jb64.encode(codedBuffer.array());
    }

    private void encryptBlock(byte[] blk) {
        if (key.length!=ROUNDS*BLOCK_AREA) throw new IllegalStateException("Invalid Key!");
        for (int r=0;r<ROUNDS;r++) {
            int start = r*BLOCK_AREA;
            byte[] subkey = Arrays.copyOfRange(key, start, start + BLOCK_AREA);
            subBytes(blk, subkey[0]);
            shiftRows(blk, true);
//			mixColumns(blk); TODO: Understand this
            addKey(blk, subkey);
        }
    }

    @Override
    public String decrypt(String coded) {
        ByteBuffer cipherBuffer = ByteBuffer.wrap(jb64.decode(coded));
        if (cipherBuffer.capacity()%BLOCK_AREA!=0)
            throw new IllegalArgumentException("Incomplete cipherBuffer length=" + cipherBuffer.capacity());

        ByteBuffer decodedBuffer = ByteBuffer.allocate(cipherBuffer.capacity());
        byte[] arr = new byte[BLOCK_AREA];
        while (cipherBuffer.hasRemaining()) {
            cipherBuffer.get(arr);
            decryptBlock(arr);
            decodedBuffer.put(arr);
        }
        return new String(decodedBuffer.array(), charset);
    }

    private void decryptBlock(byte[] blk) {
        if (key.length!=ROUNDS*BLOCK_AREA) throw new IllegalStateException("Invalid Key!");
        for (int r=ROUNDS-1;r>=0;r--) {
            int start = r*BLOCK_AREA;
            byte[] subkey = Arrays.copyOfRange(key, start, start + BLOCK_AREA);
            addKey(blk, subkey);
            shiftRows(blk, false);
            subBytes(blk, (byte) -subkey[0]);
//			mixColumns(blk); TODO: Understand this
        }
    }

    private void subBytes(byte[] blk, byte shift) { // TODO: Implement real affine transforms and galois field
        if (blk.length != BLOCK_AREA) throw new IllegalArgumentException("Array length is " + blk.length);
        for (int i=0;i<blk.length;i++) {
            blk[i]+=shift;
        }
    }

    private void shiftRows(byte[] blk, boolean direction) { // Offset each row by its y coordinate within square block
        if (blk.length != BLOCK_AREA) throw new IllegalArgumentException("Array length is " + blk.length);
        for (int y=1;y<BLOCK_SIZE;y++) {
            int fi = y*BLOCK_SIZE;
            byte[] row = Arrays.copyOfRange(blk, fi, fi+BLOCK_SIZE);
            for (int x=0;x<BLOCK_SIZE;x++) blk[fi+x] = row[(x + (direction?y:BLOCK_SIZE-y)) % BLOCK_SIZE];
        }
    }

//	private void mixColumns(byte[] blk) { // Fix this
//		if (blk.length != BLOCK_AREA) throw new IllegalArgumentException("Array length is " + blk.length);
//		for (int x=0;x<BLOCK_SIZE;x++) {
//			byte[] col = new byte[BLOCK_SIZE];
//			for (int y=0;y<BLOCK_SIZE;y++) col[y] = blk[x+y*BLOCK_SIZE];
//			mixColumn(col);
//			for (int y=0;y<BLOCK_SIZE;y++) blk[x+y*BLOCK_SIZE] = col[y];
//		}
//	}
//
//	private void mixColumn(byte[] r) { // https://en.wikipedia.org/wiki/Rijndael_MixColumns#Implementation_example
//		if (r.length != BLOCK_SIZE) throw new IllegalArgumentException("Array length is " + r.length);
//	    byte[] a = new byte[4];
//	    byte[] b = new byte[4];
//	    int h;
//	    /* The array 'a' is simply a copy of the input array 'r'
//	     * The array 'b' is each element of the array 'a' multiplied by 2
//	     * in Rijndael's Galois field
//	     * a[n] ^ b[n] is element n multiplied by 3 in Rijndael's Galois field */
//	    for (byte c = 0; c < 4; c++) {
//	        a[c] = r[c];
//	        /* h is 0xff if the high bit of r[c] is set, 0 otherwise */
//	        h = (r[c] >> 7) & 1; /* arithmetic right shift, thus shifting in either zeros or ones */
//	        b[c] = (byte) (r[c] << 1); /* implicitly removes high bit because b[c] is an 8-bit char, so we xor by 0x1b and not 0x11b in the next line */
//	        b[c] ^= h * 0x1B; /* Rijndael's Galois field */
//	    }
//	    r[0] = (byte) (b[0] ^ a[3] ^ a[2] ^ b[1] ^ a[1]); /* 2 * a0 + a3 + a2 + 3 * a1 */
//	    r[1] = (byte) (b[1] ^ a[0] ^ a[3] ^ b[2] ^ a[2]); /* 2 * a1 + a0 + a3 + 3 * a2 */
//	    r[2] = (byte) (b[2] ^ a[1] ^ a[0] ^ b[3] ^ a[3]); /* 2 * a2 + a1 + a0 + 3 * a3 */
//	    r[3] = (byte) (b[3] ^ a[2] ^ a[1] ^ b[0] ^ a[0]); /* 2 * a3 + a2 + a1 + 3 * a0 */
//	}

    private void addKey(byte[] arr, byte[] rndkey) { // XOR with key
        for (int i=0;i<BLOCK_AREA;i++) arr[i] ^= rndkey[i];
    }

}
