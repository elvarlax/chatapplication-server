package dtu.appliedcrypto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import dtu.appliedcrypto.chatapplication_server.crypto.PublicKeyCrypto;
import org.junit.Test;

import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;

import dtu.appliedcrypto.chatapplication_server.certs.Certificates;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Verify certificate successfully
     */
    @Test
    public void verifyCertificateSuccessfully() throws Exception {
        Certificates certificates = new Certificates("keystores/Alice.jks", "boapNCkkbW");
        Certificate alice = certificates.getCert();
        Certificate ca = certificates.getCert(Certificates.ALIAS_CA);
        certificates.verify(ca, alice);
    }

    /**
     * Verify certificate unsuccessfully
     */
    @Test(expected = SignatureException.class)
    public void verifyCertificateUnsuccessfully() throws Exception {
        Certificates certificates = new Certificates("keystores/Alice.jks", "boapNCkkbW");
        Certificate agent = certificates.getCert(new FileInputStream("certificates/alice_selfsigned.cer"));
        Certificate ca = certificates.getCert(Certificates.ALIAS_CA);
        certificates.verify(ca, agent);
    }

    @Test
    public void encryptAndDecryptKey() throws Exception {
        PublicKeyCrypto pkc = new PublicKeyCrypto();
        byte[] key = pkc.generateSharedKey();
        Certificates certificates = new Certificates("keystores/Alice.jks", "boapNCkkbW");
        Certificate alice = certificates.getCert();
        byte[] encrKey = pkc.encryptText(key, alice.getPublicKey());
        byte[] decrKey = pkc.decryptText(encrKey, certificates.getPrivateKey());
        assertArrayEquals(decrKey, key);
    }

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

    @Test(expected = SignatureException.class)
    public void shouldInValidateSelfSignedCert() throws Exception {
        // arrange
        String keyStore = "keystores/Test.jks";
        String keyPass = "123456";
        String selfSignedCert = "certificates/alice_selfsigned.cer";

        // act
        Certificates certs = new Certificates(keyStore, keyPass);
        Certificate selfSigned = certs.getCert(new FileInputStream(selfSignedCert));

        // assert
        assertNotNull(selfSigned);
        certs.verify(selfSigned);
    }
}
