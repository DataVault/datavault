package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TivoliStorageManager extends Device implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(TivoliStorageManager.class);
    public static final String TSM_SERVER_NODE1_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm1.opt";
    public static final String TSM_SERVER_NODE2_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm2.opt";
    //public List<String> locations = null;

    // todo : can we change this to COPY_BACK?
    public Verify.Method verificationMethod = Verify.Method.LOCAL_ONLY;

    public TivoliStorageManager(String name, Map<String,String> config) throws Exception  {
        super(name, config);
        locations = new ArrayList<String>();
        locations.add(TivoliStorageManager.TSM_SERVER_NODE1_OPT);
        locations.add(TivoliStorageManager.TSM_SERVER_NODE2_OPT);
        super.multipleCopies = true;

        // Unpack the config parameters (in an implementation-specific way)
        // Actually I can't think of any parameters that we need.
    }
    
    @Override
    public long getUsableSpace() throws Exception {
    		long retVal = 0;

        ProcessBuilder pb = new ProcessBuilder("dsmc", "query", "filespace");

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Filespace output failed.");
            logger.info(p.getErrorStream().toString());
            logger.info(p.getOutputStream().toString());
            throw new Exception("Filespace output failed.");
        }
        
        // need to parse the output to get the usable space value
        // this looks like it will be a bit of a pain
        // I suspect the format might be quite awkward
        // (need to wait till I can actually connect to a tsm before I can do this)

        return retVal;
    }
    
    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
    		// todo : monitor progress
    	
    		// do we need to check if the selected retrieval space has enough free space? (is the file bagged and tarred atm or is the actual space going to be different?)
    		// actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
    	
    		// The working file appears to be bagged and tarred when we get here
    		// in the local version of this class the FileCopy class adds info to the progess object
    		// I don't think we need to use the patch at all in this version 

    		//try {
    			// if this fails try to retrieve from second node, I think the only failure possible
    			// at this stage is a missing archive, checksums are checked later in Retrieve.java
    		//	this.retrieve(path, working, progress, TivoliStorageManager.TSM_SERVER_NODE1_OPT);
    		//} catch (Exception node1) {
    			// if this fails too just continue on to the error display
    		//	this.retrieve(path, working, progress, TivoliStorageManager.TSM_SERVER_NODE2_OPT);
    		//}
    		throw new UnsupportedOperationException();
    }
    
    public void retrieve(String path, File working, Progress progress, String optFilePath) throws Exception {
    	
    		logger.info("Retrieve command is " + "dsmc " + " retrieve " + working.getAbsolutePath() + " -optfile=" + optFilePath);
        ProcessBuilder pb = new ProcessBuilder("dsmc", "retrieve", working.getAbsolutePath(), "-optfile=" + optFilePath);
        Process p = pb.start();
        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Retrieval of " + working.getName() + " failed using " + optFilePath + ". ");
            InputStream error = p.getErrorStream();
            for (int i = 0; i < error.available(); i++) {
            		logger.info("" + error.read());
            }
            throw new Exception("Retrieval of " + working.getName() + " failed. ");
            
        }
        // FILL IN THE REST OF PROGRESS x dirs, x files, x bytes etc.
    }
    
    @Override
    public String store(String path, File working, Progress progress) throws Exception {
    		
    		
        // todo : monitor progress

        // Note: generate a uuid to be passed as the description. We should probably use the deposit UUID instead (do we need a specialised archive method)?
        // Just a thought - Does the filename contain the deposit uuid? Could we use that as the description?
        String randomUUIDString = UUID.randomUUID().toString();
        
        this.storeInTSMNode(path, working, progress, TivoliStorageManager.TSM_SERVER_NODE1_OPT, randomUUIDString);
        this.storeInTSMNode(path, working, progress, TivoliStorageManager.TSM_SERVER_NODE2_OPT, randomUUIDString);

        return randomUUIDString;
    }
    
    private String storeInTSMNode(String path, File working, Progress progress, String optFilePath, String description) throws Exception {
		
        // check we have enough space to store the data (is the file bagged and tarred atm or is the actual space going to be different?)
        // actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
        
        // The working file appears to be bagged and tarred when we get here
		// in the local version of this class the FileCopy class adds info to the progess object
		// I don't think we need to use the patch at all in this version 
        logger.info("Store command is " + "dsmc" + " archive " + working.getAbsolutePath() +  " -description=" + description + " -optfile=" + optFilePath);
        ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + optFilePath);

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
            logger.info(p.getErrorStream().toString());
            logger.info(p.getOutputStream().toString());
            throw new Exception("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
        }

        return description;
    }
    


    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
}
