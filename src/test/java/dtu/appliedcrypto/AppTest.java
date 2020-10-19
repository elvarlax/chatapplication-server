package dtu.appliedcrypto;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;
import dtu.appliedcrypto.chatapplication_server.crypto.PublicKeyCrypto;
import org.junit.Test;

import java.io.FileInputStream;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Verify certificate successfully
     */
    @Test
    public void verifyCertificateSuccessfully() throws Exception {
        Certificates certificates = new Certificates("certificates/AliceKeyStore.jks", "123456");
        Certificate alice = certificates.getCert("Alice");
        Certificate ca = certificates.getCert("TestCA");
        certificates.verify(ca, alice);
    }

    /**
     * Verify certificate unsuccessfully
     */
    @Test(expected = SignatureException.class)
    public void verifyCertificateUnsuccessfully() throws Exception {
        Certificates certificates = new Certificates("certificates/AliceKeyStore.jks", "123456");
        Certificate agent = certificates.getCert(new FileInputStream("certificates/Agent.cer"));
        Certificate ca = certificates.getCert("TestCA");
        certificates.verify(ca, agent);
    }

    @Test
    public void encryptAndDecryptKey() throws Exception {
        PublicKeyCrypto pkc = new PublicKeyCrypto();
        byte[] key = pkc.generateSharedKey();
        Certificates certificates = new Certificates("certificates/AliceKeyStore.jks", "123456");
        Certificate alice = certificates.getCert("Alice");
        byte[] encrKey = pkc.encryptText(key, alice.getPublicKey());
        byte[] decrKey = pkc.decryptText(encrKey, certificates.getPrivateKey("alice", "123456"));
        assertTrue(Arrays.equals(decrKey, key));
    }
}
