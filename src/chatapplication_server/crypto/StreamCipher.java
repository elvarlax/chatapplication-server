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

    public StreamCipher(byte[] keyBytes) {
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("keyBytes wrong length for AES key"); 
        }
        this.secret = new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[][] aesCbcEncrypt(SecretKey key, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME); 
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new byte[][] { cipher.getIV(), cipher.doFinal(data) };
    }

    private static byte[] aesCbcDecrypt(SecretKey key, byte[] iv, byte[] cipherText) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    public byte[][] encrypt(String message) throws GeneralSecurityException {
        return aesCbcEncrypt(this.secret, message.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(byte[] iv, byte[] cipherText) throws GeneralSecurityException {
        byte[] textBytes = aesCbcDecrypt(this.secret, iv, cipherText);
        return new String(textBytes);
    }
}
