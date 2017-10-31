package org.datavaultplatform.worker.operations;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
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
        testDir = new File(resourcesDir, "packager-output");
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
                TEST_FILE,
                "bag-info.txt",
                "manifest-md5.txt",
                "tagmanifest-md5.txt",
                "bagit.txt");

        File dir =  new File(testDir, "createbag");
        dir.mkdir();

        try{
            FileUtils.copyFileToDirectory(
                    new File(packagerResources, TEST_FILE),
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
        final String TEST_FILE1 = "item.pdf";
        final String TEST_FILE2 = "banjo.jpg";
        final String EMPTY_DIR_NAME = "emptydir";
        final String CHILD_DIR_NAME = "childdir";
        
        File parentDir =  new File(testDir, "validatebag");
        parentDir.mkdir();
        File emptyDir =  new File(parentDir, EMPTY_DIR_NAME);
        emptyDir.mkdir();
        File childDir =  new File(parentDir, CHILD_DIR_NAME);
        File test1file = null;
        File test2file = null;
        
        try{
            test1file = new File(packagerResources, TEST_FILE1);
            FileUtils.copyFileToDirectory(
                    new File(packagerResources, TEST_FILE1),
                    parentDir);
            test2file = new File(packagerResources, TEST_FILE2);
            FileUtils.copyFileToDirectory(test2file, childDir);
            
            // create symlink in parent file to file in child
            // TODO: currently doesn't work
            /*System.setProperty("user.dir", parentDir.getAbsolutePath());
            Files.createSymbolicLink(
                    Paths.get(TEST_FILE2),
                    Paths.get("childdir" + File.separator + TEST_FILE2));*/
        }
        catch(IOException ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        
        try{
            Packager.createBag(parentDir);
            assertTrue(Packager.validateBag(parentDir));

            // #1 check files in manifest 
            List<String> lines = FileUtils.readLines(
                    new File(parentDir.getAbsolutePath() + File.separator + "manifest-md5.txt"));
            
         
            // the order of files in manifest may change
            assertTrue(lines.contains(this.getChecksum(test1file) + "  data" +
                    File.separator + TEST_FILE1));
            assertTrue(lines.contains(this.getChecksum(test2file) + "  data" +
                    File.separator + CHILD_DIR_NAME + File.separator + TEST_FILE2));
           
            // #2 check symlinks
            // TODO
            
            // #3 check empty dir exists
            assertTrue(new File(parentDir.getAbsolutePath(), EMPTY_DIR_NAME).isDirectory());
        }
        
        catch(Exception ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
    // get the mdf checksum of a file
    private String getChecksum(File f){
        String cksum = null;
        try{
            FileInputStream fis = new FileInputStream(f);
            cksum = DigestUtils.md5Hex(fis);
            fis.close();
        }
        catch(IOException ex){
            fail(ex.getMessage());
        }
        
        return cksum;
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
