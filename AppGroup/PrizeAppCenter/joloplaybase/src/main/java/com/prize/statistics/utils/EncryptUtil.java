package com.prize.statistics.utils;

import com.prize.app.util.JLog;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * longbaoxiu
 * 2016/12/20.14:16
 *
 */

public class EncryptUtil {
    private final static String ENCRYPTKEY = "021ad6ed";
    private static byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final String XOR_KEY = "ddsad5464kior4423dsa";

    /**
     * data异或
     * @param bytes byte[]
     * @return byte[]
     */
    public static byte[] encrypt(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        int keyLen = XOR_KEY.length();

        for (int i = 0; i < len; i++) {
            bytes[i] ^= XOR_KEY.charAt(i%keyLen);
        }
        return bytes;
    }
    public static String encryptDES(String encryptString) {
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(ENCRYPTKEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            byte[] encryptedData = new byte[0];
            encryptedData = cipher.doFinal(encryptString.getBytes());
            return Base64.encode(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            JLog.i("PRIZE", "encryptDES-e=" + e.getMessage());
        }
        return "";
    }

    public static String decryptDES(String decryptString) {
        try {
            byte[] byteMi = new Base64().decode(decryptString);
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(ENCRYPTKEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte decryptedData[] = cipher.doFinal(byteMi);
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
