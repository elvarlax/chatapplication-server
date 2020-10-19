package dtu.appliedcrypto.chatapplication_server.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class simulates pre-established symmetric keys
 */
public class SymmetricCipherUtility {
  private final static Map<String, SymmetricCipher> cipherMap = new HashMap<String, SymmetricCipher>();

  /**
   * Gets or creates a stream cipher for Client-Server communication channel.
   * 
   * @param id some uniqe identifier of the channel (e.g. userName)
   * @return a stream cipher for given channel
   * @throws GeneralSecurityException
   */
  public static SymmetricCipher getCipher(String id) throws GeneralSecurityException {
    if (!cipherMap.containsKey(id)) {
      throw new GeneralSecurityException("Cipher was not initialized");
    }
    return cipherMap.get(id);
  }
  public static SymmetricCipher getCipher(String id, byte[] key) throws GeneralSecurityException {
    if (!cipherMap.containsKey(id)) {
      cipherMap.put(id, new SymmetricCipher(key));
    }
    return cipherMap.get(id);
  }
}
