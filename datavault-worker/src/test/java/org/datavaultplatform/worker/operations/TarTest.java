package org.datavaultplatform.worker.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
    private static File bigdataResourcesDir;
    private static File testDir;
    
    @BeforeClass
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        packagerResourcesDir = new File(resourcesPath + File.separator + "packager");
        bigdataResourcesDir = new File(resourcesPath + File.separator + "big_data");
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
    public void testSimpleFileGCMEncriptedTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleFileGCMEncriptedTarCreationAndExtraction...");
        
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
        
        Cipher cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, null);
        
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
        
        Cipher cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, null);
        
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
        System.out.println("End testSimpleFileGCMEncriptedTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @Test
    public void testSimpleDirectoryGCMEncryptedTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testSimpleDirectoryGCMEncryptedTarCreationAndExtraction...");
        
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
        
        Cipher cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, null);
        
        File outputTarFile = new File(testDir, "outputDirEnc.tar");
                
        // Create a Tar file from resource dir
        try {
            Tar.createEncryptedTar(packagerResourcesDir, outputTarFile, cIn);
        } catch (Exception e) {
            fail("Unexpected Exception thrown while creating tar: " + e);
        }
        
        Cipher cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, null);
        
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
        System.out.println("End testSimpleDirectoryGCMEncryptedTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
    @Test
    public void testBigFileTarCreationAndExtraction() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Start testBigFileTarCreationAndExtraction...");
        
        File inputFile = new File(bigdataResourcesDir, "big_file");
        
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
        
        final long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + 
                TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");
        
        try {
            Tar.unTar(outputTarFile, testDir.toPath());
        } catch (Exception e) {
            fail("Unexpected Exception thrown while extracting files from tar: " + e);
        }
        
        File expectedOutputFile = new File(testDir, "big_file");
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
        System.out.println("End testBigFileTarCreationAndExtraction");
        System.out.println("Total execution time: " + 
                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
    }
    
//    @Test
//    public void testBigFileGCMEncriptionAndDecryption() {
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
//        File outputTarFile = new File(testDir, "GCMencriptedOutputFile.tar");
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        final long midTime = System.currentTimeMillis();
//        System.out.println("\t Tar creation: " + 
//                TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");
//        
//        Cipher cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        File expectedOutputFile = new File(testDir, "big_file");
//        assertTrue(expectedOutputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(outputTarFile);
//        FileUtils.deleteQuietly(expectedOutputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileGCMEncriptionAndDecryption");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
//    
//    @Test
//    public void testBigFileCBCEncriptedTarCreationAndExtraction() {
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testBigFileCBCEncriptedTarCreationAndExtraction...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey();
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV(16);
//        
//        Cipher cIn = Encryption.initCBCCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
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
//        File outputTarFile = new File(testDir, "CBCencriptedOutputFile.tar");
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        Cipher cOut = Encryption.initCBCCipher(Cipher.DECRYPT_MODE, aesKey, iv);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        File expectedOutputFile = new File(testDir, "big_file");
//        assertTrue(expectedOutputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(outputTarFile);
//        FileUtils.deleteQuietly(expectedOutputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileCBCEncriptedTarCreationAndExtraction");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
//    
//    @Test
//    public void testBigFileCTREncriptedTarCreationAndExtraction() {
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testBigFileCTREncriptedTarCreationAndExtraction...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey();
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV(16);
//        
//        Cipher cIn = Encryption.initCTRCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
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
//        File outputTarFile = new File(testDir, "CBCencriptedOutputFile.tar");
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        Cipher cOut = Encryption.initCTRCipher(Cipher.DECRYPT_MODE, aesKey, iv);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        File expectedOutputFile = new File(testDir, "big_file");
//        assertTrue(expectedOutputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(outputTarFile);
//        FileUtils.deleteQuietly(expectedOutputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileCTREncriptedTarCreationAndExtraction");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
//    
//    @Test
//    public void testBigFileCFBEncriptedTarCreationAndExtraction() {
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testBigFileCFBEncriptedTarCreationAndExtraction...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey();
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV(16);
//        
//        Cipher cIn = Encryption.initCFBCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
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
//        File outputTarFile = new File(testDir, "CBCencriptedOutputFile.tar");
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        final long midTime = System.currentTimeMillis();
//        System.out.println("\t Tar creation: " + 
//                TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");
//        
//        Cipher cOut = Encryption.initCFBCipher(Cipher.DECRYPT_MODE, aesKey, iv);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        File expectedOutputFile = new File(testDir, "big_file");
//        assertTrue(expectedOutputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(outputTarFile);
//        FileUtils.deleteQuietly(expectedOutputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileCFBEncriptedTarCreationAndExtraction");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
    
//    @Test
//    public void testBigFileCCMEncriptedTarCreationAndExtraction() {
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testBigFileCCMEncriptedTarCreationAndExtraction...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey();
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV(13);
//        
//        Cipher cIn = Encryption.initCCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
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
//        File outputTarFile = new File(testDir, "CBCencriptedOutputFile.tar");
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(inputFile, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        Cipher cOut = Encryption.initCCMCipher(Cipher.DECRYPT_MODE, aesKey, iv);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        File expectedOutputFile = new File(testDir, "big_file");
//        assertTrue(expectedOutputFile.exists());
//
//        long csumOutputFile = 0;
//        try {
//            csumOutputFile = FileUtils.checksum(expectedOutputFile, new CRC32()).getValue();
//        } catch (IOException ioe) {
//            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
//        }
//        
//        assertEquals(csumInputFile, csumOutputFile);
//        
//        FileUtils.deleteQuietly(outputTarFile);
//        FileUtils.deleteQuietly(expectedOutputFile);
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testBigFileCCMEncriptedTarCreationAndExtraction");
//        System.out.println("Total execution time: " + 
//                TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "." +
//                TimeUnit.MILLISECONDS.toMillis(endTime - startTime) + " sec");
//    }
    
//    @Test
//    public void testBigDataEncryptedTarCreationAndExtraction() {
//        final long startTime = System.currentTimeMillis();
//        System.out.println("Start testSimpleDirectoryEncryptedTarCreationAndExtraction...");
//        
//        // Generating Key
//        SecretKey aesKey = null;
//        try {
//            aesKey = Encryption.generateSecretKey();
//        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
//            fail("Key being request is for AES algorithm, "
//                    + "but this cryptographic algorithm is not available in the environment "  + 
//                    noSuchAlgoExc);
//        }
//        
//        // Generating IV
//        byte iv[] = Encryption.generateIV();
//        
//        Cipher cIn = Encryption.initCipher(Cipher.ENCRYPT_MODE, aesKey, iv, null);
//        
//        File outputTarFile = new File(testDir, "bigData.tar.gz");
//                
//        // Create a Tar file from resource dir
//        try {
//            Tar.createEncryptedTar(bigdataResourcesDir, outputTarFile, cIn);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while creating tar: " + e);
//        }
//        
//        Cipher cOut = Encryption.initCipher(Cipher.DECRYPT_MODE, aesKey, iv, null);
//        
//        try {
//            Tar.unTar(outputTarFile, testDir.toPath(), cOut);
//        } catch (Exception e) {
//            fail("Unexpected Exception thrown while extracting files from tar: " + e);
//        }
//        
//        // Check output directory is as expected
//        File outputDir = new File(testDir + File.separator + bigdataResourcesDir.getName());
//        
//        for (File inputFile : bigdataResourcesDir.listFiles()) {
//            if (inputFile.isFile()) {
//                String fileName = inputFile.getName();
//                File outputFile = new File(outputDir, fileName);
//                
//                assertTrue(outputFile.exists());
//                
//                long csumInputFile = 0;
//                long csumOutputFile = 0;
//                try {
//                    csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
//                    csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
//                } catch (IOException ioe) {
//                    fail("Unexpected Exception thrown while getting checksum: " + ioe);
//                }
//                
//                assertEquals(csumInputFile, csumOutputFile);
//            }
//        }
//        
//
////        FileUtils.deleteQuietly(outputTarFile);
////        try {
////            FileUtils.deleteDirectory(outputDir);
////        } catch (IOException e) {
////            fail("Unexpected Exception thrown while deleting output fodler: " + e);
////        }
//
//        final long endTime = System.currentTimeMillis();
//        System.out.println("End testSimpleDirectoryEncryptedTarCreationAndExtraction");
//        System.out.println("Total execution time: " + 
//                String.format("%02d min, %02d sec", 
//                        TimeUnit.MILLISECONDS.toMinutes(startTime-endTime),
//                        TimeUnit.MILLISECONDS.toSeconds(startTime-endTime) - 
//                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime-endTime))
//                )
//        );
//    }
    
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
