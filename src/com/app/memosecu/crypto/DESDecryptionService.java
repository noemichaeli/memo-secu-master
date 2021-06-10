package com.app.memosecu.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;


public class DESDecryptionService {

    public static final String PBEWithMD5AndDES = "PBEWithMD5AndDES";

    
    /**
     * This method initialises a local decryption cipher, and decrypts the given string.
     * It's here as a convenience method for backwards compatibility with the old DES 
     * encryption algorithm pre 1.3
     * @param password
     * @param salt
     * @param ciphertext
     * @return The decrypted bytes
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeyException 
     * @throws InvalidAlgorithmParameterException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidPasswordException 
     */
    public static byte[] decrypt(char[] password, byte[] salt, byte[] ciphertext) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidPasswordException {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBEWithMD5AndDES);
        SecretKey secretKey = keyFac.generateSecret(pbeKeySpec);

        return decrypt(secretKey, salt, ciphertext);
    }

    
    public static byte[] decrypt(SecretKey secretKey, byte[] salt, byte[] ciphertext) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidPasswordException {
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
        Cipher desDecryptionCipher = Cipher.getInstance(PBEWithMD5AndDES);
        desDecryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParamSpec);

        // Do the decryption
        byte[] retVal;
        try {
            retVal = desDecryptionCipher.doFinal(ciphertext);
        } catch (BadPaddingException e) {
            throw new InvalidPasswordException(); 
        }
        return retVal;
    }

}
