package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicReference;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.DownloadConfig;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.TransferState;
import oracle.cloudstorage.ftm.UploadConfig;
import oracle.cloudstorage.ftm.exception.ClientException;
import oracle.cloudstorage.ftm.exception.ObjectExists;
import oracle.cloudstorage.ftm.exception.ObjectNotFound;

public class OracleObjectStorageClassic extends Device implements ArchiveStore {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static String DEFAULT_CONTAINER_NAME = "datavault-container-edina";
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private static final String PROPERTIES_FILE_PATH = System.getProperty("user.home") + "/.occ/occ.properties";
	private FileTransferManager manager = null;
	private static String USER_NAME = "user-name";
	private static String PASSWORD = "password";
	private static String SERVICE_NAME = "service-name";
	private static String SERVICE_URL = "service-url";
	private static String IDENTITY_DOMAIN = "identity-domain";
	private static String CONTAINER_NAME = "container-name";
	private static int defaultRetryTime = 30;
	private static int defaultMaxRetries = 48; // 24 hours if retry time is 30 minutes
	private static int retryTime = OracleObjectStorageClassic.defaultRetryTime;
	private static int maxRetries = OracleObjectStorageClassic.defaultMaxRetries;
	
	/*
	 * Add local jars to mvn
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/ftm-api-2.4.2.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=ftm-api -Dversion=1.0 -Dpackaging=jar
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/low-level-api-core-1.14.19.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=low-level-api-core -Dversion=1.0 -Dpackaging=jar
	 * 
	 * javax.json-1.0.4.jar has been added to the common project pom file and when I tried to add the other jars from the sample code to the pom the validator said they were 
	 * already managed ( log4j-1.2.17.jar, slf4j-api-1.7.7.jar, slf4j-log4j12-1.7.7.jar) 
	 */
	public OracleObjectStorageClassic(String name, Map<String, String> config) throws Exception {
		super(name, config);
		super.depositIdStorageKey = true;
		String retryKey = "occRetryTime";
		String maxKey = "occMaxRetries";

		if (config.containsKey(retryKey)){
			try {
				OracleObjectStorageClassic.retryTime = Integer.parseInt(config.get(retryKey));
			} catch (NumberFormatException nfe) {
				OracleObjectStorageClassic.retryTime = OracleObjectStorageClassic.defaultRetryTime;
			}
		}
		if (config.containsKey(maxKey)){
			try {
				OracleObjectStorageClassic.maxRetries = Integer.parseInt(config.get(maxKey));
			} catch (NumberFormatException nfe) {
				OracleObjectStorageClassic.maxRetries = OracleObjectStorageClassic.defaultMaxRetries;
			}
		}
	}

	@Override
    public Verify.Method getVerifyMethod() {
        return this.verificationMethod;
    }

