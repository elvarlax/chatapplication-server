package chatapplication_server.crypto;

import java.nio.charset.StandardCharsets;

import java.security.GeneralSecurityException;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class StreamCipher {
    private SecretKey secret;
    public final static String CIPHER_TYPE = "AES/CBC/PKCS7Padding";

    /**
     * @param keyBytes some initial shared secret ("password") 
     */
    public StreamCipher(byte[] keyBytes) {
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("keyBytes wrong length for AES key"); 
        }
        this.secret = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * AES encrypt primitive (CBC mode with PKCS7 padding) 
     * @param key some secret
     * @param data data to be encrypted
     * @return input vector and encrypted data as byte arrays
     * @throws GeneralSecurityException
     */
    private static byte[][] aesCbcEncrypt(SecretKey key, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME); 
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new byte[][] { cipher.getIV(), cipher.doFinal(data) };
    }

    /**
     * AES decrypt primitive (CBC mode with PKCS7 padding)
     * @param key some secret
     * @param iv input vector
     * @param cipherText encrypted data
     * @return plainText data as byte array
     * @throws GeneralSecurityException
     */
    private static byte[] aesCbcDecrypt(SecretKey key, byte[] iv, byte[] cipherText) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    /**
     * Stream Cipher encryption
     * @param message a message to be encrypted
     * @return input vector and encrypted message as byte arrays
     * @throws GeneralSecurityException
     */
    public byte[][] encrypt(String message) throws GeneralSecurityException {
        return aesCbcEncrypt(this.secret, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Stream Cipher decryption
     * @param iv input vector
     * @param cipherText encrypted data
     * @return plain text
     * @throws GeneralSecurityException
     */
    public String decrypt(byte[] iv, byte[] cipherText) throws GeneralSecurityException {
        byte[] textBytes = aesCbcDecrypt(this.secret, iv, cipherText);
        return new String(textBytes);
    }
}
