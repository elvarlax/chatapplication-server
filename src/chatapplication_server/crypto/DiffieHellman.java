package chatapplication_server.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.util.BigIntegers;

/**
 * Contains building blocks needed for Diffie-Hellman Key Exchange.
 */
public class DiffieHellman {
  
    private static final SecureRandom RND = new SecureRandom();
    private static final int SECRET_BIT_COUNT = 2048;
    /**
    * The prime for standardized 2048-bit MODP Group. 
    * (<a href="https://www.ietf.org/rfc/rfc3526.txt">RFC 3526</a>)<p>
    *
    * Belongs to a group with id 14.
    */
    private static final String MODP_2048_PRIME_HEX = (
        "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
        "29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
        "EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
        "E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
        "EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
        "C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
        "83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
        "670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B" +
        "E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9" +
        "DE2BCBF6 95581718 3995497C EA956AE5 15D22618 98FA0510" +
        "15728E5A 8AACAA68 FFFFFFFF FFFFFFFF")
        .replaceAll("\\s", "");

    public static final BigInteger P = new BigInteger(MODP_2048_PRIME_HEX, 16);
    public static final BigInteger G = BigInteger.valueOf(2L);

    /**
     * Generates a random number between 0 and 2^{@value #SECRET_BIT_COUNT}.<p>
     */
    public static BigInteger generateRandomSecret()
    {
        return BigIntegers.createRandomBigInteger(SECRET_BIT_COUNT, RND);
    }

    /**
     * Computes a common secret key K such that K = | partiakKey^(secret) | mod p
     * @param partialKey a value received from another entity
     * @param secret own secret key
     * @return common secret key
     */
    public static BigInteger getCommonKey(BigInteger partialKey, BigInteger secret)
    {
        return partialKey.modPow(secret, P);
    }

    /**
     * Computs a partial key K such that K = | g^(secret) | mod p
     * @param secret own secret key
     * @return a partial key
     */
    public static BigInteger getPartialKey(BigInteger secret)
    {
        return G.modPow(secret, P);
    }
}
