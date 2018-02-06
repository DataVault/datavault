package org.datavaultplatform.common.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.io.Progress;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TivoliStorageManagerTest {
	
	private TivoliStorageManager tsm = null;
	private static String tsmResources;
	//private static File testDir;
	
	@BeforeClass
	public static void setUpClass() {
		String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";

        tsmResources = resourcesDir + File.separator + "tsm";
        //testDir = new File(resourcesDir, "tsm-output");   
	}

	@Before
	public void setUp() {
		String name = "Test";
		Map<String,String> config = new HashMap<String, String>();
		try {
			tsm = new TivoliStorageManager(name, config);
		} catch (Exception e) {
			fail("Could not construct TivoliStorageManager object");
		}
    }

	private BufferedReader queryArchive(File working, String description,  String optFilePath) throws Exception{
		ProcessBuilder pb = new ProcessBuilder("dsmc", "query", "archive",  working.getAbsolutePath(), "-description=" + description, "-optfile=" + optFilePath);
		Process p = pb.start();
		p.waitFor();
		
		InputStream is = p.getInputStream();
		assertNotNull("InputStream should not be null", is);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		return reader;
	}
	
	private void deleteArchive(String description,  String optFilePath) throws Exception{
		
		String path = "/";
		ProcessBuilder pb = new ProcessBuilder("dsmc", "delete", "archive", path, "-subdir=yes", "-description=" + description, "-optfile=" + optFilePath, "-noprompt");
		//System.out.println(pb.command());

		Process p = pb.start();
		p.waitFor();
	}
	
    @Test
    public void testStoreValidParams() {
//    		Progress progress = new Progress();
//    		File working  = new File(tsmResources, "test.tar");
//    		String path = "/tmp";
//    		String retVal = null;
//    		try {
//    			// store the tar
//			retVal = tsm.store(path, working, progress);
//			assertNotNull("RetVal should not be null", retVal);
//    			// check it is now in TSM node 1
//			BufferedReader reader = this.queryArchive(working, retVal, TivoliStorageManager.TSM_SERVER_NODE1_OPT);
//    			String line = null;
//    			while ( (line = reader.readLine()) != null) {
//    				assertFalse("Node1 attempt to store failed", line.contains("No files matching search criteria were found"));
//    			}
//    			
//    			reader = this.queryArchive(working, retVal, TivoliStorageManager.TSM_SERVER_NODE2_OPT);
//    			line = null;
//    			while ( (line = reader.readLine()) != null) {
//    				assertFalse("Node2 attempt to store failed", line.contains("No files matching search criteria were found"));
//    			}
//    			
//    			// delete from TSM node 1
//    			deleteArchive(retVal, TivoliStorageManager.TSM_SERVER_NODE1_OPT);
//    			deleteArchive(retVal, TivoliStorageManager.TSM_SERVER_NODE2_OPT);
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage()); 
//		}
    }
    
    @Test
    public void testStoreNonExistantFile() {
//    		Progress progress = new Progress();
//		File working  = new File(tsmResources, "testx.tar");
//		String path = "/tmp";
//		try {
//			// store the tar
//			tsm.store(path, working, progress);
//		// exception should be thrown by store so we should never get here
//		fail("Exception should have been thrown"); 
//		} catch (Exception e) {
//			assertNotNull("Exception should not be null", e);
//			assertEquals("Message not as expected", "Deposit of testx.tar using /opt/tivoli/tsm/client/ba/bin/dsm1.opt failed. ", e.getMessage());
//		}
    }
    
    @Test
    public void testRetrieveValidParams() {
//	    	Progress progress = new Progress();
//		File working  = new File(tsmResources, "test.tar");
//		File temp  = new File(tsmResources, "test.tar.tmp");
//		String path = "/tmp";
//		String retVal = null;
//		try {
//			// store the tar
//			System.out.println("Store the tar");
//			retVal = tsm.store(path, working, progress);
//			//File retrieved = new File("/tmp/" + retVal);
//			assertNotNull("RetVal should not be null", retVal);
//			// check it is now in TSM node 1
//			System.out.println("check it has been archived");
//			BufferedReader reader = this.queryArchive(working, retVal, TivoliStorageManager.TSM_SERVER_NODE1_OPT);
//			String line = null;
//			while ( (line = reader.readLine()) != null) {
//				assertFalse("Node 1 attempt to store failed", line.contains("No files matching search criteria were found"));
//			}
//			
//			reader = this.queryArchive(working, retVal, TivoliStorageManager.TSM_SERVER_NODE2_OPT);
//			line = null;
//			while ( (line = reader.readLine()) != null) {
//				assertFalse("Node 2 attempt to store failed", line.contains("No files matching search criteria were found"));
//			}
//			
//			// temp move the file (so we can retrieve without overwriting)
//			FileUtils.moveFile(working, temp);
//			assertFalse("The working file shouldn't exist", working.exists());
//			
//			// retrieve from TSM
//			System.out.println("Retrieve the tar");
//			//dsmc  retrieve /tmp/datavault/temp/2848@ubuntu-xenial/513c2b11-30df-4947-846b-a64309c61eb8.tar
//			tsm.retrieve(path, working, progress);
//			
//			// check that the /tmp/"retval" dir now exists
//			System.out.println("Check the retrieve archive exits");
//			assertTrue("The retrieved archive doesn't exist", working.exists());
//			
//			// delete from TSM node 1
//			System.out.println("Delete from TSM");
//			deleteArchive(retVal, TivoliStorageManager.TSM_SERVER_NODE1_OPT);
//			
//			// delete from TSM node2
//			deleteArchive(retVal, TivoliStorageManager.TSM_SERVER_NODE2_OPT);
//			
//			// delete the retrieved tar
//			System.out.println("Delete the retrieved archive");
//			Boolean deleted = working.delete();
//			assertTrue("Retrieved archive not cleaned up", deleted);
//			
//			// move the source file back
//			FileUtils.moveFile(temp, working);
//			assertTrue("The working file should exist", working.exists());
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage()); 
//		}
    }
    
    @Test
    public void testRetrieveNonExistantFile() {
    		// retrieve from TSM
//     	Progress progress = new Progress();
//    		File working  = new File("/tmp/test.tar");
//    		String path = "/tmp";
//    		
//    		try {
//	    		System.out.println("Retrieve the tar");
//	    		//dsmc  retrieve /tmp/datavault/temp/2848@ubuntu-xenial/513c2b11-30df-4947-846b-a64309c61eb8.tar
//	    		tsm.retrieve(path, working, progress);
//	    		fail("Exception should have been thrown"); 
//    		} catch (Exception e) {
//    			assertNotNull("Exception should not be null", e);
//    			assertEquals("Message not as expected", "Retrieval of test.tar failed. ", e.getMessage());
//    		}
    }
    
	@After
    public void tearDown() {
	       
    }
}
