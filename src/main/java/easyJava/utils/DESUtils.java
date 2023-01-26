package easyJava.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 */
public class DESUtils {
    public static String pwd =
            "9588028820109132570743325311898426347857298773549" +
                    "4687588750185795377577721630844788736994473060344662006164" +
                    "11960574122434059469100235892702736860872901247123456";


    /**
     * * @return byte[]
     */
    public static byte[] encrypt(byte[] datasource) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(pwd.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * * @param password String     * @return byte[]
     * * @throws Exception
     */
    public static byte[] decrypt(byte[] src) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(pwd.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        return cipher.doFinal(src);
    }

    /**
     * @param str
     * @param numSalt
     * @return
     */
    public static String encrypt(String str, Integer numSalt) {

        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            c[i] = (char) (c[i] ^ numSalt);
        }
        return new String(c);
    }

    /**
     * @param args
     */

}