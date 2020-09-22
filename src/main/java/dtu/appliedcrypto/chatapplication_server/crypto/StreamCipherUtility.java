package dtu.appliedcrypto.chatapplication_server.crypto;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class simulates pre-established symmetric keys
 */
public class StreamCipherUtility {
  private final static Map<String, StreamCipher> cipherMap = new HashMap<String, StreamCipher>();

  /**
   * Gets or creates a stream cipher for Client-Server communication channel.
   * 
   * @param id some uniqe identifier of the channel (e.g. userName)
   * @return a stream cipher for given channel
   * @throws GeneralSecurityException
   */
  public static StreamCipher getCipher(String id) throws GeneralSecurityException {
    if (!cipherMap.containsKey(id)) {
      cipherMap.put(id, new StreamCipher(id));
    }
    return cipherMap.get(id);
  }
}
