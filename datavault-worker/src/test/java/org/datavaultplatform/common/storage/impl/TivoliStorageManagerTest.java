package org.datavaultplatform.common.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.datavaultplatform.common.io.Progress;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TivoliStorageManagerTest {
	
	private TivoliStorageManager tsm = null;
	
	@BeforeClass
	public static void setUpClass() {
	        
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

	private BufferedReader queryArchive(File working, String retVal) throws Exception{
		ProcessBuilder pb = new ProcessBuilder("dsmc", "query", "archive",  working.getAbsolutePath(), "-description=" + retVal);
		Process p = pb.start();
		p.waitFor();
		
		InputStream is = p.getInputStream();
		assertNotNull("InputStream should not be null", is);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//		String line = null;
//		while ( (line = reader.readLine()) != null) {
//			System.out.println(line);
//		}
		return reader;
	}
	
	private void deleteArchive(String retVal) throws Exception{
		
		String path = "/";
		ProcessBuilder pb = new ProcessBuilder("dsmc", "delete", "archive", path, "-subdir=yes", "-description=" + retVal, "-noprompt");
		System.out.println(pb.command());

		Process p = pb.start();
		p.waitFor();
	}
	
    @Test
    public void testStoreValidParams() {
    		Progress progress = new Progress();
    		File working  = new File("/tmp/test.tar");
    		String path = "/tmp";
    		String retVal = null;
    		try {
    			// store the tar
			retVal = tsm.store(path, working, progress);
			assertNotNull("RetVal should not be null", retVal);
    			// check it is now in TSM
			BufferedReader reader = this.queryArchive(working, retVal);
    			String line = null;
    			while ( (line = reader.readLine()) != null) {
    				assertFalse("Attempt to store failed", line.contains("No files matching search criteria were found"));
    			}
//    			// delete from TSM
    			deleteArchive(retVal);
		} catch (Exception e) {
			fail("Unexpected exception " + e.getMessage()); 
		}
    }
    
    @Test
    public void testStoreNonExistantFile() {
    	Progress progress = new Progress();
		File working  = new File("/tmp/testx.tar");
		String path = "/tmp";
		String retVal = null;
		try {
			// store the tar
		retVal = tsm.store(path, working, progress);
		// exception should be thrown by store so we should never get here
		fail("Exception should have been thrown"); 
		} catch (Exception e) {
			assertNotNull("Exception should not be null", e);
			assertEquals("Message not as expected", "Deposit of testx.tar failed. ", e.getMessage());
		}
    }
    
    @Test
    public void testRetrieveValidParams() {
    		
    }
    
    @Test
    public void testRetrieveNonExistantFile() {
    		
    }
    
    @Test
    public void testRetrieveValidParamsDuplicateFileNames() {
    		
    }
    
   
 //   @Test
//   public void testRetrieve() {
//    		Progress progress = new Progress();
//    		File working  = new File("/tmp/test.tar");
//    		String path = "/tmp";
//
//    		try {
//			tsm.retrieve(path, working, progress);
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage()); 
//		} 
//    		
//    		fail("Need to test the file was retrieved to the expected location");
//    		fail("Test me (retrieve");
//    }
	    
	@After
    public void tearDown() {
	       
    }
}
