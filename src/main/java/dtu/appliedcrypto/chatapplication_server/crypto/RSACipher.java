package dtu.appliedcrypto.chatapplication_server.crypto;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class RSACipher {
  public final static String CIPHER_TYPE = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

  public static byte[] encrypt(PublicKey key, byte[] plaintext) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(plaintext);
  }

  public static byte[] decrypt(PrivateKey key, byte[] ciphertext) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(ciphertext);
  }
}
