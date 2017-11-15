package org.datavaultplatform.worker.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;

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
            Bag bag = Packager.createBag(dir);
            assertNotNull(bag);
            
            // check bag contains expected files
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    assertTrue(expectFiles.contains(file.getName()));
                }
            }
            
            File testFile =  new File(dir + File.separator + "data", TEST_FILE);
            assertTrue(testFile.exists());
            
            // check test file checksum
            FileInputStream fis = new FileInputStream(testFile);
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();
            Manifest manifest= bag.getPayLoadManifests().iterator().next();
            assertEquals(md5, manifest.getFileToChecksumMap().get(testFile.toPath()));
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
        final String TEST_FILE_WITH_SPACE = "peacock butterfly.jpeg";
        final String EMPTY_DIR_NAME = "emptydir";
        final String CHILD_DIR_NAME = "childdir";
        
        File parentDir =  new File(testDir, "validatebag");
        parentDir.mkdir();
        File emptyDir =  new File(parentDir, EMPTY_DIR_NAME);
        emptyDir.mkdir();
        File childDir =  new File(parentDir, CHILD_DIR_NAME);
        File test1file = null;
        File test2file = null;
        File test3file = null;
        
        try{
            test1file = new File(packagerResources, TEST_FILE1);
            FileUtils.copyFileToDirectory(
                    new File(packagerResources, TEST_FILE1),
                    parentDir);
            test2file = new File(packagerResources, TEST_FILE2);
            FileUtils.copyFileToDirectory(test2file, childDir);
            test3file = new File(packagerResources, TEST_FILE_WITH_SPACE);
            FileUtils.copyFileToDirectory(test3file, parentDir);
            
            // create symlink in parent file to file in child
            // TODO: currently doesn't work
            /*Path source = Paths.get(childDir.getAbsolutePath() + File.separator + TEST_FILE2);
            Path link = Paths.get(parentDir.getAbsolutePath() + File.separator + TEST_FILE2);
            Path relativeSrc = link.getParent().relativize(source); 
            Files.createSymbolicLink(link, relativeSrc);*/
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
            assertTrue(lines.contains(this.getChecksum(test1file) + "  data" +
                    File.separator + TEST_FILE1));
            assertTrue(lines.contains(this.getChecksum(test2file) + "  data" +
                    File.separator + CHILD_DIR_NAME + File.separator + TEST_FILE2));
            assertTrue(lines.contains(this.getChecksum(test3file) + "  data" +
                    File.separator + TEST_FILE_WITH_SPACE));
           
            // #2 check symlinks
            // TODO
            
            // #3 check empty dir exists
            // doesn't work
            //assertTrue(new File(parentDir.getAbsolutePath(), EMPTY_DIR_NAME).isDirectory());
            
            // # 4 check files exist
            assertTrue(new File(parentDir + File.separator + "data",
                    TEST_FILE1).exists());
            assertTrue(new File(parentDir + File.separator + "data" + File.separator + CHILD_DIR_NAME,
                    TEST_FILE2).exists());
            assertTrue(new File(parentDir + File.separator + "data",
                    TEST_FILE_WITH_SPACE).exists());
        }        
        catch(Exception ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    } 
    
    @Test
    public void testAddMetadata() {
        final String TEST_FILE = "item.pdf";
        File dir =  new File(testDir, "addmetadata");
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
            if(Packager.createBag(dir) != null){                
                // the contents of the metadata files are of no
                // interest for the purposes of this test
                final String DEPOSIT_META = "jings";
                final String VAULT_META = "crivvens";
                final String FT_META = "help ma";
                final String EXT_META = "boab";
                
                Packager.addMetadata(dir, DEPOSIT_META, VAULT_META, FT_META, EXT_META);
                
                // check contents of metadata files
                File df = new File(dir + File.separator + Packager.metadataDirName, Packager.depositMetaFileName);
                assertEquals(DEPOSIT_META, FileUtils.readFileToString(df));
                File vf = new File(dir + File.separator + Packager.metadataDirName, Packager.vaultMetaFileName);
                assertEquals(VAULT_META, FileUtils.readFileToString(vf));
                File ff = new File(dir + File.separator + Packager.metadataDirName, Packager.fileTypeMetaFileName);
                assertEquals(FT_META, FileUtils.readFileToString(ff));
                File ef = new File(dir + File.separator + Packager.metadataDirName, Packager.externalMetaFileName);
                assertEquals(EXT_META, FileUtils.readFileToString(ef));
                
                // validate checksum is tagmanifest  
                List<String> lines = FileUtils.readLines(
                        new File(dir.getAbsolutePath() + File.separator + "tagmanifest-md5.txt"));
                assertTrue(lines.contains(DigestUtils.md5Hex(DEPOSIT_META) + "  metadata" +
                        File.separator + Packager.depositMetaFileName));
                assertTrue(lines.contains(DigestUtils.md5Hex(VAULT_META) + "  metadata" +
                        File.separator + Packager.vaultMetaFileName));
                assertTrue(lines.contains(DigestUtils.md5Hex(FT_META) + "  metadata" +
                        File.separator + Packager.fileTypeMetaFileName));
                assertTrue(lines.contains(DigestUtils.md5Hex(EXT_META) + "  metadata" +
                        File.separator + Packager.externalMetaFileName));
            }
            else{
                fail("Bag creation failed");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }
    
    @Test
    public void testExtractMetadata() {
        final String TEST_FILE = "item.pdf";
        File dir =  new File(testDir, "extractmetadata");
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
            File metaDir =  new File(testDir, "tmp");
            if(Packager.createBag(dir) != null && Packager.extractMetadata(dir, metaDir)){
                // check metadata files are in new directory
                assertTrue(new File(metaDir, "tagmanifest-md5.txt").exists());
                assertTrue(new File(metaDir, "bag-info.txt").exists());
                assertTrue(new File(metaDir, "manifest-md5.txt").exists());
                assertTrue(new File(metaDir, "bagit.txt").exists());
            }
            else{
                fail("Problem extracting to metadata"); 
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }
    @Test
    public void testInvalidFilenames() {
        final String TEST_FILE = "col:n.tiff";
        File dir =  new File(testDir, "invalidfilenames");
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
            if(Packager.createBag(dir) != null){
                // TODO: this should fail
                //fail(TEST_FILE + " is not a valid filename");
            } 
        }
        catch(Exception ex) {
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