	@Override
	public long getUsableSpace() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void retrieve(String depositId, File working, Progress progress) throws Exception {
		for (int r = 0; r < OracleObjectStorageClassic.maxRetries; r++) {
			try {
				this.manager = FileTransferManager.getDefaultFileTransferManager(this.getTransferAuth());
				DownloadConfig downloadConfig = new DownloadConfig();
				String containerName = this.getContainerName();
	            TransferResult downloadResult = manager.download(downloadConfig, containerName, depositId, working);
	            logger.info("Task completed. State:" + downloadResult.toString());
	            TransferState ts = downloadResult.getState();
	            while (ts.equals(TransferState.RestoreInProgress)) {
	                    logger.info("Restore in progress. % completed: " + downloadResult.getRestoreCompletedPercentage());
	                    Thread.sleep(1 * 60 * 1000); // Wait for 1 mins.
	                    downloadResult = manager.download(downloadConfig, containerName, depositId, working);
	                    ts = downloadResult.getState();
	            }
	            logger.info("Download Result:" + downloadResult.toString());
	            break;
			} catch (ClientException ce) {
				logger.error("Download failed. " + ce.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries -1)) {
					throw ce;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} catch (Exception e) {
				logger.error("Download failed. " + e.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries -1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} finally {
				if (this.manager != null) {
					this.manager.shutdown();
				}
			}
		}
		
	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		for (int r = 0; r < OracleObjectStorageClassic.maxRetries; r++) {
			try {

//				TivoliStorageManager.TSMTracker loc1 = new TivoliStorageManager.TSMTracker();
//				loc1.setLocation("loc1");
//				Thread loc1Thread = new Thread(loc1);
//				final AtomicReference throwableReference = new AtomicReference<Throwable>();
//				loc1Thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//					public void uncaughtException(Thread t, Throwable e) {
//						throwableReference.set(e);
//					}
//				});
//				loc1Thread.start();
//				TivoliStorageManager.TSMTracker loc2 = new TivoliStorageManager.TSMTracker();
//				loc2.setLocation("loc2");
//				Thread loc2Thread = new Thread(loc2);
//				loc2Thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//					public void uncaughtException(Thread t, Throwable e) {
//						throwableReference.set(e);
//					}
//				});
//				loc2Thread.start();
//
//				while (loc1Thread.isAlive() && loc2Thread.isAlive()) {
//					TimeUnit.MINUTES.sleep(1);
//				}
//
//				Throwable throwable = (Throwable)throwableReference.get();
//				if (throwable != null) {
//					if (throwable instanceof Exception) {
//						throw (Exception) throwable;
//					} else if (throwable instanceof RuntimeException) {
//						throw (RuntimeException)throwable;
//					}
//				}

//				ExecutorService executor = Executors.newSingleThreadExecutor();
//				TivoliStorageManager.TSMTrackerToo loc1 = new TivoliStorageManager.TSMTrackerToo();
//				loc1.setLocation("loc1");
//				TivoliStorageManager.TSMTrackerToo loc2 = new TivoliStorageManager.TSMTrackerToo();
//				loc2.setLocation("loc2");
//				Future<Void> loc1Future = executor.submit(loc1);
//				Future<Void> loc2Future = executor.submit(loc2);
//
//				executor.shutdown();
//				try {
//					loc1Future.get();
//					loc2Future.get();
//					logger.info("loc1 result " + loc1.result);
//					logger.info("loc2 result " + loc2.result);
//				} catch (ExecutionException ee) {
//					Throwable cause = ee.getCause();
//					if (cause instanceof Exception) {
//						logger.info("Upload failed. " + cause.getMessage());
//						throw (Exception) cause;
//					}
//				}



				String containerName = this.getContainerName();
				this.manager = FileTransferManager.getDefaultFileTransferManager(this.getTransferAuth());
				UploadConfig uploadConfig = new UploadConfig();
				uploadConfig.setOverwrite(false);
				uploadConfig.setStorageClass(CloudStorageClass.Archive);
				logger.info("Uploading file " + working.getName() + " to container " + containerName + " as " + depositId);
				TransferResult uploadResult = this.manager.upload(uploadConfig, containerName, depositId, working);
				logger.info("Upload completed successfully.");
				logger.info("Upload result:" + uploadResult.toString());
				break;
			} catch (ObjectExists oe) {
				// if the object already exists we must be attempting to restart a job
				// so just ignore exception and carry on
				logger.info("Uploaded previously: skipping.");
				break;
			} catch (ClientException ce) {
				logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageClassic.retryTime + " mins " + ce.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries -1)) {
					throw ce;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} catch (Exception e) {
				logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageClassic.retryTime + " mins " + e.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries -1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} finally {
				if (this.manager != null) {
					this.manager.shutdown();
				}
			}
		}
		
		return depositId;
	}
	
	@Override
	public void delete(String path, File working, Progress progress) throws Exception {
		try {
			this.manager = FileTransferManager.getDefaultFileTransferManager(this.getTransferAuth());
			manager.deleteObject(this.getContainerName(), path);
            logger.info("Delete Successful from Oracle Cloud Storage");
		} catch (ObjectNotFound  ce) {
			logger.error("Object does not exists in Oracle Cloud Storage " + ce.getMessage());
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}
	}

	private String getContainerName() throws Exception {
		Properties prop = this.getProperties();
		String contName = prop.getProperty(OracleObjectStorageClassic.CONTAINER_NAME);
		return (contName != null) ? contName : OracleObjectStorageClassic.DEFAULT_CONTAINER_NAME;
	}

	private FileTransferAuth getTransferAuth() throws Exception {
		Properties prop = this.getProperties();
		FileTransferAuth retVal = new FileTransferAuth(
				prop.getProperty(OracleObjectStorageClassic.USER_NAME),
				prop.getProperty(OracleObjectStorageClassic.PASSWORD).toCharArray(),
				prop.getProperty(OracleObjectStorageClassic.SERVICE_NAME),
				prop.getProperty(OracleObjectStorageClassic.SERVICE_URL),
				prop.getProperty(OracleObjectStorageClassic.IDENTITY_DOMAIN)
		);
		return retVal;
	}

	private Properties getProperties() throws Exception {
		Properties retVal = new Properties();
		try (InputStream is = new FileInputStream(OracleObjectStorageClassic.PROPERTIES_FILE_PATH)) {
			retVal.load(is);
		} catch (Exception e) {
			logger.info("Failed to read Occ properties file.");
			throw e;
		}

		return retVal;
	}
		
}
