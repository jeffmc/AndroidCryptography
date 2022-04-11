package com.example.androidcryptography;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JeffBase64 { // Taken from my Github https://github.com/jeffmc/AESimplementation/blob/main/src/net/mcmillan/cryptography/JeffBase64.java

    public static final String charStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    public static final char[] chars = charStr.toCharArray();

    public Charset charset = StandardCharsets.UTF_8;

    public String encode(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw); // round up by block area
        StringBuilder sb = new StringBuilder();
        byte[] seg; // octet segment
        byte s; // sextet segment
        byte mask = 0b111111;
        while (buf.hasRemaining()) {
            int rem = buf.remaining();
            seg = new byte[rem>3?3:rem];
            buf.get(seg);
            int t = ((seg[0] & 0xFF) >>> 2);
            sb.append(chars[t]);

            s = (byte) (seg[0] << 4);
            if (seg.length > 1) s |= (byte) ((seg[1]&0xFF) >>> 4);
            s &= mask;
            sb.append(chars[s]);

            if (seg.length < 2) break;

            s = (byte) (seg[1] << 2);
            if (seg.length > 2) s |= (byte) ((seg[2]&0xFF) >>> 6);
            s &= mask;
            sb.append(chars[s]);

            if (seg.length < 3) break;

            s = (byte) seg[2];
            s &= mask;
            sb.append(chars[s]);
        }
        return sb.toString();
    }

    public byte[] decode(String encoded) {
        CharBuffer buf = CharBuffer.wrap(encoded.trim().toCharArray());
        ByteBuffer dec = ByteBuffer.allocate((buf.capacity()/3+1)*4);
        char[] cseg; // char segment
        byte[] iseg; // char indexes of segment
        byte b; // building
        while (buf.hasRemaining()) {
            int rem = buf.remaining();
            cseg = new char[rem>4?4:rem];
            iseg = new byte[cseg.length];
            buf.get(cseg);
            for (int i=0;i<cseg.length;i++) {
                int idx = charStr.indexOf(cseg[i]);
                if (idx < 0) throw new IllegalArgumentException("Illegal Character(" + idx + "): " + "'" + cseg[i] + "'");
                iseg[i] = (byte) idx;
            }
            if (cseg.length < 2) break;

            b = (byte) (iseg[0] << 2);
            byte d = (byte) ((byte) (iseg[1] >>> 4) & 0b11);
            b |= d;
            dec.put(b);

            if (cseg.length < 3) break;

            b = (byte) (iseg[1] << 4);
            b |= (byte) (iseg[2] >>> 2) & 0b1111;
            dec.put(b);

            if (cseg.length < 4) break;

            b = (byte) (iseg[2] << 6);
            b |= (byte) iseg[3] & 0b111111;
            dec.put(b);
        }
        byte[] res = new byte[dec.position()];
        dec.position(0);
        dec.get(res);
        return res;
    }

    public static String binStr(byte in) {
        return String.format("%8s", Integer.toBinaryString(in & 0xFF)).replace(' ', '0');
    }
    public static String binStrInt(int in) {
        return String.format("%32s", Integer.toBinaryString(in)).replace(' ', '0');
    }


}
