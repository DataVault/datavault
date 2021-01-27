package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.util.Map;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
This class has been upgraded to use the Gen 2 Oracle Object Storage rather than classic.
For reasons it was easier to do this than add a new Gen 2 plugin due to there currently
being no mechanism for changing the plugins of a deployed instance.
 */
public class OracleObjectStorageClassic extends Device implements ArchiveStore {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static String DEFAULT_CONTAINER_NAME = "datavault-container-edina";
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private ObjectStorage client = null;
	private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/.oci/config";
	private static final String PROFILE = "DEFAULT";
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
	private final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
	private final ConfigFileAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

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
			break;
			/*try {
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
			}*/
		}
		
	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		for (int r = 0; r < OracleObjectStorageClassic.maxRetries; r++) {
			logger.info("Oracle Gen 2 Store method start 1");

			Map<String, String> metadata = null;
			String contentType = null;
			String contentEncoding = null;
			String contentLanguage = null;

			logger.debug("TenantId '" + this.provider.getTenantId() + "'");
			logger.debug("UserId '" + this.provider.getUserId() + "'");
			logger.debug("Fingerprint '" + this.provider.getFingerprint() + "'");
			logger.debug("KeyId '" + this.provider.getKeyId() + "'");
			logger.debug("PrivateKey '" + this.provider.getPrivateKey() + "'");
			logger.debug("Get AuthDetailsProvider End");

			logger.debug("Creating client");
			ObjectStorage client = new ObjectStorageClient(this.provider);

			UploadConfiguration uploadConfiguration =
					UploadConfiguration.builder()
							.allowMultipartUploads(true)
							.allowParallelUploads(true)
							.build();

			logger.debug("Creating upload manager");

			UploadManager uploadManager = new UploadManager(client, uploadConfiguration);
			PutObjectRequest request =
					PutObjectRequest.builder()
							.bucketName("datavault-demo-container-san")
							.namespaceName("uoedatavault")
							.objectName(depositId)
							.contentType(contentType)
							.contentLanguage(contentLanguage)
							.contentEncoding(contentEncoding)
							.opcMeta(metadata)
							.build();

			UploadManager.UploadRequest uploadDetails =
					UploadManager.UploadRequest.builder(working).allowOverwrite(true).build(request);

			UploadManager.UploadResponse response = uploadManager.upload(uploadDetails);
			logger.info("Oracle response:" + response.toString());
			break;
			// TODO: Remember the retries stuff
			//this.client = new ObjectStorageAsyncClient(this.getAuthDetailsProvider());
			//AuthenticationDetailsProvider auth = this.getAuthDetailsProvider();
			//if (configFile == null) {
			//	logger.debug("Auth is null");
			//	throw new Exception("Auth is null");
			//} else {
			//	logger.debug("Auth is not null");
			//}

			//this.client = new ObjectStorageClient(auth);
			//if (this.client == null) {
			//	logger.debug("Client is null");
			//	throw new Exception("Client is null");
			//} else {
			//	logger.debug("Client is not null");
			//}
			//logger.debug("Oracle Gen 2 Store method start 2");
			//this.client.setRegion(Region.UK_LONDON_1);
			//break;
			/*try {
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
			}*/
		}
		
		return depositId;
	}
	
	@Override
	public void delete(String path, File working, Progress progress) throws Exception {
		/*try {
			this.manager = FileTransferManager.getDefaultFileTransferManager(this.getTransferAuth());
			manager.deleteObject(this.getContainerName(), path);
            logger.info("Delete Successful from Oracle Cloud Storage");
		} catch (ObjectNotFound  ce) {
			logger.error("Object does not exists in Oracle Cloud Storage " + ce.getMessage());
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}*/
	}

	/*private String getContainerName() throws Exception {
		Properties prop = this.getProperties();
		String contName = prop.getProperty(OracleObjectStorageClassic.CONTAINER_NAME);
		return (contName != null) ? contName : OracleObjectStorageClassic.DEFAULT_CONTAINER_NAME;
	}*/

	/*private FileTransferAuth getTransferAuth() throws Exception {
		Properties prop = this.getProperties();
		FileTransferAuth retVal = new FileTransferAuth(
				prop.getProperty(OracleObjectStorageClassic.USER_NAME),
				prop.getProperty(OracleObjectStorageClassic.PASSWORD).toCharArray(),
				prop.getProperty(OracleObjectStorageClassic.SERVICE_NAME),
				prop.getProperty(OracleObjectStorageClassic.SERVICE_URL),
				prop.getProperty(OracleObjectStorageClassic.IDENTITY_DOMAIN)
		);
		return retVal;
	}*/

	/*private Properties getProperties() throws Exception {
		Properties retVal = new Properties();
		try (InputStream is = new FileInputStream(OracleObjectStorageClassic.PROPERTIES_FILE_PATH)) {
			retVal.load(is);
		} catch (Exception e) {
			logger.info("Failed to read Occ properties file.");
			throw e;
		}

		return retVal;
	}*/

	private AuthenticationDetailsProvider getAuthDetailsProvider() throws Exception {
		ConfigFileReader.ConfigFile config = this.getProperties();
		AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);
		if (provider == null) {
			logger.debug("Failed to get provider");
			throw new Exception("Failed to get provider");
		}
		logger.debug("TenantId '" + provider.getTenantId() + "'");
		logger.debug("UserId '" + provider.getUserId() + "'");
		logger.debug("Fingerprint '" + provider.getFingerprint() + "'");
		logger.debug("KeyId '" + provider.getKeyId() + "'");
		logger.debug("PrivateKey '" + provider.getPrivateKey() + "'");
		logger.debug("Get AuthDetailsProvider End");
		return provider;
	}

	private ConfigFileReader.ConfigFile getProperties() throws Exception {
		ConfigFileReader.ConfigFile retVal = ConfigFileReader.parse(OracleObjectStorageClassic.CONFIG_FILE_PATH, OracleObjectStorageClassic.PROFILE);
		if (retVal == null) {
			logger.debug("Problem getting the Oracle config");
			throw new Exception("Oracle Config is null");
		}
		
		return retVal;
	}
		
}
