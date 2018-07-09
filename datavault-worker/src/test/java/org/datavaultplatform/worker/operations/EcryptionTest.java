package org.datavaultplatform.worker.operations;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.datavaultplatform.common.crypto.Encryption;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EcryptionTest {

    private static File bigdataResourcesDir;
    private static File testDir;

    @BeforeClass
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources";

        bigdataResourcesDir = new File(resourcesPath + File.separator + "big_data");
        testDir = new File(resourcesPath + File.separator + "tmp");

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        System.out.println("\nSecurity-Provider:");
        for (Provider prov : Security.getProviders()) {
            System.out.println("  " + prov + ": " + prov.getInfo());
        }
    }

    @Before
    public void setUp() {
        try {
            testDir.mkdir();
        } catch (SecurityException se) {
            fail(se.getMessage());
        }

        BasicConfigurator.configure();
    }

    @Test
    public void testBigFileGCMCriptoWithoutAAD() {
        System.out.println("Start testBigFileGCMCriptoWithoutAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte iv[] = Encryption.generateIV(Encryption.IV_SIZE);
        
        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "big_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "big_file");
        
        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);

        System.out.println("End testBigFileGCMCriptoWithoutAAD");
    }

    @Test
    public void testBigFileGCMCriptoWithAAD() {
        System.out.println("Start testBigFileGCMCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte iv[] = Encryption.generateIV(Encryption.IV_SIZE);
        
        // Any random data can be used as tag. Some common examples could be domain name
        byte[] aadData = "bd67a172c130822d6c306e02b1d9544f".getBytes();
        
        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }
        
        File inputFile = new File(bigdataResourcesDir, "big_file"); // big_file

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "big_file");
        
        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);
        
        System.out.println("End testBigFileGCMCriptoWithAAD");
    }
    
    @Test
    @Category(org.datavaultplatform.SlowTest.class)
    public void testHugeFileGCMCriptoWithAAD() {
        System.out.println("Start testHugeFileGCMCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte iv[] = Encryption.generateIV(Encryption.IV_SIZE);
        
        // Any random data can be used as tag. Some common examples could be domain name
        byte[] aadData = "bd67a172c130822d6c306e02b1d9544f".getBytes();

        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "1GB_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "huge_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);
        
        System.out.println("End testHugeFileGCMCriptoWithAAD");
    }
    
    @Test
    @Category(org.datavaultplatform.SlowTest.class)
    public void testHugeFileCBCCriptoWithAAD() {
        System.out.println("Start testHugeFileCBCCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte iv[] = Encryption.generateIV(Encryption.IV_CBC_SIZE);

        Cipher cIn = null;
        try {
            cIn = Encryption.initCBCCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "1GB_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "huge_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initCBCCipher(Cipher.DECRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);
        
        System.out.println("End testHugeFileCBCCriptoWithAAD");
    }

}
