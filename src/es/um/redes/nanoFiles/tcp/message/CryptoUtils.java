package es.um.redes.nanoFiles.tcp.message;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Willy
 * @author Jorge
 * 
 * Utility class with static methods to provide AES encryption and decryption 
 * functionality for securing data. Abstracts the handling of cryptographic operations 
 * using a predefined secret key and the Java Cryptography Architecture (JCA).
 */

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "gfparralopez@um.";

    public static byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
}
