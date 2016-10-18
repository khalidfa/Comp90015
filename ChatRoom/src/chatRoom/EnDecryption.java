package chatRoom;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
//import org.apache.commons.codec.binary.Base64;

//import Decoder.BASE64Decoder;
//import Decoder.BASE64Encoder;

import java.util.Base64;

public class EnDecryption 
{
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";
    
    public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(EnDecryption.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("UTF-8"));
        System.out.println("encryptee byte: " + encryptedByteValue);
        String encryptedValue64 = Base64.getEncoder().encodeToString(encryptedByteValue);
        return encryptedValue64;
               
    }
    
    public static String decrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(EnDecryption.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        System.out.println("this is the value " + value);
        byte [] decryptedValue64 = Base64.getDecoder().decode(value);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        String decryptedValue = new String(decryptedByteValue,"UTF-8");
        System.out.println(decryptedValue);
        return decryptedValue;
                
    }
    
    private static Key generateKey() throws Exception 
    {
        Key key = new SecretKeySpec(EnDecryption.KEY.getBytes(),EnDecryption.ALGORITHM);
        return key;
    }
}