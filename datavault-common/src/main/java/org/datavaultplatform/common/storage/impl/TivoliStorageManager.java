package org.datavaultplatform.common.storage.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.ProcessHelper;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
@Slf4j
@Getter
public class TivoliStorageManager extends Device implements ArchiveStore {

	public static final int DEFAULT_RETRY_TIME = 30;
	public static final int DEFAULT_MAX_RETRIES = 48;
	public static final String DSM_OPT_1 = "dsm1.opt";
	public static final String DSM_OPT_2 = "dsm2.opt";
	public static final boolean DEFAULT_REVERSE = false;
	public static final String DEFAULT_TEMP_PATH_PREFIX = "/tmp/datavault/temp";
	public static final Verify.Method VERIFICATION_METHOD = Verify.Method.COPY_BACK;
	public static final String PROPERTY_USER_DIR = "user.dir";
	public static final String ENV_PATH = "PATH";
	// default locations of TSM option files
	public static String DEFAULT_TSM_SERVER_NODE1_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm1.opt";
	public static String DEFAULT_TSM_SERVER_NODE2_OPT = "/opt/tivoli/tsm/client/ba/bin/dsm2.opt";
	
    private final int maxRetries;
	private final boolean reverse;
	private final int retryTimeMinutes;
	private final String tempPathPrefix;
	private final String tsmServerNodeOpt1;
	private final String tsmServerNodeOpt2;

	// Clock is mutable to allow us to change Clock during tests
	private Clock clock = Clock.systemDefaultZone();

