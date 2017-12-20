package org.datavaultplatform.worker.operations;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.Test;

public class EcryptionTest {
    
    @Test
    public void testSimpleEncryptionWithAAD(){
        String messageToEncrypt = "This is a message to test the AES encryption." ;
        
        // Any random data can be used as tag. Some common examples could be domain name...
        byte[] aadData = "random".getBytes() ; 

        // Use different key+IV pair for encrypting/decrypting different parameters

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey();
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment "  + 
                    noSuchAlgoExc);
        }

        assertNotNull(aesKey);
        
        // Generating IV
        byte iv[] = Encryption.generateIV();
        
        assertNotNull(iv);
        
        byte[] encryptedText = Encryption.aesEncrypt(messageToEncrypt, aesKey, iv, aadData);
        
        assertNotNull(encryptedText);
        
        assertNotEquals(messageToEncrypt, Base64.getEncoder().encodeToString(encryptedText));
        
        // Same key, IV and GCM Specs for decryption as used for encryption.
        byte[] decryptedText = Encryption.aesDecrypt(encryptedText, aesKey, iv, aadData);

        assertEquals(messageToEncrypt, new String(decryptedText));
    }

    @Test
    public void testSimpleEncryptionWithoutAAD(){
        String messageToEncrypt = "This is a message to test the AES encryption." ;

        // Use different key+IV pair for encrypting/decrypting different parameters

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey();
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment "  + 
                    noSuchAlgoExc);
        }
        
        // Generating IV
        byte iv[] = Encryption.generateIV();
        
        byte[] encryptedText = Encryption.aesEncrypt(messageToEncrypt, aesKey, iv);
        
        assertNotEquals(messageToEncrypt, Base64.getEncoder().encodeToString(encryptedText));
        
        // Same key, IV and GCM Specs for decryption as used for encryption.
        byte[] decryptedText = Encryption.aesDecrypt(encryptedText, aesKey, iv);

        assertEquals(messageToEncrypt, new String(decryptedText));
    }
}
