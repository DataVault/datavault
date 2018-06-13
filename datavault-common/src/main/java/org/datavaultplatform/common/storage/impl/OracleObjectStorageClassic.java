package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.Verify.Method;
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

public class OracleObjectStorageClassic extends Device implements ArchiveStore {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static String defaultContainerName = "datavault-test-container-edina";
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private static final String demoAccountPropertiesFilepath = "/tmp/occ.properties";
	private FileTransferAuth auth;
	private FileTransferManager manager = null;
	
	/*
	 * Add local jars to mvn
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/ftm-api-2.4.2.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=ftm-api -Dversion=1.0 -Dpackaging=jar
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/low-level-api-core-1.14.19.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=low-level-api-core -Dversion=1.0 -Dpackaging=jar
	 */
	public OracleObjectStorageClassic(String name, Map<String, String> config) throws Exception {
		super(name, config);
		super.depositIdStorageKey = true;
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(OracleObjectStorageClassic.demoAccountPropertiesFilepath)) {
            prop.load(is);
	    } catch (Exception e) {
	            logger.info("Failed to read demo account properties file.");
	            throw e;
	    }
		//logger.info("User name is '" + prop.getProperty("user-name") + "'");
		//logger.info("Service name is '" + prop.getProperty("service-name") + "'");
		//logger.info("Service url is '" + prop.getProperty("service-url") + "'");
		//logger.info("Identity domain is '" + prop.getProperty("identity-domain") + "'");
		this.auth = new FileTransferAuth(
				prop.getProperty("user-name"),
				prop.getProperty("password").toCharArray(),
				prop.getProperty("service-name"), 
				prop.getProperty("service-url"), 
				prop.getProperty("identity-domain")
		);
	}

	@Override
	public Method getVerifyMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getUsableSpace() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void retrieve(String depositId, File working, Progress progress) throws Exception {
		try {
			this.manager = FileTransferManager.getDefaultFileTransferManager(this.auth);
			DownloadConfig downloadConfig = new DownloadConfig();
            TransferResult downloadResult = manager.download(downloadConfig, OracleObjectStorageClassic.defaultContainerName, depositId, working);
            logger.info("Task completed. State:" + downloadResult.toString());
            TransferState ts = downloadResult.getState();
            while (ts.equals(TransferState.RestoreInProgress)) {
                    logger.info("Restore in progress. % completed: " + downloadResult.getRestoreCompletedPercentage());
                    Thread.sleep(1 * 60 * 1000); // Wait for 1 mins.
                    downloadResult = manager.download(downloadConfig, OracleObjectStorageClassic.defaultContainerName, depositId, working);
                    ts = downloadResult.getState();
            }
            logger.info("Download Result:" + downloadResult.toString());
		} catch (ClientException ce) {
			logger.error("Download failed. " + ce.getMessage());
			throw ce;
		} catch (Exception e) {
			logger.error("Upload failed. " + e.getMessage());
			throw e;
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}
		
	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {

		try {
			this.manager = FileTransferManager.getDefaultFileTransferManager(this.auth);
			UploadConfig uploadConfig = new UploadConfig();
			uploadConfig.setOverwrite(true);
			uploadConfig.setStorageClass(CloudStorageClass.Archive);
			logger.info("Uploading file " + working.getName() + " to container " + OracleObjectStorageClassic.defaultContainerName);
			TransferResult uploadResult = this.manager.upload(uploadConfig, OracleObjectStorageClassic.defaultContainerName, depositId, working);
			logger.info("Upload completed successfully.");
			logger.info("Upload result:" + uploadResult.toString());
		} catch (ClientException ce) {
			logger.error("Upload failed. " + ce.getMessage());
			throw ce;
		} catch (Exception e) {
			logger.error("Upload failed. " + e.getMessage());
			throw e;
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}
		
		return depositId;
	}
}
