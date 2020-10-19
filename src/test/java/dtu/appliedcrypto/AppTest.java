package dtu.appliedcrypto;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void shouldLoadTestCerts() throws Exception {
        // arrange
        String keyStore = "keystores/Test.jks";
        String keyPass = "123456";

        // act
        Certificates certs = new Certificates(keyStore, keyPass);
        Certificate ca = certs.getCert(Certificates.ALIAS_CA);
        Certificate privateCert = certs.getCert(Certificates.ALIAS_PRIVATE_CERT);
        PrivateKey privateKey = certs.getPrivateKey();

        // assert
        assertNotNull(ca);
        assertNotNull(privateCert);
        assertNotNull(privateKey);
    }

    @Test
    public void shouldValidateSignedCert() throws Exception {
        // arrange
        String keyStore = "keystores/Test.jks";
        String keyPass = "123456";

        // act
        Certificates certs = new Certificates(keyStore, keyPass);
        Certificate ca = certs.getCert(Certificates.ALIAS_CA);
        Certificate privateCert = certs.getCert(Certificates.ALIAS_PRIVATE_CERT);

        // assert
        certs.verify(ca, privateCert);
        certs.verify(privateCert);
    }
}
