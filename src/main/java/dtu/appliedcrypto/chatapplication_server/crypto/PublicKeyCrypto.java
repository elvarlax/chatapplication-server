package dtu.appliedcrypto.chatapplication_server.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;


public class PublicKeyCrypto {
    private Cipher cipher;

	public PublicKeyCrypto() throws Exception {
		this.cipher = Cipher.getInstance("RSA");
	}

    public String encryptText(String msg, PublicKey key) throws Exception {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return new String(Base64.getEncoder().encode(cipher.doFinal(msg.getBytes("UTF-8"))));
    }

    public String decryptText(String msg, PrivateKey key) throws Exception {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(msg.getBytes())), "UTF-8");
    }
}