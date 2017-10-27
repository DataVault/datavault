package org.datavaultplatform.worker.operations;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackagerTest {
    private static String packagerResources;
    private static File testDir;

    @BeforeClass
    public static void setUpClass() {
        String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";

        packagerResources = resourcesDir + File.separator + "packager";
        testDir = new File(resourcesDir + File.separator + "packager-output");
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
    public void testCreateBag() {
        final String TEST_FILE = "item.pdf";
        
        // files expected in bag
        final List<String> expectFiles = Arrays.asList(
                "bagit.txt",
                "item.pdf",
                "bag-info.txt",
                "manifest-md5.txt",
                "tagmanifest-md5.txt",
                "bagit.txt");

        File dir =  new File(testDir + File.separator + "createbag");
        dir.mkdir();

        try{
            FileUtils.copyFileToDirectory(
                    new File(packagerResources + File.separator + TEST_FILE),
                    dir);
        }
        catch(IOException ex){
            fail(ex.getMessage());
        }
        
        try {
            Packager.createBag(dir);

            // check bag contains expected files
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                    assertTrue(expectFiles.contains(file.getName()));
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testValidateBag() { 
        final String TEST_FILE = "item.pdf";
        File dir =  new File(testDir + File.separator + "createbag");
        dir.mkdir();
    
        try{
            FileUtils.copyFileToDirectory(
                    new File(packagerResources + File.separator + TEST_FILE),
                    dir);
        }
        catch(IOException ex){
            fail(ex.getMessage());
        }
        
        try{
            File bag = new File("/mnt/dataexchange/test/bagit");
            Packager.createBag(bag);
            assertTrue(Packager.validateBag(bag));
        }
        catch(Exception ex){
            fail(ex.getMessage());
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
        System.out.println("tearDown");

        BasicConfigurator.resetConfiguration();
    }
}
