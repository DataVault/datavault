package org.datavaultplatform.common.storage.impl;

import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TivoliStorageManager extends Device implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(TivoliStorageManager.class);
    // default locations of TSM option files
    public static String TSM_SERVER_NODE1_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm1.opt";
    public static String TSM_SERVER_NODE2_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm2.opt";
    public static String TEMP_PATH_PREFIX = "/tmp/datavault/temp/";

    public Verify.Method verificationMethod = Verify.Method.COPY_BACK;
    private int retryTime = 60;

    public TivoliStorageManager(String name, Map<String,String> config) throws Exception  {
        super(name, config);
        String optionsKey = "optionsDir";
    	String tempKey = "tempDir";
        // if we have non default options in datavault.properties use them
        if (config.containsKey(optionsKey)) {
        	String optionsDir = config.get(optionsKey);
        	TivoliStorageManager.TSM_SERVER_NODE1_OPT = optionsDir + "/dsm1.opt";
        	TivoliStorageManager.TSM_SERVER_NODE2_OPT = optionsDir + "/dsm2.opt";
        }
        if (config.containsKey(tempKey)) {
        	TivoliStorageManager.TEMP_PATH_PREFIX = config.get(tempKey);
        }
        locations = new ArrayList<String>();
        locations.add(TivoliStorageManager.TSM_SERVER_NODE1_OPT);
        locations.add(TivoliStorageManager.TSM_SERVER_NODE2_OPT);
        super.multipleCopies = true;
        super.depositIdStorageKey = true;
        for (String key : config.keySet()) {
        		logger.info("Config value for " + key + " is " + config.get(key));
        }
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
    		throw new UnsupportedOperationException();
    }
    
    @Override
    public void retrieve(String depositId, File working, Progress progress, String optFilePath) throws Exception {
    	
    	String fileDir = TivoliStorageManager.TEMP_PATH_PREFIX + "/" + depositId;
    	String filePath = fileDir + "/" + working.getName();
    	if (! Files.exists(Paths.get(fileDir))) {
    		Files.createDirectory(Paths.get(fileDir));
    	}
    	logger.info("Retrieve command is " + "dsmc " + " retrieve " + filePath + " -description=" + depositId + " -optfile=" + optFilePath + "-replace=true");
    	while (true) {
	        ProcessBuilder pb = new ProcessBuilder("dsmc", "retrieve", filePath, "-description=" + depositId, "-optfile=" + optFilePath, "-replace=true");
	        Process p = pb.start();
	        // This class is already running in its own thread so it can happily pause until finished.
	        p.waitFor();
	
	        if (p.exitValue() != 0) {
	            logger.info("Retrieval of " + working.getName() + " failed using " + optFilePath + ". ");
	            InputStream error = p.getErrorStream();
	            for (int i = 0; i < error.available(); i++) {
	            		logger.info("" + error.read());
	            }
	            //throw new Exception("Retrieval of " + working.getName() + " failed. ");
	            logger.info("Retrieval of " + working.getName() + " failed. Retrying in " + this.retryTime + " mins");
	            TimeUnit.MINUTES.sleep(this.retryTime);
	            
	        } else {
		        // FILL IN THE REST OF PROGRESS x dirs, x files, x bytes etc.
		        if (Files.exists(Paths.get(filePath))) {
		        	Files.move(Paths.get(filePath), Paths.get(working.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		        }
		        Files.delete(Paths.get(fileDir));
		        break;
	        }
    	}
    }
    
    @Override
    public String store(String depositId, File working, Progress progress) throws Exception {
    		
    		
        // todo : monitor progress

        // Note: generate a uuid to be passed as the description. We should probably use the deposit UUID instead (do we need a specialised archive method)?
        // Just a thought - Does the filename contain the deposit uuid? Could we use that as the description?
        //String randomUUIDString = UUID.randomUUID().toString();
    	String pathPrefix = TivoliStorageManager.TEMP_PATH_PREFIX;
    	Path sourcePath = Paths.get(working.getAbsolutePath());
    	Path destinationDir = Paths.get(pathPrefix + "/" + depositId);
    	Path destinationFile = Paths.get(pathPrefix + "/" + depositId + "/" + working.getName());
    	if (Files.exists(sourcePath)) {
    		logger.info("Moving from temp to deposit id");
    		Files.createDirectory(destinationDir);
    		Files.move(sourcePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    	}
    	
    	File tsmFile = new File(pathPrefix + "/" + depositId + "/" + working.getName());
        this.storeInTSMNode(tsmFile, progress, TivoliStorageManager.TSM_SERVER_NODE1_OPT, depositId);
        this.storeInTSMNode(tsmFile, progress, TivoliStorageManager.TSM_SERVER_NODE2_OPT, depositId);
        
        if (Files.exists(destinationFile)) {
        	logger.info("Moving from deposit id to temp");
        	Files.move(destinationFile, sourcePath, StandardCopyOption.REPLACE_EXISTING);
        	Files.delete(destinationDir);
        }
        return depositId;
    }
    
    private String storeInTSMNode(File working, Progress progress, String optFilePath, String description) throws Exception {
		
        // check we have enough space to store the data (is the file bagged and tarred atm or is the actual space going to be different?)
        // actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
        
        // The working file appears to be bagged and tarred when we get here
		// in the local version of this class the FileCopy class adds info to the progress object
		// I don't think we need to use the patch at all in this version
    	//File path = working.getAbsoluteFile().getParentFile();
        logger.info("Store command is " + "dsmc" + " archive " + working.getAbsolutePath() +  " -description=" + description + " -optfile=" + optFilePath);
        ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + optFilePath);
        //pb.directory(path);
        while (true) {
	        Process p = pb.start();
	
	        // This class is already running in its own thread so it can happily pause until finished.
	        p.waitFor();
	
	        if (p.exitValue() != 0) {
	            logger.info("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
	            InputStream error = p.getErrorStream();
	            if (error != null) {
	            	logger.info(IOUtils.toString(error, StandardCharsets.UTF_8));
	            }
	            InputStream output = p.getInputStream();
	            if (output != null) {
	            	logger.info(IOUtils.toString(output, StandardCharsets.UTF_8));
	            }
	            //throw new Exception("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
	            logger.info("Deposit of " + working.getName() + " using " + optFilePath + " failed.  Retrying in " + this.retryTime + " mins");
	            TimeUnit.MINUTES.sleep(this.retryTime);
	        } else {
	        	break;
	        }
        }

        return description;
    }
    


    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
}
