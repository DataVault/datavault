package org.datavaultplatform.common.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
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

    @Test
    public void testStore() {
    		Progress progress = new Progress();
    		File working  = new File("/tmp/test.tar");
    		String path = "/tmp";
    		String retVal = null;
    		String expected = "not sure what this should be yet";
//    		try {
//			retVal = tsm.store(path, working, progress);
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage()); 
//		} 
//    		
//    		assertNotNull("RetVal should not be null", retVal);
//    		assertEquals("RetVal not as expected", expected, retVal);
    }
    
    @Test
    public void testRetrieve() {
    		Progress progress = new Progress();
    		File working  = new File("/tmp/test.tar");
    		String path = "/tmp";

//    		try {
//			tsm.retrieve(path, working, progress);
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage()); 
//		} 
//    		
//    		fail("Need to test the file was retrieved to the expected location");
    }
    
    @Test
    public void testGetUsableSpace() {
	    long freeSpace = 0;
	    long expectedFreeSpace = 666; // dunno what this is yet
	    
//		try {
//			freeSpace = tsm.getUsableSpace();
//		} catch (Exception e) {
//			fail("Unexpected exception " + e.getMessage());
//		}
//	      
//	    assertNotNull("Free space value is null", freeSpace);
//	    assertEquals("Free space value is not as expected", freeSpace, expectedFreeSpace);
    }
    
//    @Test
//    public void testGetUsableSpaceToolBox() {
//	    long freeSpace = 0;
//	    long expectedFreeSpace = 666; // dunno what this is yet
//	    
//		try {
//			freeSpace = tsm.getUsableSpaceToolBox();
//		} catch (Exception e) {
//			fail("Could not construct TivoliStorageManager object");
//		}
//	      
//	    assertNotNull("Free space value is null", freeSpace);
//	    assertEquals("Free space value is not as expected", freeSpace, expectedFreeSpace);
//    }
	    
	@After
    public void tearDown() {
	       
    }
}
