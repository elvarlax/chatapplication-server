package dtu.appliedcrypto.chatapplication_server.certs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Certificates {
    private static final String KeyStoreType = "JKS";
    final String keyStore;
    final String keyStorePass;
    private final KeyStore ks;

    /**
     * Constructor for the Certificate class.
     * Loads in the keystore with the provided password
     * @param keyStore - Path to the keystore
     * @param keyStorePass - Password of the keystore
     * @throws Exception
     */
    public Certificates(String keyStore, String keyStorePass) throws Exception {
        this.keyStore = keyStore;
        this.keyStorePass = keyStorePass;

        ks = KeyStore.getInstance(KeyStoreType);
        FileInputStream ksfis = new FileInputStream(keyStore);
        BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
        ks.load(ksbufin, keyStorePass.toCharArray());
    }

    /**
     * Verifies the target certificate against the CA certificate
     * @param ca 
     * @param target 
     * @throws Exception
     */
    public void verify(Certificate ca, Certificate target) throws Exception {
        target.verify(ca.getPublicKey());
    }

    /**
     * Retrieves the private key with the given password
     * @param alias
     * @param password
     * @return
     * @throws Exception
     */
    public PrivateKey getPrivateKey(String alias, String password) throws Exception {
        PrivateKey privKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        return privKey;
    }

    /**
     * Retrieves certificate based on the alias
     * @param alias
     * @return
     * @throws Exception
     */
    public Certificate getCert(String alias) throws Exception {
        Certificate cert = ks.getCertificate(alias);
        return cert;
    }

    /**
     * Retrieves certificate based on the FileInputStream
     * @param file
     * @return
     * @throws Exception
     */
    public Certificate getCert(FileInputStream file) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate cert = factory.generateCertificate(file);
        return cert;
    }

    /**
     * Adds a certificate to the keystore
     * @param alias
     * @param cert
     * @throws Exception
     */
    public void addCert(String alias, Certificate cert) throws Exception {
        ks.setCertificateEntry(alias, cert);
    }
}