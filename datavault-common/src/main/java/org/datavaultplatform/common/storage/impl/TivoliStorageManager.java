package org.datavaultplatform.common.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;

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
import java.util.function.Function;

@Slf4j
public class TivoliStorageManager extends Device implements ArchiveStore {

	public static final String DEFAULT_TEMP_PATH_PREFIX = "/tmp/datavault/temp";
	public static final int DEFAULT_RETRY_TIME = 30;
	public static final int DEFAULT_MAX_RETRIES = 48; // 24 hours if retry time is 30 minutes
	private static final Verify.Method verificationMethod = Verify.Method.COPY_BACK;
	
    // default locations of TSM option files
    public static String DEFAULT_TSM_SERVER_NODE1_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm1.opt";
    public static String DEFAULT_TSM_SERVER_NODE2_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm2.opt";
	
    private final int retryTimeMins;
    private final int maxRetries;
	private final boolean reverse;
	private final String tempPathPrefix;
	private final String tsmServerNodeOpt1;
	private final String tsmServerNodeOpt2;

    public TivoliStorageManager(String name, Map<String,String> config) {
		super(name, config);
		this.retryTimeMins = lookup(PropNames.TSM_RETRY_TIME, Integer::parseInt, DEFAULT_RETRY_TIME);
		this.maxRetries = lookup(PropNames.TSM_MAX_RETRIES, Integer::parseInt, DEFAULT_MAX_RETRIES);
		this.reverse = lookup(PropNames.TSM_REVERSE, Boolean::parseBoolean, false);
		this.tempPathPrefix = lookup(PropNames.TEMP_DIR, Function.identity(), DEFAULT_TEMP_PATH_PREFIX);
		this.tsmServerNodeOpt1 = lookup(PropNames.OPTIONS_DIR, optionsDir -> optionsDir + "/dsm1.opt", DEFAULT_TSM_SERVER_NODE1_OPT);
		this.tsmServerNodeOpt2 = lookup(PropNames.OPTIONS_DIR, optionsDir -> optionsDir + "/dsm2.opt", DEFAULT_TSM_SERVER_NODE2_OPT);
		this.locations = new ArrayList<>();
		if (reverse) {
			locations.add(this.tsmServerNodeOpt2);
			locations.add(this.tsmServerNodeOpt1);
		} else {
			locations.add(this.tsmServerNodeOpt1);
			locations.add(this.tsmServerNodeOpt2);
		}
		super.multipleCopies = true;
		super.depositIdStorageKey = true;
		for (String key : config.keySet()) {
			log.info("Config value for " + key + " is " + config.get(key));
		}
    }

	private <T> T lookup(String key, Function<String, T> parser, T defaultValue) {
		T result = defaultValue;
		if (config.containsKey(key)) {
			try {
				result = parser.apply(config.get(key));
			} catch (RuntimeException ex) {
				result = defaultValue;
			}
		}
		return result;
	}
    
    @Override
    public long getUsableSpace() throws Exception {
    		long retVal = 0;

        ProcessBuilder pb = new ProcessBuilder("dsmc", "query", "filespace");

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            log.info("Filespace output failed.");
            log.info(p.getErrorStream().toString());
            log.info(p.getOutputStream().toString());
            throw new Exception("Filespace output failed.");
        }
        
        // need to parse the output to get the usable space value
        // this looks like it will be a bit of a pain
        // I suspect the format might be quite awkward
        // (need to wait till I can actually connect to a tsm before I can do this)

