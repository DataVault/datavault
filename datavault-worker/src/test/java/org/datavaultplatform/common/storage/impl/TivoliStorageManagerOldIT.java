package org.datavaultplatform.common.storage.impl;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.io.Progress;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class TivoliStorageManagerOldIT {
	
	private TivoliStorageManager tsm = null;
	private static String tsmResources;
	//private static File testDir;
	
	@BeforeAll
	public static void setUpClass() {
		String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";

        tsmResources = resourcesDir + File.separator + "tsm";
        //testDir = new File(resourcesDir, "tsm-output");   
	}

	@BeforeEach
	public void setUp() {
		String name = "Test";
		Map<String,String> config = new HashMap<>();
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
		assertNotNull(is, "InputStream should not be null");
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
    //@Category(org.datavaultplatform.test.TSMTest.class)
    public void testStoreValidParams() {
    		Progress progress = new Progress();
    		File working  = new File(tsmResources, "test.tar");
    		String path = "/tmp";
    		String retVal;
    		try {
    			// store the tar
			retVal = tsm.store(path, working, progress);
			assertNotNull(retVal, "RetVal should not be null");
    			// check it is now in TSM node 1
			BufferedReader reader = this.queryArchive(working, retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT );
    			String line;
    			while ( (line = reader.readLine()) != null) {
    				assertFalse(line.contains("No files matching search criteria were found"), "Node1 attempt to store failed");
    			}

    			reader = this.queryArchive(working, retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
					while ( (line = reader.readLine()) != null) {
    				assertFalse(line.contains("No files matching search criteria were found"), "Node2 attempt to store failed");
    			}

    			// delete from TSM node 1
    			deleteArchive(retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
    			deleteArchive(retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
		} catch (Exception e) {
			fail("Unexpected exception " + e.getMessage());
		}
    }
    
    @Test
    //@Category(org.datavaultplatform.test.TSMTest.class)
    public void testStoreNonExistantFile() {
    		Progress progress = new Progress();
		File working  = new File(tsmResources, "testx.tar");
		String path = "/tmp";
		try {
			// store the tar
			tsm.store(path, working, progress);
		// exception should be thrown by store so we should never get here
		fail("Exception should have been thrown");
		} catch (Exception e) {
			assertNotNull(e, "Exception should not be null");
			assertEquals("Deposit of testx.tar using /opt/tivoli/tsm/client/ba/bin/dsm1.opt failed. ", e.getMessage(), "Message not as expected");
		}
    }
    
    @Test
    //@Category(org.datavaultplatform.test.TSMTest.class)
    public void testRetrieveValidParams() {
	    	Progress progress = new Progress();
		File working  = new File(tsmResources, "test.tar");
		File temp  = new File(tsmResources, "test.tar.tmp");
		String path = "/tmp";
		String retVal;
		try {
			// store the tar
			System.out.println("Store the tar");
			retVal = tsm.store(path, working, progress);
			//File retrieved = new File("/tmp/" + retVal);
			assertNotNull(retVal, "RetVal should not be null");
			// check it is now in TSM node 1
			System.out.println("check it has been archived");
			BufferedReader reader = this.queryArchive(working, retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
			String line;
			while ( (line = reader.readLine()) != null) {
				assertFalse(line.contains("No files matching search criteria were found"), "Node 1 attempt to store failed");
			}
			
			reader = this.queryArchive(working, retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
			while ( (line = reader.readLine()) != null) {
				assertFalse(line.contains("No files matching search criteria were found"), "Node 2 attempt to store failed");
			}
			
			// temp move the file (so we can retrieve without overwriting)
			FileUtils.moveFile(working, temp);
			assertFalse(working.exists(), "The working file shouldn't exist");
			
			// retrieve from TSM node 1
			System.out.println("Retrieve the tar");
			//dsmc  retrieve /tmp/datavault/temp/2848@ubuntu-xenial/513c2b11-30df-4947-846b-a64309c61eb8.tar
			tsm.retrieve(path, working, progress, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
			
			// check that the /tmp/"retval" dir now exists
			System.out.println("Check the retrieve archive exits");
			assertTrue(working.exists(), "The retrieved archive doesn't exist");
			
			// delete from TSM node 1
			System.out.println("Delete from TSM");
			deleteArchive(retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
			
			// delete from TSM node2
			deleteArchive(retVal, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
			
			// delete the retrieved tar
			System.out.println("Delete the retrieved archive");
			boolean deleted = working.delete();
			assertTrue(deleted, "Retrieved archive not cleaned up");
			
			// move the source file back
			FileUtils.moveFile(temp, working);
			assertTrue(working.exists(), "The working file should exist");
		} catch (Exception e) {
			fail("Unexpected exception " + e.getMessage());
		}
    }
    
    @Test
    //@Category(org.datavaultplatform.test.TSMTest.class)
    public void testRetrieveNonExistantFileFromNodeOne() {
    		// retrieve from TSM
     	Progress progress = new Progress();
    		File working  = new File("/tmp/test.tar");
    		String path = "/tmp";
    		
    		try {
	    		System.out.println("Retrieve the tar");
	    		//dsmc  retrieve /tmp/datavault/temp/2848@ubuntu-xenial/513c2b11-30df-4947-846b-a64309c61eb8.tar
	    		tsm.retrieve(path, working, progress, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE1_OPT);
	    		fail("Exception should have been thrown");
    		} catch (Exception e) {
    			assertNotNull(e, "Exception should not be null");
    			assertEquals("Retrieval of test.tar failed. ", e.getMessage(), "Message not as expected");
    		}
    }
    
    @Test
    //@Category(org.datavaultplatform.test.TSMTest.class)
    public void testRetrieveNonExistantFileFromNodeTwo() {
    		// retrieve from TSM
     	Progress progress = new Progress();
    		File working  = new File("/tmp/test.tar");
    		String path = "/tmp";
    		
    		try {
	    		System.out.println("Retrieve the tar");
	    		//dsmc  retrieve /tmp/datavault/temp/2848@ubuntu-xenial/513c2b11-30df-4947-846b-a64309c61eb8.tar
	    		tsm.retrieve(path, working, progress, TivoliStorageManager.DEFAULT_TSM_SERVER_NODE2_OPT);
	    		fail("Exception should have been thrown");
    		} catch (Exception e) {
    			assertNotNull(e, "Exception should not be null");
    			assertEquals("Retrieval of test.tar failed. ", e.getMessage(), "Message not as expected");
    		}
    }
    
		@AfterEach
    public void tearDown() {
    }
}
