package dtu.appliedcrypto;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;
import org.junit.Test;

import java.security.cert.Certificate;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
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
}