        return retVal;
    }
    
    @Override
    public void retrieve(String path, File working, Progress progress) {
    		throw new UnsupportedOperationException();
    }
    
    @Override
    public void retrieve(String depositId, File working, Progress progress, String optFilePath) throws Exception {
    	
    	String fileDir = tempPathPrefix + "/" + depositId;
    	String filePath = fileDir + "/" + working.getName();
    	if (! Files.exists(Paths.get(fileDir))) {
    		Files.createDirectory(Paths.get(fileDir));
    	}
    	log.info("Retrieve command is " + "dsmc " + " retrieve " + filePath + " -description=" + depositId + " -optfile=" + optFilePath + "-replace=true");
		boolean retrieved = false;
    	for (int r = 0; r < maxRetries && !retrieved; r++) {
	        ProcessBuilder pb = new ProcessBuilder("dsmc", "retrieve", filePath, "-description=" + depositId, "-optfile=" + optFilePath, "-replace=true");
	        Process p = pb.start();
	        // This class is already running in its own thread so it can happily pause until finished.
	        p.waitFor();
	
	        if (p.exitValue() != 0) {
	            log.info("Retrieval of " + working.getName() + " failed using " + optFilePath + ". ");
	            InputStream error = p.getErrorStream();
	            for (int i = 0; i < error.available(); i++) {
	            		log.info("" + error.read());
	            }
	            if (r == (maxRetries - 1)) {
					throw new Exception("Retrieval of " + working.getName() + " failed. ");
				}
	            log.info("Retrieval of " + working.getName() + " failed. Retrying in " + retryTimeMins + " mins");
	            TimeUnit.MINUTES.sleep(retryTimeMins);
	            
	        } else {
				retrieved = true;
		        // FILL IN THE REST OF PROGRESS x dirs, x files, x bytes etc.
		        if (Files.exists(Paths.get(filePath))) {
		        	Files.move(Paths.get(filePath), Paths.get(working.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		        }
		        Files.delete(Paths.get(fileDir));
	        }
    	}
    }
    
    @Override
    public String store(String depositId, File working, Progress progress) throws Exception {
    		
    		
        // todo : monitor progress

        // Note: generate a uuid to be passed as the description. We should probably use the deposit UUID instead (do we need a specialised archive method)?
        // Just a thought - Does the filename contain the deposit uuid? Could we use that as the description?
    	String pathPrefix = tempPathPrefix;
    	Path sourcePath = Paths.get(working.getAbsolutePath());
    	Path destinationDir = Paths.get(pathPrefix + "/" + depositId);
    	Path destinationFile = Paths.get(pathPrefix + "/" + depositId + "/" + working.getName());
    	if (Files.exists(sourcePath)) {
    		log.info("Moving from temp to deposit id");
    		if (! Files.exists(destinationDir)) {
				Files.createDirectory(destinationDir);
			}
			Files.copy(sourcePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    	}
    	File tsmFile = new File(pathPrefix + "/" + depositId + "/" + working.getName());
    	// thread for each node
		ExecutorService executor = Executors.newFixedThreadPool(2);
		TSMTracker loc1 = new TSMTracker(tsmServerNodeOpt1, tsmFile, progress, depositId, maxRetries, retryTimeMins);
		TSMTracker loc2 = new TSMTracker(tsmServerNodeOpt2, tsmFile, progress, depositId, maxRetries, retryTimeMins);

		Future<String> loc1Future = executor.submit(loc1);
		Future<String> loc2Future = executor.submit(loc2);
		executor.shutdown();
		try {
			String result1 = loc1Future.get();
			String result2 = loc2Future.get();
			log.info("loc1 result " + result1);
			log.info("loc2 result " + result2);
		} catch (ExecutionException ee) {
			Throwable cause = ee.getCause();
			if (cause instanceof Exception) {
				log.info("TSM upload failed. " + cause.getMessage());
				throw (Exception) cause;
			}
		}

        if (Files.exists(destinationFile)) {
        	log.info("Moving from deposit id to temp");
        	//Files.move(destinationFile, sourcePath, StandardCopyOption.REPLACE_EXISTING);
        	Files.delete(destinationFile);
        	Files.delete(destinationDir);
        }
        return depositId;
    }

	@Override
	public String store(String path, File working, Progress progress, String timeStampDirname) throws Exception {
		throw new UnsupportedOperationException();
	}

    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
    
    @Override
    public void delete(String depositId, File working, Progress progress, String optFilePath) throws Exception {
    	String fileDir = tempPathPrefix + "/" + depositId;
    	String filePath = fileDir + "/" + working.getName();
		//for (int r = 0; r < TivoliStorageManager.maxRetries; r++) {
		log.info("Delete command is " + "dsmc delete archive " + filePath +  " -noprompt -optfile=" + optFilePath);
		ProcessBuilder pb = new ProcessBuilder("dsmc", "delete", "archive", filePath, "-noprompt" , "-optfile=" + optFilePath);
		Process p = pb.start();
		p.waitFor();
	
		if (p.exitValue() != 0) {
			log.info("Delete of " + depositId + " failed.");
			InputStream error = p.getErrorStream();
			for (int i = 0; i < error.available(); i++) {
				log.info("" + error.read());
			}				
		} else {
			log.info("Delete of " + depositId + " is Successful.");
			//break;
		}
    }
	
	/*
	 * The TSM Tape Driver 'dsmc' executable should be on the Java PATH
	 */
	public static boolean checkTSMTapeDriver() {
		try {
			ProcessBuilder pb = new ProcessBuilder("which", "dsmc");

			log.info("user.dir [{}]", System.getProperty("user.dir"));
			log.info("PB 'path' [{}]", pb.environment().get("PATH"));

			Process process = pb.start();

			int status = process.waitFor(5, TimeUnit.SECONDS) ? process.exitValue() : -1;
			String pOutput = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).trim();
			String pError = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8).trim();
			if (status == 0) {
				// canonicalPath resolves relative paths against user.dir and removes . and ..
				Path canonicalPath = Paths.get(new File(pOutput).getCanonicalPath());
				log.info("'dsmc' - is found on PATH by 'which' at [{}]", canonicalPath);
				return true;
			} else {
				log.info("'dsmc' - is NOT found on PATH by 'which' {}", pError);
				return false;
			}
		} catch (Exception ex) {
			log.error("problem trying to detect 'dsmc'", ex);
			return false;
		}
	}

	@Override
	public Logger getLogger() {
		return log;
	}
}


