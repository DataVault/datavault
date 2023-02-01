package org.datavaultplatform.worker.operations;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class TarTest {
    private static File packagerResourcesDir;
    private static File bigdataResourcesDir;
    private static File testDir;
    
    @BeforeAll
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";
        
        packagerResourcesDir = new File(resourcesPath + File.separator + "packager");
        bigdataResourcesDir = new File(resourcesPath + File.separator + "big_data");
        testDir = new File(resourcesPath + File.separator + "tmp");
    }

    @BeforeEach
    public void setUp() {
        try {
            testDir.mkdir();
        } catch(SecurityException se) {
            fail(se.getMessage());
        }
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
    
    @AfterEach
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(testDir);
        } catch(IOException ex) {
            fail(ex.getMessage());
        }
    }

    /** Tar up actual files but do not write output tar file **/
    @SneakyThrows
    @Test
    void testTarBigDataDirectoryIgnoreTarOutput() {
        File directoryToTar = new ClassPathResource("big_data").getFile();
        Tar2.createTar(directoryToTar, null);
    }

}
