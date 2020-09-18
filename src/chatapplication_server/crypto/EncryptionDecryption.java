package chatapplication_server.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;


public class EncryptionDecryption {
    private static String sharedKey = "";
    public static void setSharedKey(BigInteger key){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.byteValue());
            byte[] digest = md.digest();
            String shaKey = String.format("%064x", new BigInteger(1, digest));
        }
        catch(Exception e){
            System.out.println(e.toString());
        }



    }
    public static String Encrypt(String message){
        return "";
    }
    public static String Decrypt(String message){
        return "";
    }
}