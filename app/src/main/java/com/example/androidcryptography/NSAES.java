package com.example.androidcryptography;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

// NSAES (Not-So-Advanced Encryption Standard) derived from AES https://en.wikipedia.org/wiki/Advanced_Encryption_Standard
// Still decently advanced, I just took a few shortcuts to make my implementation simpler;
// No mixing columns, no finite field affine transformations in substitute bytes step either,
// also key generation is nothing like round-key schedule in the real scheme.
public class NSAES extends EncryptionScheme {

    private long keySeed = 1234567890;
    private byte[] key; // Actively derived from keySeed in UI impl

    public static final int MIN_BLOCK_SIZE = 2;
    public static final int MAX_BLOCK_SIZE = 8;
    private int rounds = 4, blockSize = 4, blockArea = blockSize * blockSize;

    public Charset charset = StandardCharsets.US_ASCII;
    private JeffBase64 jb64 = new JeffBase64();

    private void generateKeyFromSeed() {
        Random r = new Random(keySeed);
        byte[] newKey = new byte[rounds * blockArea];
        r.nextBytes(newKey);
        this.key = newKey;
    }

    public NSAES() {
        generateKeyFromSeed();
    }

    public int blockSize() { return blockSize; }
    public void blockSize(int size) {
        blockSize = size;
        blockArea = size*size;
        generateKeyFromSeed();
    }
    public int getBlockArea() { return blockArea; }

    public int rounds() { return rounds; }
    public void rounds(int newRounds) { rounds = newRounds; }

    public long seed() { return keySeed; }
    public void seed(long seed) {
        keySeed = seed;
        generateKeyFromSeed();
    }

    @Override
    public String schemeName() { return "NSAES"; }

    @Override
    public String encrypt(String raw) {
        ByteBuffer rawBuffer = ByteBuffer.allocate(raw.length() + blockArea - raw.length() % blockArea); // round up by block area
        rawBuffer.put(raw.getBytes(charset));
        rawBuffer.position(0);

        ByteBuffer codedBuffer = ByteBuffer.allocate(rawBuffer.capacity());
        byte[] arr = new byte[blockArea];
        while (rawBuffer.hasRemaining()) {
            rawBuffer.get(arr);
            encryptBlock(arr);
            codedBuffer.put(arr);
        }
        return jb64.encode(codedBuffer.array());
    }

    private void encryptBlock(byte[] blk) {
        if (key.length != rounds * blockArea) throw new IllegalStateException("Invalid Key!");
        for (int r = 0; r< rounds; r++) {
            int start = r* blockArea;
            byte[] subkey = Arrays.copyOfRange(key, start, start + blockArea);
            subBytes(blk, subkey[0]);
            shiftRows(blk, true);
//			mixColumns(blk); TODO: Understand this
            addKey(blk, subkey);
        }
    }

    @Override
    public String decrypt(String coded) {
        ByteBuffer cipherBuffer;
//        try {
            cipherBuffer = ByteBuffer.wrap(jb64.decode(coded));
//        } catch (IllegalArgumentException e) {
//            return e.getMessage();
//        }
//        if (cipherBuffer.capacity() % BLOCK_AREA!=0) // Will throw if an incomplete block contained
//            throw new IllegalArgumentException("Incomplete cipherBuffer length=" + cipherBuffer.capacity());

        ByteBuffer decodedBuffer = ByteBuffer.allocate(cipherBuffer.capacity());
        byte[] arr = new byte[blockArea];
        while (cipherBuffer.hasRemaining()) {
            try {
                cipherBuffer.get(arr);
            } catch (BufferUnderflowException e) {
                break;
            }
            decryptBlock(arr);
            decodedBuffer.put(arr);
        }
        return new String(decodedBuffer.array(), charset).trim(); // Without trimming, repeatedly flipping direction would append whitespace each time.
    }

    private void decryptBlock(byte[] blk) {
        if (key.length!= rounds * blockArea) throw new IllegalStateException("Invalid Key!");
        for (int r = rounds -1; r>=0; r--) {
            int start = r* blockArea;
            byte[] subkey = Arrays.copyOfRange(key, start, start + blockArea);
            addKey(blk, subkey);
            shiftRows(blk, false);
            subBytes(blk, (byte) -subkey[0]);
//			mixColumns(blk); TODO: Understand this
        }
    }

    private void subBytes(byte[] blk, byte shift) { // TODO: Implement real affine transforms and galois field
        if (blk.length != blockArea) throw new IllegalArgumentException("Array length is " + blk.length);
        for (int i=0;i<blk.length;i++) {
            blk[i]+=shift;
        }
    }

    private void shiftRows(byte[] blk, boolean direction) { // Offset each row by its y coordinate within square block
        if (blk.length != blockArea) throw new IllegalArgumentException("Array length is " + blk.length);
        for (int y = 1; y< blockSize; y++) {
            int fi = y* blockSize;
            byte[] row = Arrays.copyOfRange(blk, fi, fi+ blockSize);
            for (int x = 0; x< blockSize; x++) blk[fi+x] = row[(x + (direction?y: blockSize -y)) % blockSize];
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
        for (int i = 0; i< blockArea; i++) arr[i] ^= rndkey[i];
    }

}
