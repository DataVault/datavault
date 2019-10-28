package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
	private FileTransferAuth auth;
	private FileTransferManager manager = null;
	private String containerName;
	private static String USER_NAME = "user-name";
	private static String PASSWORD = "password";
	private static String SERVICE_NAME = "service-name";
	private static String SERVICE_URL = "service-url";
	private static String IDENTITY_DOMAIN = "identity-domain";
	private static String CONTAINER_NAME = "container-name";
	private static int defaultRetryTime = 30;
	private static int retryTime = OracleObjectStorageClassic.defaultRetryTime;
	
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
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(OracleObjectStorageClassic.PROPERTIES_FILE_PATH)) {
            prop.load(is);
	    } catch (Exception e) {
	            logger.info("Failed to read demo account properties file.");
	            throw e;
	    }
		this.auth = new FileTransferAuth(
				prop.getProperty(OracleObjectStorageClassic.USER_NAME),
				prop.getProperty(OracleObjectStorageClassic.PASSWORD).toCharArray(),
				prop.getProperty(OracleObjectStorageClassic.SERVICE_NAME), 
				prop.getProperty(OracleObjectStorageClassic.SERVICE_URL), 
				prop.getProperty(OracleObjectStorageClassic.IDENTITY_DOMAIN)
		);
		String contName = prop.getProperty(OracleObjectStorageClassic.CONTAINER_NAME);
		this.containerName = contName != null ? contName : OracleObjectStorageClassic.DEFAULT_CONTAINER_NAME;
		if (config.containsKey(retryKey)){
			try {
				OracleObjectStorageClassic.retryTime = Integer.parseInt(config.get(retryKey));
			} catch (NumberFormatException nfe) {
				retryTime = OracleObjectStorageClassic.defaultRetryTime;
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
		while (true) {
			try {
				this.manager = FileTransferManager.getDefaultFileTransferManager(this.auth);
				DownloadConfig downloadConfig = new DownloadConfig();
	            TransferResult downloadResult = manager.download(downloadConfig, this.containerName, depositId, working);
	            logger.info("Task completed. State:" + downloadResult.toString());
	            TransferState ts = downloadResult.getState();
	            while (ts.equals(TransferState.RestoreInProgress)) {
	                    logger.info("Restore in progress. % completed: " + downloadResult.getRestoreCompletedPercentage());
	                    Thread.sleep(1 * 60 * 1000); // Wait for 1 mins.
	                    downloadResult = manager.download(downloadConfig, this.containerName, depositId, working);
	                    ts = downloadResult.getState();
	            }
	            logger.info("Download Result:" + downloadResult.toString());
	            break;
			} catch (ClientException ce) {
				logger.error("Download failed. " + ce.getMessage());
				//throw ce;
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} catch (Exception e) {
				logger.error("Download failed. " + e.getMessage());
				//throw e;
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
		while (true) {
			try {
				this.manager = FileTransferManager.getDefaultFileTransferManager(this.auth);
				UploadConfig uploadConfig = new UploadConfig();
				uploadConfig.setOverwrite(false);
				uploadConfig.setStorageClass(CloudStorageClass.Archive);
				logger.info("Uploading file " + working.getName() + " to container " + this.containerName + " as " + depositId);
				TransferResult uploadResult = this.manager.upload(uploadConfig, this.containerName, depositId, working);
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
				//throw ce;
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			} catch (Exception e) {
				logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageClassic.retryTime + " mins " + e.getMessage());
				//throw e;
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
			this.manager = FileTransferManager.getDefaultFileTransferManager(this.auth);
			manager.deleteObject(this.containerName, path);
            logger.info("Delete Successful from Oracle Cloud Storage");
		} catch (ObjectNotFound  ce) {
			logger.error("Object does not exists in Oracle Cloud Storage " + ce.getMessage());
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}
	}
		
}
