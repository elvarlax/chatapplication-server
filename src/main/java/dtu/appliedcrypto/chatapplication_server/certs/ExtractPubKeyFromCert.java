/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.chatapplication_server.certs;

import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ExtractPubKeyFromCert {
  public static void main(String args[]) throws Exception {
    FileInputStream fr = new FileInputStream("Bob.cer");
    CertificateFactory cf = CertificateFactory.getInstance("X509");
    X509Certificate c = (X509Certificate) cf.generateCertificate(fr);
    System.out.println("\tCertificate for: " + c.getSubjectDN());
    System.out.println("\tCertificate issued by: " + c.getIssuerDN());
    System.out.println("\tThe certificate is valid from " + c.getNotBefore() + " to "
        + c.getNotAfter());
    System.out.println("\tCertificate SN# " + c.getSerialNumber());
    System.out.println("\tGenerated with " + c.getSigAlgName());
    
    
    //X509Certificate cert = loadCertificate(df);
    System.out.println(c.getSigAlgName());//SHA1withRSA
    PublicKey key=c.getPublicKey();
       System.out.println("\tkey " + key.toString());
    System.out.println(key.getAlgorithm());//java.lang.NullPointerException
    }
    
}