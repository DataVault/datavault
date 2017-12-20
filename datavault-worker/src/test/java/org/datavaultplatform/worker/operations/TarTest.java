package org.datavaultplatform.worker.operations;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TarTest {
    private static File packagerResourcesDir;
    private static File testDir;
    
    @BeforeClass
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        packagerResourcesDir = new File(resourcesPath + File.separator + "packager");
        testDir = new File(resourcesPath + File.separator + "tmp");
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
    public void testSimpleFileTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleFileTarCreationAndExtraction...");
        
        File inputFile = new File(packagerResourcesDir, "item.pdf");
        
        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }
        
        File outputTarFile = new File(testDir, "outputFile.tar");
        // Create a Tar file from resource dir
        try {
            Tar.createTar(inputFile, outputTarFile);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while creating tar: " + e);
        }
        
        try {
            Tar.unTar(outputTarFile, testDir.toPath());
        } catch (Exception e) {
            fail("Unexpected Exception thrown while extracting files from tar: " + e);
        }
        
        File expectedOutputFile = new File(testDir, "item.pdf");
        assertTrue(expectedOutputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }
        
        assertEquals(csumInputFile, csumOutputFile);
        
        FileUtils.deleteQuietly(outputTarFile);
        FileUtils.deleteQuietly(expectedOutputFile);
        
        final long endTime = System.currentTimeMillis();
        System.out.println("End testSimpleFileTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @Test
    public void testSimpleDirectoryTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleDirectoryTarCreationAndExtraction...");
        
        File outputTarFile = new File(testDir, "outputFile.tar");
                
        // Create a Tar file from resource dir
        try {
            Tar.createTar(packagerResourcesDir, outputTarFile);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while creating tar: " + e);
        }
        
        try {
            Tar.unTar(outputTarFile, testDir.toPath());
        } catch (Exception e) {
            fail("Unexpected Exception thrown while extracting files from tar: " + e);
        }
        
        // Check output directory is as expected
        File outputDir = new File(testDir + File.separator + packagerResourcesDir.getName());
        
        for (File inputFile : packagerResourcesDir.listFiles()) {
            if (inputFile.isFile()) {
                String fileName = inputFile.getName();
                File outputFile = new File(outputDir, fileName);
                
                assertTrue(outputFile.exists());
                
                long csumInputFile = 0;
                long csumOutputFile = 0;
                try {
                    csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
                    csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
                } catch (IOException ioe) {
                    fail("Unexpected Exception thrown while getting checksum: " + ioe);
                }
                
                assertEquals(csumInputFile, csumOutputFile);
            }
        }

        FileUtils.deleteQuietly(outputTarFile);
        try {
            FileUtils.deleteDirectory(outputDir);
        } catch (IOException e) {
            fail("Unexpected Exception thrown while deleting output fodler: " + e);
        }

        final long endTime = System.currentTimeMillis();
        System.out.println("End testSimpleDirectoryTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @Test
    public void testSimpleFileEncriptedTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleFileEncriptedTarCreationAndExtraction...");
        
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
        
        Cipher cIn = Encryption.initCipher(Cipher.ENCRYPT_MODE, aesKey, iv, null);
        
        File inputFile = new File(packagerResourcesDir, "item.pdf");
        
        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }
        
        File outputTarFile = new File(testDir, "encriptedOutputFile.tar");
        // Create a Tar file from resource dir
        try {
            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while creating tar: " + e);
        }
        
        Cipher cOut = Encryption.initCipher(Cipher.DECRYPT_MODE, aesKey, iv, null);
        
        try {
            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while extracting files from tar: " + e);
        }
        
        File expectedOutputFile = new File(testDir, "item.pdf");
        assertTrue(expectedOutputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }
        
        assertEquals(csumInputFile, csumOutputFile);
        
        FileUtils.deleteQuietly(outputTarFile);
        FileUtils.deleteQuietly(expectedOutputFile);

        final long endTime = System.currentTimeMillis();
        System.out.println("End testSimpleFileEncriptedTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @Test
    public void testSimpleDirectoryEncryptedTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleDirectoryEncryptedTarCreationAndExtraction...");
        
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
        
        Cipher cIn = Encryption.initCipher(Cipher.ENCRYPT_MODE, aesKey, iv, null);
        
        File outputTarFile = new File(testDir, "outputFile.tar");
                
        // Create a Tar file from resource dir
        try {
            Tar.createEncryptedTar(packagerResourcesDir, outputTarFile, cIn);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while creating tar: " + e);
        }
        
        Cipher cOut = Encryption.initCipher(Cipher.DECRYPT_MODE, aesKey, iv, null);
        
        try {
            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while extracting files from tar: " + e);
        }
        
        // Check output directory is as expected
        File outputDir = new File(testDir + File.separator + packagerResourcesDir.getName());
        
        for (File inputFile : packagerResourcesDir.listFiles()) {
            if (inputFile.isFile()) {
                String fileName = inputFile.getName();
                File outputFile = new File(outputDir, fileName);
                
                assertTrue(outputFile.exists());
                
                long csumInputFile = 0;
                long csumOutputFile = 0;
                try {
                    csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
                    csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
                } catch (IOException ioe) {
                    fail("Unexpected Exception thrown while getting checksum: " + ioe);
                }
                
                assertEquals(csumInputFile, csumOutputFile);
            }
        }

        FileUtils.deleteQuietly(outputTarFile);
        try {
            FileUtils.deleteDirectory(outputDir);
        } catch (IOException e) {
            fail("Unexpected Exception thrown while deleting output fodler: " + e);
        }

        final long endTime = System.currentTimeMillis();
        System.out.println("End testSimpleDirectoryEncryptedTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @After
    public void tearDown() {
        try{
            FileUtils.deleteDirectory(testDir);
        }
        catch(IOException ex){
            fail(ex.getMessage());
        }

        BasicConfigurator.resetConfiguration();
    }

}
