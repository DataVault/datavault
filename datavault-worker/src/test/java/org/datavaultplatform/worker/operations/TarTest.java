package org.datavaultplatform.worker.operations;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

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
    }
    
    @Test
    public void testSimpleDirectoryTarCreationAndExtraction() {
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
