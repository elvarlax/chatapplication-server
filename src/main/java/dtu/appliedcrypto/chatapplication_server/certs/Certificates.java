package dtu.appliedcrypto.chatapplication_server.certs;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.KeyStore.Entry;
import java.security.PrivateKey;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.security.cert.Certificate;

public class Certificates {
    private static final String KeyStoreType = "JKS";
    private final KeyStore ks;

    final String keyStore;
    final String keyStorePass;

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

    public PublicKey getPublicKey(String alias) throws Exception {
        PublicKey pubKey = this.getCert(alias).getPublicKey();
        return pubKey;
    }

    public Certificate getCert(String alias) throws Exception {
        Certificate cert = ks.getCertificate(alias);
        return cert;
    }

    public void addCert(String alias, Certificate cert) throws Exception {
        Entry entry = (Entry) cert;
        ks.setEntry(alias, entry, null);
    }

    public PrivateKey getPrivateKey(String alias, String password) throws Exception {
        PrivateKey privKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        return privKey;
    }
}