package dtu.appliedcrypto.chatapplication_server.certs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class Certificates {
    KeyStore ks;
    FileInputStream ksfis;
    BufferedInputStream ksbufin;
    final String keyStore = "KeyStore.jks";
    final String keyStorePass = "123456";

    public Certificates() throws Exception {
        ks = KeyStore.getInstance("JKS");
        ksfis = new java.io.FileInputStream(keyStore);
        ksbufin = new java.io.BufferedInputStream(ksfis);
        ks.load(ksbufin, keyStorePass.toCharArray());
    }

    public boolean verifyCert(X509Certificate target, X509Certificate CA) throws Exception {
        boolean verified;
        try {
            target.verify(CA.getPublicKey());
            verified = true;
        } catch (SignatureException e) {
            verified = false;
        }
        return verified;
    }

    public PublicKey getPublicKey(String alias) throws Exception {
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        PublicKey pubKey = cert.getPublicKey();
        return pubKey;
    }

    public Certificate getCert(String alias) throws Exception {
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        return cert;
    }

    public PrivateKey getPrivateKey(String alias, String password) throws Exception {
        PrivateKey privKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        return privKey;
    }
}