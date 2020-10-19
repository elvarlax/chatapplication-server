package dtu.appliedcrypto;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;
import dtu.appliedcrypto.chatapplication_server.crypto.DiffieHellman;
import dtu.appliedcrypto.chatapplication_server.crypto.PublicKeyCrypto;

import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.security.SignatureException;
import java.security.cert.Certificate;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Verify certificate
     */
    @Test
    public void verifyCertificate() throws Exception {
        String keyStore = "certificates/AliceKeyStore.jks";
        String keyPass = "123456";
        Certificates certificates = new Certificates(keyStore, keyPass);
        Certificate alice = certificates.getCert("Alice");
        Certificate ca = certificates.getCert("TestCA");
        certificates.verify(ca, alice);
    }

    /**
     * Verify certificate with malicious agent
     */
    @Test(expected = SignatureException.class)
    public void verifyWithMaliciousAgent() throws Exception {
        String keyStore = "certificates/AliceKeyStore.jks";
        String keyPass = "123456";
        Certificates certificates = new Certificates(keyStore, keyPass);
        Certificate alice = certificates.getCert("Alice");
        Certificate agent = certificates.getCert(new FileInputStream("certificates/Agent.cer"));
        certificates.verify(agent, alice);
    }

    @Test
    public void encryptAndDecryptKey() throws Exception {
        PublicKeyCrypto pkc = new PublicKeyCrypto();
        byte[] key = pkc.generateSharedKey();
        
        String keyStore = "certificates/AliceKeyStore.jks";
        String keyPass = "123456";
        Certificates certificates = new Certificates(keyStore, keyPass);
        Certificate alice = certificates.getCert("Alice");
        byte[] encrKey = pkc.encryptText(key, alice.getPublicKey());
        byte[] decrKey = pkc.decryptText(encrKey, certificates.getPrivateKey("alice", "123456"));
        assertEquals(decrKey, key);
    }
}
