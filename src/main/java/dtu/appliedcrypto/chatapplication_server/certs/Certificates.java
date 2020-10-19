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

public class Certificates {

  private static final String KEY_STORE_TYPE = "JKS";
  public static final String ALIAS_CA = "CA";
  public static final String ALIAS_PRIVATE_CERT = "PRIVATE";

  private final KeyStore store;
  final String keyStorePass;
  final File keyStoreFile;

  public Certificates(String keyStoreFile, String keyStorePass) throws Exception {
    this.store = KeyStore.getInstance(KEY_STORE_TYPE);
    this.keyStoreFile = new File(keyStoreFile);
    this.keyStorePass = keyStorePass;

    if (this.keyStoreFile.exists()) {
      store.load(new BufferedInputStream(new FileInputStream(this.keyStoreFile)), this.keyStorePass.toCharArray());
    } else {
      store.load(null, null);
      store.store(new FileOutputStream(this.keyStoreFile), this.keyStorePass.toCharArray());
    }

    if (getCert(ALIAS_CA) == null || getCert(ALIAS_PRIVATE_CERT) == null) {
      throw new Exception("KeyStore should already have CA and CERT stored");
    }
  }

  public Certificates(String keyStoreFile, String keyStorePass, String caFile, String certFile) throws Exception {
    this.store = KeyStore.getInstance(KEY_STORE_TYPE);
    this.keyStoreFile = new File(keyStoreFile);
    this.keyStorePass = keyStorePass;

    if (this.keyStoreFile.exists()) {
      store.load(new BufferedInputStream(new FileInputStream(this.keyStoreFile)), this.keyStorePass.toCharArray());
    } else {
      store.load(null, null);
    }

    // store CA certificate
    addCert(ALIAS_CA, getCert(new FileInputStream(caFile)));

    // store personal certificate
    addCert(ALIAS_PRIVATE_CERT, getCert(new FileInputStream(certFile)));
  }

  public void addCert(String alias, Certificate cert) throws Exception {
    store.setCertificateEntry(alias, cert);

    // store changes
    FileOutputStream fos = new FileOutputStream(this.keyStoreFile);
    store.store(fos, this.keyStorePass.toCharArray());
    fos.close();
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

  public PrivateKey getPrivateKey() throws Exception {
    PrivateKey privKey = (PrivateKey) store.getKey(ALIAS_PRIVATE_CERT, this.keyStorePass.toCharArray());
    return privKey;
  }

  public void verify(Certificate target) throws Exception {
    Certificate ca = getCert(ALIAS_CA);
    verify(ca, target);
  }

  public void verify(Certificate ca, Certificate target) throws Exception {
    target.verify(ca.getPublicKey());
  }
}