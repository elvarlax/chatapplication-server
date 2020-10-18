package dtu.appliedcrypto.chatapplication_server.certs;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Certificates {

  private static final String KEY_STORE_TYPE = "JKS";
  private static final String CA_TOKEN = "CA";
  private static final String CERT_TOKEN = "CERT";
  private static final String SECRET_KEY_TOKEN = "SECRET";

  private final KeyStore store;
  final String keyStorePass;

  public Certificates(String keyStoreFile, String keyStorePass) throws Exception {
    this.store = KeyStore.getInstance(KEY_STORE_TYPE);
    this.keyStorePass = keyStorePass;

    File file = new File(keyStoreFile);
    if (file.exists()) {
      store.load(new BufferedInputStream(new FileInputStream(file)), this.keyStorePass.toCharArray());
    } else {
      store.load(null, null);
      store.store(new FileOutputStream(file), this.keyStorePass.toCharArray());
    }

    if (getCert(CA_TOKEN) == null || getCert(CERT_TOKEN) == null) {
      throw new Exception("KeyStore shoul already have CA and CERT stored");
    }
  }

  public Certificates(String keyStoreFile, String keyStorePass, String caFile, String certFile) throws Exception {
    this.store = KeyStore.getInstance(KEY_STORE_TYPE);
    this.keyStorePass = keyStorePass;

    File file = new File(keyStoreFile);
    if (file.exists()) {
      store.load(new BufferedInputStream(new FileInputStream(file)), this.keyStorePass.toCharArray());
    } else {
      store.load(null, null);
      store.store(new FileOutputStream(file), this.keyStorePass.toCharArray());
    }

    // store CA certificate
    addCert(CA_TOKEN, getCert(new FileInputStream(caFile)));

    // store personal certificate
    addCert(CERT_TOKEN, getCert(new FileInputStream(certFile)));
  }

  public void addCert(String alias, Certificate cert) throws Exception {
    store.setCertificateEntry(alias, cert);
  }

  public Certificate getCert(String alias) throws Exception {
    Certificate cert = store.getCertificate(alias);
    return cert;
  }

  public Certificate getCert(FileInputStream file) throws Exception {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    Certificate cert = factory.generateCertificate(file);
    return cert;
  }

  public PublicKey getPublicKey(String alias) throws Exception {
    PublicKey pubKey = this.getCert(alias).getPublicKey();
    return pubKey;
  }

  public PrivateKey getPrivateKey(String password) throws Exception {
    PrivateKey privKey = (PrivateKey) store.getKey(SECRET_KEY_TOKEN, password.toCharArray());
    return privKey;
  }

  public void verify(Certificate target) throws Exception {
    Certificate ca = getCert("CA");
    verify(ca, target);
  }

  public void verify(Certificate ca, Certificate target) throws Exception {
    target.verify(ca.getPublicKey());
  }
}