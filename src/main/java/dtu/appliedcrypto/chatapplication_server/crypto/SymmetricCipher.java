package dtu.appliedcrypto.chatapplication_server.crypto;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SymmetricCipher {
    public final static int IV_LENGTH = 16;
    public final static String CIPHER_TYPE = "AES/CBC/PKCS7Padding";
    private final SecretKey secret;

    /**
     * @param keyBytes some initial shared secret ("password")
     */
    public SymmetricCipher(byte[] keyBytes) {
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("keyBytes wrong length for AES key");
        }
        this.secret = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * @param key
     * @throws NoSuchAlgorithmException
     */
    public SymmetricCipher(String key) throws NoSuchAlgorithmException {
        this(MessageDigest.getInstance("SHA256").digest(key.getBytes(StandardCharsets.UTF_8)));
    }

    public SymmetricCipher(BigInteger key) throws NoSuchAlgorithmException {
        this(MessageDigest.getInstance("SHA256").digest(key.toByteArray()));
    }

    /**
     * AES encrypt primitive (CBC mode with PKCS7 padding)
     *
     * @param key  some secret
     * @param data data to be encrypted
     * @return input vector and encrypted data as byte arrays
     * @throws GeneralSecurityException
     */
    private static byte[][] aesCbcEncrypt(SecretKey key, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new byte[][]{cipher.getIV(), cipher.doFinal(data)};
    }

    /**
     * AES decrypt primitive (CBC mode with PKCS7 padding)
     *
     * @param key        some secret
     * @param iv         input vector
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
     *
     * @param message a message to be encrypted
     * @return input vector and encrypted message as byte array
     * @throws GeneralSecurityException
     */
    public byte[] encrypt(String message) throws GeneralSecurityException {
        byte[][] result = aesCbcEncrypt(this.secret, message.getBytes(StandardCharsets.UTF_8));
        return ArrayUtils.addAll(result[0], result[1]);
    }

    /**
     * Stream cipher decryption
     *
     * @param cipherText encrypted data
     * @return plain text
     * @throws GeneralSecurityException
     */
    public String decrypt(byte[] cipherText) throws GeneralSecurityException {
        byte[] charBytes = aesCbcDecrypt(this.secret, Arrays.copyOfRange(cipherText, 0, IV_LENGTH),
                Arrays.copyOfRange(cipherText, IV_LENGTH, cipherText.length));
        return new String(charBytes);
    }
}
