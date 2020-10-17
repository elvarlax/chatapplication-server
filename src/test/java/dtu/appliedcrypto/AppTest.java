package dtu.appliedcrypto;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import java.security.cert.Certificate;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void shouldLoadKeyStore() throws Exception {
        String keyStore = "certificates/Alice.jks";
        String keyPass = "123456";
        Certificates certs = new Certificates(keyStore, keyPass);
        assertNotNull(certs);
    }

    @Test
    public void shouldLoadAlicesCerts() throws Exception {
        String keyStore = "certificates/Alice.jks";
        String keyPass = "123456";
        Certificates certs = new Certificates(keyStore, keyPass);
        Certificate cert = certs.getCert("Alice");
        assertNotNull(cert);
    }
}
