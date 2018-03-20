package org.datavaultplatform.worker.operations;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EcryptionTest {

    private static File bigdataResourcesDir;
    private static File testDir;
    
    @BeforeClass
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        bigdataResourcesDir = new File(resourcesPath + File.separator + "big_data");
        testDir = new File(resourcesPath + File.separator + "tmp");
        
        System.out.println( "\nSecurity-Provider:" );
        for( Provider prov : Security.getProviders() ) {
            System.out.println( "  " + prov + ": " + prov.getInfo() );
        }
        
        
        try {
            System.out.println( "\nMaxAllowedKeyLength (for '" + Cipher.getInstance("AES").getProvider() + "' using current 'JCE Policy Files'):\n"
                    + "  DES        = " + Cipher.getMaxAllowedKeyLength( "DES"        ) + "\n"
                    + "  Triple DES = " + Cipher.getMaxAllowedKeyLength( "Triple DES" ) + "\n"
                    + "  AES        = " + Cipher.getMaxAllowedKeyLength( "AES"        ) + "\n"
                    + "  Blowfish   = " + Cipher.getMaxAllowedKeyLength( "Blowfish"   ) + "\n"
                    + "  RSA        = " + Cipher.getMaxAllowedKeyLength( "RSA"        ) + "\n" );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + e);
        }
    }

    @Before
    public void setUp() {
        try{
            testDir.mkdir();
        }
        catch(SecurityException se) {
            fail(se.getMessage());
        }

        BasicConfigurator.configure();
    }
    
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
        
        byte[] encryptedText = Encryption.aesGCMEncrypt(messageToEncrypt, aesKey, iv, aadData);
        
        assertNotNull(encryptedText);
        
        assertNotEquals(messageToEncrypt, Base64.getEncoder().encodeToString(encryptedText));
        
        // Same key, IV and GCM Specs for decryption as used for encryption.
        byte[] decryptedText = Encryption.aesGCMDecrypt(encryptedText, aesKey, iv, aadData);

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
        
        byte[] encryptedText = Encryption.aesGCMEncrypt(messageToEncrypt, aesKey, iv);
        
        assertNotEquals(messageToEncrypt, Base64.getEncoder().encodeToString(encryptedText));
        
        // Same key, IV and GCM Specs for decryption as used for encryption.
        byte[] decryptedText = Encryption.aesGCMDecrypt(encryptedText, aesKey, iv);

        assertEquals(messageToEncrypt, new String(decryptedText));
    }
    
    @Test
//    @Category(org.datavaultplatform.SlowTest.class)
    public void testBigFileGCMCripto(){
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testBigFileGCMCripto...");
        
        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment "  + 
                    noSuchAlgoExc);
        }
        
        // Generating IV
        byte iv[] = Encryption.generateIV();
        
        byte[] aadData = "random".getBytes() ; // Any random data can be used as tag. Some common examples could be domain name...
        
        Cipher cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
        
        File inputFile = new File(bigdataResourcesDir, "big_file");
        
        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }
        
        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doBufferedCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while extracting files from tar: " + sw);
        }
        
        final long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + 
                TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");
        
        File outputFile = new File(testDir, "big_file");
        
        Cipher cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
        
        try {
            Encryption.doBufferedCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while extracting files from tar: " + sw);
        }
        
        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }
        
        assertEquals(csumInputFile, csumOutputFile);
        
//        FileUtils.deleteQuietly(encryptedFile);
//        FileUtils.deleteQuietly(outputFile);

        final long endTime = System.currentTimeMillis();
        System.out.println("End testBigFileGCMCripto");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
//    @Test
//    public void testBigFileGCMEncriptionAndDecryption(){
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testBigFileGCMEncriptionAndDecryption...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey(128);
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV();
//        
//        byte[] aadData = "random".getBytes() ; // Any random data can be used as tag. Some common examples could be domain name...
//        
//        Cipher cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
//        
//        File inputFile = new File(bigdataResourcesDir, "big_file");
//        
//        long csumInputFile = 0;
//        try {
//            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
//        }
//        
//        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
//        // Create a Tar file from resource dir
//        try {
//            Encryption.encryptFile(inputFile, encryptedFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        final long midTime = System.currentTimeMillis();
//        System.out.println("\t Encryption: " + 
//                TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");
//        
//        File outputFile = new File(testDir, "big_file");
//        
//        Cipher cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
//        
//        try {
//            Encryption.decryptFile(encryptedFile, outputFile, cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        assertTrue(outputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(encryptedFile);
//        FileUtils.deleteQuietly(outputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileGCMEncriptionAndDecryption");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
}
