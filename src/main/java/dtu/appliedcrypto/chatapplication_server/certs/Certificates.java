package dtu.appliedcrypto.chatapplication_server.certs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Certificates {
    private static final String KeyStoreType = "JKS";
    final String keyStore;
    final String keyStorePass;
    private final KeyStore ks;

    public Certificates(String keyStore, String keyStorePass) throws Exception {
        this.keyStore = keyStore;
        this.keyStorePass = keyStorePass;

        ks = KeyStore.getInstance(KeyStoreType);
        FileInputStream ksfis = new FileInputStream(keyStore);
        BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
        ks.load(ksbufin, keyStorePass.toCharArray());
    }

    public void verify(Certificate ca, Certificate target) throws Exception {
        target.verify(ca.getPublicKey());
    }

    public PrivateKey getPrivateKey(String alias, String password) throws Exception {
        PrivateKey privKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        return privKey;
    }

    public Certificate getCert(String alias) throws Exception {
        Certificate cert = ks.getCertificate(alias);
        return cert;
    }

    public Certificate getCert(FileInputStream file) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate cert = factory.generateCertificate(file);
        return cert;
    }

    public void addCert(String alias, Certificate cert) throws Exception {
        Entry entry = (Entry) cert;
        ks.setEntry(alias, entry, null);
    }
    public void addCertTest(String alias, Certificate cert) throws Exception {
        ks.setCertificateEntry(alias, cert);
    }
}