    public TivoliStorageManager(String name, Map<String,String> config) {
		super(name, config);
		Assert.notNull(config, "The config map cannot be null");
		log.info("Config Size [{}]", config.size());
		config.forEach((key, value) -> log.info("Config value for [{}] is [{}]", key, value));
		this.retryTimeMinutes = lookup(PropNames.TSM_RETRY_TIME, Integer::parseInt, DEFAULT_RETRY_TIME);
		if (this.retryTimeMinutes < 0) {
			throw new IllegalArgumentException(String.format("The config property of %s[%s] cannot be less than 0", PropNames.TSM_RETRY_TIME, retryTimeMinutes));
		}
		this.maxRetries = lookup(PropNames.TSM_MAX_RETRIES, Integer::parseInt, DEFAULT_MAX_RETRIES);
		if (this.maxRetries < 1) {
			throw new IllegalArgumentException(String.format("The config property of %s[%s] cannot be less than 1", PropNames.TSM_MAX_RETRIES, maxRetries));
		}
		this.reverse = lookup(PropNames.TSM_REVERSE, Boolean::parseBoolean, DEFAULT_REVERSE);
		this.tempPathPrefix = lookup(PropNames.TEMP_DIR, Function.identity(), DEFAULT_TEMP_PATH_PREFIX);
		this.tsmServerNodeOpt1 = lookup(PropNames.OPTIONS_DIR, optionsDir -> Paths.get(optionsDir).resolve(DSM_OPT_1).toString(), DEFAULT_TSM_SERVER_NODE1_OPT);
		this.tsmServerNodeOpt2 = lookup(PropNames.OPTIONS_DIR, optionsDir -> Paths.get(optionsDir).resolve(DSM_OPT_2).toString(), DEFAULT_TSM_SERVER_NODE2_OPT);
		List<String> tempLocations = Arrays.asList(tsmServerNodeOpt1, tsmServerNodeOpt2);
		if (reverse) {
			Collections.reverse(tempLocations);
		}
		locations = Collections.unmodifiableList(tempLocations);
		this.multipleCopies = true;
		this.depositIdStorageKey = true;
		log.info("{}", this);
    }

	
    @Override
    public long getUsableSpace() throws Exception {
    	long retVal = 0;

		ProcessHelper.ProcessInfo info = getProcessInfo("tsmGetUsableSpace","dsmc", "query", "filespace");

        if (info.wasFailure()) {
			String message = "Filespace output failed.";
			logProcessOutput(info, message);
            throw new Exception(message);
        }
        
        // TODO : need to parse the output to get the usable space value
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
    public void retrieve(String depositId, File target, Progress progress, String optFilePath) throws Exception {
		Assert.isTrue(target != null, "the target cannot be null");
		Path depositDirectoryPath = getDepositDirectoryPath(depositId);
		
		// this is the name of the file within TSM
		Path tsmFilePath = depositDirectoryPath.resolve(target.getName());

		// Paths are better to deal with than Files
		Path targetPath = target.toPath().toAbsolutePath();

		String timestampedDirName = SftpUtils.getTimestampedDirectoryName(clock);
		Path retrieveToParentPath = Paths.get(this.tempPathPrefix).resolve(timestampedDirName);
		Files.createDirectories(retrieveToParentPath);
		
		// This is where we will place the retrieved TSM file on the local disk
		Path retrieveToPath =  retrieveToParentPath.resolve(target.getName());
		
		boolean retrieved = false;
    	for (int r = 0; r < maxRetries && !retrieved; r++) {
			log.info("retrieve [{}] attempt[{}/{}]", tsmFilePath, r+1, maxRetries);
	        ProcessHelper.ProcessInfo info = getProcessInfo("tsmRetrieve" ,
					"dsmc", "retrieve", tsmFilePath.toString(), retrieveToPath.toString(), "-description=" + depositId, "-optfile=" + optFilePath, "-replace=true");
			String attemptCtx = String.format("attempt[%s/%s]", r+1, maxRetries);
	        if (info.wasFailure()) {
				String errMsg = String.format("Retrieval of [%s/%s] failed using location[%s]%s" , depositId, target.getName(), optFilePath, attemptCtx);
				logProcessOutput(info, errMsg);
				boolean lastAttempt = r == (maxRetries -1);
	            if (lastAttempt) {
					throw new Exception(errMsg);
				}
	            log.info("{} Retrying in {} mins", errMsg, retryTimeMinutes);
	            TimeUnit.MINUTES.sleep(retryTimeMinutes);
	        } else {
		        if (Files.exists(retrieveToPath)) {
					
					log.info("Moving from retrieveTargetPath[{}] to targetPath[{}]", tsmFilePath, targetPath);

					Path targetPathDir = targetPath.getParent();
					if (targetPathDir != null) {
						Files.createDirectories(targetPathDir);
					}
					
					Files.move(retrieveToPath, targetPath, REPLACE_EXISTING, ATOMIC_MOVE);

					retrieved = true;
					String msg = String.format("Retrieval of [%s/%s] succeeded using location[%s]%s", depositId, target.getName(), optFilePath, attemptCtx);
					log.info(msg);
					
					// TODO : retrieveToPath should not exist if we've just moved it ?
					Files.deleteIfExists(retrieveToPath);
		        } else {
					String msg = String.format("The file [%s] does not exist after retrieved from TSM.", retrieveToPath);
					throw new Exception(msg);
				}
	        }
    	}
    }
	
    @Override
    public String store(String depositId, File source, Progress progress) throws Exception {
		Assert.isTrue(source != null, "The source cannot be null");
		Path sourcePath = source.toPath().toAbsolutePath();
		if (!Files.exists(sourcePath)) {
			throw new Exception(String.format("The source [%s] does not exist", sourcePath));
		}
		Path depositDirectoryPath = getDepositDirectoryPath(depositId);
		Path tsmFilePath = depositDirectoryPath.resolve(source.getName());

		log.info("Copying from sourcePath[{}] to tsmFilePath[{}]", sourcePath, tsmFilePath);
		Files.copy(sourcePath, tsmFilePath, REPLACE_EXISTING);

		TaskExecutor<String> executor = new TaskExecutor<>(2, "storeOnTSM");
		
		TSMTracker loc1 = getTSMTracker(tsmServerNodeOpt1, tsmFilePath.toFile(), progress, depositId, maxRetries, retryTimeMinutes);
		TSMTracker loc2 = getTSMTracker(tsmServerNodeOpt2, tsmFilePath.toFile(), progress, depositId, maxRetries, retryTimeMinutes);

		executor.add(loc1);
		executor.add(loc2);
		
		executor.execute(result -> log.info("storeOnTSM result [{}]", result));

        if (Files.exists(tsmFilePath)) {
        	Files.deleteIfExists(tsmFilePath);
			// TODO : Don't delete the deposit directory : there might be more chunks of this deposit using the same deposit directory
        	// Files.delete(destinationDirPath);
        }
        return depositId;
    }

    @Override
    public Verify.Method getVerifyMethod() {
        return VERIFICATION_METHOD;
    }
    
    @Override
    public void delete(String depositId, File working, Progress progress, String optFilePath) throws Exception {
		Path depositDirectoryPath = getDepositDirectoryPath(depositId);
		Path tsmFilePath = depositDirectoryPath.resolve(working.getName());
		log.info("Delete [{}]",tsmFilePath);
		ProcessHelper.ProcessInfo info = getProcessInfo("tsmDelete", 
				"dsmc", "delete", "archive", tsmFilePath.toString(), "-noprompt", "-optfile=" + optFilePath);
		if (info.wasFailure()) {
			String errMessage = String.format("Delete of [%s] failed.", tsmFilePath);
			logProcessOutput(info, errMessage);
			throw new Exception(errMessage);
		} else {
			log.info("Delete of [{}] was Successful.", tsmFilePath);
		}
    }
	
	/*
	 * The TSM Tape Driver 'dsmc' executable should be on the Java PATH
	 */
	public static boolean checkTSMTapeDriver() {
		try {
			ProcessHelper.ProcessInfo info = CheckerUtils.getProcessInfo("tsmCheckTapeDriver", Duration.ofSeconds(5), "which", "dsmc");

			log.info("user.dir [{}]", System.getProperty(PROPERTY_USER_DIR));
			log.info("PB 'path' [{}]", new ProcessBuilder().environment().get(ENV_PATH));

			if (info.wasSuccess()) {
				// canonicalPath resolves relative paths against user.dir and removes . and ..
				String pOutput = info.getOutputMessages().get(0);
				Path canonicalPath = Paths.get(new File(pOutput).getCanonicalPath());
				log.info("'dsmc' - is found on PATH by 'which' at [{}]", canonicalPath);
				return true;
			} else {
				log.info("'dsmc' - is NOT found on PATH by 'which' {}", info.getErrorMessages());
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
	
	private <T> T lookup(String key, Function<String, T> parser, T defaultValue) {
		return lookup(config, key, parser, defaultValue);
	}

	protected static <T> T lookup(Map<String,String> config, String key, Function<String, T> parser, T defaultValue) {
		if (config.containsKey(key)) {
			try {
				return parser.apply(config.get(key));
			} catch (RuntimeException ex) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	@Override
	public String toString () {
		return new ToStringBuilder(this).
				append("name", name).
				append("maxRetries", maxRetries).
				append("retryTimeMinutes", retryTimeMinutes).
				append("tempPathPrefix", tempPathPrefix).
				append("tsmServerNodeOpt1", tsmServerNodeOpt1).
				append("tsmServerNodeOpt2", tsmServerNodeOpt2).
				append("reverse", reverse).
				toString();
	}

	/**
	 * Only a single thread can be looking to create a deposit directory
	 * @param depositId the id of the deposit
	 * @return the path to the deposit directory
	 * @throws IOException if there's a problem
	 */
	private synchronized Path getDepositDirectoryPath(String depositId) throws IOException {
		Path prefixPath = Paths.get(tempPathPrefix);
		Path destinationDirectoryPath = prefixPath.resolve(depositId);
		if (!Files.exists(destinationDirectoryPath)) {
			Files.createDirectories(destinationDirectoryPath);
		}
		return destinationDirectoryPath;
	}
	
	static void logProcessOutput(ProcessHelper.ProcessInfo info, String errMessage)  {
		log.error(errMessage);
		info.getErrorMessages().forEach( error -> log.error("stderr [{}]", error));
		info.getOutputMessages().forEach( msg -> log.error("stdout [{}]", msg));
	}

	/*
	 * This allows creation of ProcessInfo to be faked during unit tests.
	 */
	public static class CheckerUtils {
		public static ProcessHelper.ProcessInfo getProcessInfo(String desc, Duration duration, String... commands) throws Exception {
			return new ProcessHelper(desc, duration, commands).execute();
		}
	}
	
	/*
	 * This allows creation of ProcessInfo to be faked during unit tests.
	 */
	protected ProcessHelper.ProcessInfo getProcessInfo(String desc, String... commands) throws Exception {
		return new ProcessHelper(desc, commands).execute();
	}

	/*
	 * This allows creation of TSMTracker to be faked during unit tests.
	 */
	protected TSMTracker getTSMTracker(String location, File tsmFile, Progress progress, String depositId, int maxRetries, int retryTimeMinutes) {
		return new TSMTracker(location, tsmFile, progress, depositId, maxRetries, retryTimeMinutes);
	}

	public void setClock(Clock clock) {
		Assert.notNull(clock, "The clock cannot be null");
		this.clock = clock;
	}
}


