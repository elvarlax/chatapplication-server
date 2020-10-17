/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.chatapplication_server.certs;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;

public class AccessCertificates {

  public static void main( String[] args ) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    final String keyStore = "BobKeyStore.jks"; // keystore file should exisit in the program folder of the application
    final String keyStorePass = "123456"; // password of keystore
    final String keyPass = "123456";

    // load information into a keystore
    java.security.KeyStore ks = java.security.KeyStore.getInstance( "JKS" );
    java.io.FileInputStream ksfis = new java.io.FileInputStream( keyStore );
    java.io.BufferedInputStream ksbufin = new java.io.BufferedInputStream( ksfis );
    ks.load( ksbufin, keyStorePass.toCharArray() );

    // list aliases in the keystore
    java.io.FileOutputStream fos = null;
    for( java.util.Enumeration theAliases = ks.aliases(); theAliases.hasMoreElements(); ) {
      String alias = (String) theAliases.nextElement();
      java.security.cert.Certificate cert = ks.getCertificate( alias );
      //ByteUtils.saveBytesToFile( alias + ".cer", cert.getEncoded() );
     // ByteUtils.saveBytesToFile( alias + ".pubkey", cert.getPublicKey().getEncoded() );
      java.security.PrivateKey privateKey = (java.security.PrivateKey) ks.getKey( alias, keyPass.toCharArray() );
     // ByteUtils.saveBytesToFile( alias + ".privKey", privateKey.getEncoded() );
      System.out.println( "### generated certificate information for -> " + alias );
      System.out.println( cert );
    }
  }
}
