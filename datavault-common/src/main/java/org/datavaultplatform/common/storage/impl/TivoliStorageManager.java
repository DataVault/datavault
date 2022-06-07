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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class TivoliStorageManager extends Device implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(TivoliStorageManager.class);
    // default locations of TSM option files
    public static String TSM_SERVER_NODE1_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm1.opt";
    public static String TSM_SERVER_NODE2_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm2.opt";
    public static String TEMP_PATH_PREFIX = "/tmp/datavault/temp/";

    public Verify.Method verificationMethod = Verify.Method.COPY_BACK;
    private static int defaultRetryTime = 30;
	private static int defaultMaxRetries = 48; // 24 hours if retry time is 30 minutes
    private static int retryTime = TivoliStorageManager.defaultRetryTime;
    private static int maxRetries = TivoliStorageManager.defaultMaxRetries;

    public TivoliStorageManager(String name, Map<String,String> config) throws Exception  {
        super(name, config);
        String optionsKey = "optionsDir";
    	String tempKey = "tempDir";
    	String retryKey = "tsmRetryTime";
    	String maxKey = "tsmMaxRetries";
        // if we have non default options in datavault.properties use them
        if (config.containsKey(optionsKey)) {
        	String optionsDir = config.get(optionsKey);
        	TivoliStorageManager.TSM_SERVER_NODE1_OPT = optionsDir + "/dsm1.opt";
        	TivoliStorageManager.TSM_SERVER_NODE2_OPT = optionsDir + "/dsm2.opt";
        }
        if (config.containsKey(tempKey)) {
        	TivoliStorageManager.TEMP_PATH_PREFIX = config.get(tempKey);
        }
        if (config.containsKey(retryKey)){
        	try {
				TivoliStorageManager.retryTime = Integer.parseInt(config.get(retryKey));
			} catch (NumberFormatException nfe) {
				TivoliStorageManager.retryTime = TivoliStorageManager.defaultRetryTime;
			}
		}
        if (config.containsKey(maxKey)) {
			try {
				TivoliStorageManager.maxRetries = Integer.parseInt(config.get(maxKey));
			} catch (NumberFormatException nfe) {
				TivoliStorageManager.maxRetries = TivoliStorageManager.defaultMaxRetries;
			}
		}
        locations = new ArrayList<>();
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
    	for (int r = 0; r < TivoliStorageManager.maxRetries; r++) {
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
	            if (r == (TivoliStorageManager.maxRetries - 1)) {
					throw new Exception("Retrieval of " + working.getName() + " failed. ");
				}
	            logger.info("Retrieval of " + working.getName() + " failed. Retrying in " + TivoliStorageManager.retryTime + " mins");
	            TimeUnit.MINUTES.sleep(TivoliStorageManager.retryTime);
	            
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
    		if (! Files.exists(destinationDir)) {
				Files.createDirectory(destinationDir);
			}
    		//Files.move(sourcePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(sourcePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    	}
    	File tsmFile = new File(pathPrefix + "/" + depositId + "/" + working.getName());
    	// thread for each node
		ExecutorService executor = Executors.newFixedThreadPool(2);
		TivoliStorageManager.TSMTracker loc1 = new TivoliStorageManager.TSMTracker();
		loc1.setLocation(TivoliStorageManager.TSM_SERVER_NODE1_OPT);
		loc1.setWorking(tsmFile);
		loc1.setProgress(progress);
		loc1.setDescription(depositId);
		TivoliStorageManager.TSMTracker loc2 = new TivoliStorageManager.TSMTracker();
		loc2.setLocation(TivoliStorageManager.TSM_SERVER_NODE2_OPT);
		loc2.setWorking(tsmFile);
		loc2.setProgress(progress);
		loc2.setDescription(depositId);

		Future<Void> loc1Future = executor.submit(loc1);
		Future<Void> loc2Future = executor.submit(loc2);
		executor.shutdown();
		try {
			loc1Future.get();
			loc2Future.get();
			logger.info("loc1 result " + loc1.result);
			logger.info("loc2 result " + loc2.result);
		} catch (ExecutionException ee) {
			Throwable cause = ee.getCause();
			if (cause instanceof Exception) {
				logger.info("TSM upload failed. " + cause.getMessage());
				throw (Exception) cause;
			}
		}


        //this.storeInTSMNode(tsmFile, progress, TivoliStorageManager.TSM_SERVER_NODE1_OPT, depositId);
        //this.storeInTSMNode(tsmFile, progress, TivoliStorageManager.TSM_SERVER_NODE2_OPT, depositId);
        
        if (Files.exists(destinationFile)) {
        	logger.info("Moving from deposit id to temp");
        	//Files.move(destinationFile, sourcePath, StandardCopyOption.REPLACE_EXISTING);
        	Files.delete(destinationFile);
        	Files.delete(destinationDir);
        }
        return depositId;
    }
    
//    private String storeInTSMNode(File working, Progress progress, String optFilePath, String description) throws Exception {
//
//        // check we have enough space to store the data (is the file bagged and tarred atm or is the actual space going to be different?)
//        // actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
//
//        // The working file appears to be bagged and tarred when we get here
//		// in the local version of this class the FileCopy class adds info to the progress object
//		// I don't think we need to use the patch at all in this version
//    	//File path = working.getAbsoluteFile().getParentFile();
//        logger.info("Store command is " + "dsmc" + " archive " + working.getAbsolutePath() +  " -description=" + description + " -optfile=" + optFilePath);
//        ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + optFilePath);
//        //pb.directory(path);
//		for (int r = 0; r < TivoliStorageManager.maxRetries; r++) {
//	        Process p = pb.start();
//
//	        // This class is already running in its own thread so it can happily pause until finished.
//	        p.waitFor();
//
//	        if (p.exitValue() != 0) {
//	            logger.info("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
//	            InputStream error = p.getErrorStream();
//	            if (error != null) {
//	            	logger.info(IOUtils.toString(error, StandardCharsets.UTF_8));
//	            }
//	            InputStream output = p.getInputStream();
//	            if (output != null) {
//	            	logger.info(IOUtils.toString(output, StandardCharsets.UTF_8));
//	            }
//	            if (r == (TivoliStorageManager.maxRetries -1)) {
//					throw new Exception("Deposit of " + working.getName() + " using " + optFilePath + " failed. ");
//				}
//	            logger.info("Deposit of " + working.getName() + " using " + optFilePath + " failed.  Retrying in " + TivoliStorageManager.retryTime + " mins");
//	            TimeUnit.MINUTES.sleep(TivoliStorageManager.retryTime);
//	        } else {
//	        	break;
//	        }
//        }
//        return description;
//    }
    


    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
    
    @Override
    public void delete(String depositId, File working, Progress progress, String optFilePath) throws Exception {
    	String fileDir = TivoliStorageManager.TEMP_PATH_PREFIX + "/" + depositId;
    	String filePath = fileDir + "/" + working.getName();
		//for (int r = 0; r < TivoliStorageManager.maxRetries; r++) {
		logger.info("Delete command is " + "dsmc delete archive " + filePath +  " -noprompt -optfile=" + optFilePath);
		ProcessBuilder pb = new ProcessBuilder("dsmc", "delete", "archive", filePath, "-noprompt" , "-optfile=" + optFilePath);
		Process p = pb.start();
		p.waitFor();
	
		if (p.exitValue() != 0) {
			logger.info("Delete of " + depositId + " failed.");
			InputStream error = p.getErrorStream();
			for (int i = 0; i < error.available(); i++) {
				logger.info("" + error.read());
			}
			//if (r == (TivoliStorageManager.maxRetries -1)) {
				//throw new Exception("Delete of " + depositId + " using " + optFilePath + " failed. ");
				//	logger.info("Delete of " + depositId + " failed after max retries.  Attempt to remove manually");
			//}
			//logger.info("Delete of " + depositId + " failed. Retrying in " + TivoliStorageManager.retryTime + " mins");
			//TimeUnit.MINUTES.sleep(TivoliStorageManager.retryTime);
	            
		} else {
			logger.info("Delete of " + depositId + " is Successful.");
			//break;
		}
    	//}
    }

//	protected static class TSMTracker implements Runnable {
//
//		private String location;
//
//		@Override
//		public void run() {
//			// do stuff
//			//String test = null;
//			logger.debug("Starting: " + this.getLocation());
//			//logger.debug("Force error: " + test.length());
//			//throw new Exception("Bad TSM stuff");
//		}
//
//
//		public String getLocation() {
//			return this.location;
//		}
//
//		public void setLocation(String location) {
//			this.location = location;
//		}
//	}

	protected static class TSMTracker implements Callable {

    	private String location;
    	private File working;
    	private Progress progress;
    	private String description;
    	public String result;


		@Override
		public Object call() throws Exception {
			// do stuff
			//String test = null;
			//logger.debug("Starting: " + this.getLocation());
			//logger.debug("Force error: " + test.length());
			//logger.debug("About to throw Exception");
			//throw new Exception("Bad TSM stuff");
			//result = this.getLocation() + " completed";

			// @TODO check params all set

			this.storeInTSMNode(this.getWorking(), this.getProgress(), this.getLocation(), this.getDescription());
			return result;
		}

		public String getLocation() {
			return this.location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		private String storeInTSMNode(File working, Progress progress, String location, String description) throws Exception {

			// check we have enough space to store the data (is the file bagged and tarred atm or is the actual space going to be different?)
			// actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check

			// The working file appears to be bagged and tarred when we get here
			// in the local version of this class the FileCopy class adds info to the progress object
			// I don't think we need to use the patch at all in this version
			//File path = working.getAbsoluteFile().getParentFile();
			logger.info("Store command is " + "dsmc" + " archive " + working.getAbsolutePath() +  " -description=" + description + " -optfile=" + location);
			ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + description, "-optfile=" + location);
			//pb.directory(path);
			for (int r = 0; r < TivoliStorageManager.maxRetries; r++) {
				Process p = pb.start();

				// This class is already running in its own thread so it can happily pause until finished.
				p.waitFor();

				if (p.exitValue() != 0) {
					logger.info("Deposit of " + working.getName() + " using " + location + " failed. ");
					InputStream error = p.getErrorStream();
					if (error != null) {
						logger.info(IOUtils.toString(error, StandardCharsets.UTF_8));
					}
					InputStream output = p.getInputStream();
					if (output != null) {
						logger.info(IOUtils.toString(output, StandardCharsets.UTF_8));
					}
					if (r == (TivoliStorageManager.maxRetries -1)) {
						throw new Exception("Deposit of " + working.getName() + " using " + location + " failed. ");
					}
					logger.info("Deposit of " + working.getName() + " using " + location + " failed.  Retrying in " + TivoliStorageManager.retryTime + " mins");
					TimeUnit.MINUTES.sleep(TivoliStorageManager.retryTime);
				} else {
					break;
				}
			}
			result = description;
			return this.result;
		}

		public File getWorking() {
			return this.working;
		}

		public void setWorking(File working) {
			this.working = working;
		}

		public Progress getProgress() {
			return this.progress;
		}

		public void setProgress(Progress progress) {
			this.progress = progress;
		}

		public String getDescription() {
			return this.description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}


