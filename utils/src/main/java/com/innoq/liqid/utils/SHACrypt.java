package com.innoq.liqid.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHACrypt
 * 04.12.2011
 * @author Philipp Haussleiter
 *
 */
public final class SHACrypt {

    public static String encrypt(final String plaintext) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        byte raw[] = md.digest();
        char out[] = Base64Coder.encode(raw);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<out.length; i++){
            sb.append(out[i]);
        }
        return sb.toString();
    }
}
