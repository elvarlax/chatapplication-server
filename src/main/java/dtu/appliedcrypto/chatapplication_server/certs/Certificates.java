package dtu.appliedcrypto.chatapplication_server.certs;

import java.security.KeyStore;
import java.security.PublicKey;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.security.cert.Certificate;

public class Certificates {
    KeyStore ks;
    FileInputStream ksfis;
    BufferedInputStream ksbufin;
    final String ownAlias="Bob";
    final String keyStore = "KeyStore.jks";
    final String keyStorePass = "123456";
    public Certificates() throws Exception{
        ks = KeyStore.getInstance( "JKS" );
        ksfis = new java.io.FileInputStream( keyStore );
        ksbufin = new java.io.BufferedInputStream( ksfis );
        ks.load( ksbufin, keyStorePass.toCharArray() );
    }
    public boolean verifyCert(){
        //TO-DO
        return false;
    }
    public PublicKey extractPublicKey(String alias) throws Exception{
        Certificate cert = ks.getCertificate( alias );
        PublicKey pubKey = cert.getPublicKey();
        return pubKey;
    }
    public Certificate getCert(String alias) throws Exception{
        Certificate cert = ks.getCertificate(alias);
        return cert;
    }
}