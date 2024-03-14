package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.RestoreObjectsDetails;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.*;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import org.apache.commons.io.FileUtils;
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

To be clean despite being named OracleObjecStorageClassic it uses OCI not OCC
 */
public class OracleObjectStorageClassic extends Device implements ArchiveStore {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static final String DEFAULT_CONTAINER_NAME = "datavault-container-edina";
	public final Verify.Method verificationMethod = Verify.Method.CLOUD;
	private ObjectStorage client = null;
	private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/.oci/config";
	private static final String PROFILE = "DEFAULT";
	private static final int defaultRetryTime = 30;
	private static final int defaultMaxRetries = 48; // 24 hours if retry time is 30 minutes
	private static int retryTime = OracleObjectStorageClassic.defaultRetryTime;
	private static int maxRetries = OracleObjectStorageClassic.defaultMaxRetries;
	private static final String restoredKey = "Restored";
	private static String nameSpaceName = "testNameSpace";
	private static String bucketName = "testBucketName";

	public OracleObjectStorageClassic(String name, Map<String, String> config) {
		super(name, config);
		super.depositIdStorageKey = true;
		String retryKey = "occRetryTime";
		String maxKey = "occMaxRetries";
		String nameSpace = "ociNameSpace";
		String bucketName = "ociBucketName";

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

		if (config.containsKey(nameSpace)){
			LOGGER.debug("Got namespace config " + config.get(nameSpace));
			OracleObjectStorageClassic.nameSpaceName = config.get(nameSpace);
		}

		if (config.containsKey(bucketName)){
			LOGGER.debug("Got bucketName config" + config.get(bucketName));
			OracleObjectStorageClassic.bucketName = config.get(bucketName);
		}
	}

	@Override
    public Verify.Method getVerifyMethod() {
        return this.verificationMethod;
    }

	@Override
	public long getUsableSpace() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void retrieve(String depositId, File working, Progress progress) throws Exception {
		RestoreObjectsDetails restoreObjectDetails = RestoreObjectsDetails.builder()
				.objectName(depositId)
				.hours(24)
				.build();

		RestoreObjectsRequest restoreObjectsRequest = RestoreObjectsRequest.builder()
				.namespaceName(OracleObjectStorageClassic.nameSpaceName)
				.bucketName(OracleObjectStorageClassic.bucketName)
				.restoreObjectsDetails(restoreObjectDetails)
				.build();

		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
				.namespaceName(OracleObjectStorageClassic.nameSpaceName)
				.bucketName(OracleObjectStorageClassic.bucketName)
				.objectName(depositId)
				.build();

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.namespaceName(OracleObjectStorageClassic.nameSpaceName)
				.bucketName(OracleObjectStorageClassic.bucketName)
				.objectName(depositId)
				.build();

		for (int r = 0; r < OracleObjectStorageClassic.maxRetries; r++) {
			try {
				// create new client / manager each time so we can update the config
				// while in the holding pattern
				this.client = new ObjectStorageClient(getAuthDetailsProvider());
				// ask for the object to be restored
				this.client.restoreObjects(restoreObjectsRequest);

				// check if it has been restored
				//Boolean restored = false;
				int attemptCount = 0;

				while (true) {
					// retry for two hours (restores should be approx 1 hr)
					if (attemptCount > (60 / OracleObjectStorageClassic.retryTime) * 2) {
						throw new Exception("Restore failed");
					}
					HeadObjectResponse getHeadObjectResponse = this.client.headObject(headObjectRequest);
					LOGGER.debug("Object status is: " + getHeadObjectResponse.getArchivalState());
					if (getHeadObjectResponse.getArchivalState().getValue().equals(OracleObjectStorageClassic.restoredKey)) {
						break;
					}
					attemptCount++;
					TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
				}
				// once restored get it
				GetObjectResponse getObjectResponse = client.getObject(getObjectRequest);
				FileUtils.copyInputStreamToFile(getObjectResponse.getInputStream(), working);
				LOGGER.info("Oracle response:" + getObjectResponse.toString());
				break;
			} catch (Exception e) {
				LOGGER.error("Retrieve failed. " + "Retrying in " + OracleObjectStorageClassic.retryTime + " mins " + e.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries - 1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			}
		}

		
	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		for (int r = 0; r < OracleObjectStorageClassic.maxRetries; r++) {
			try {
				// create new client / manager each time so we can update the config
				// while in the holding pattern

				UploadManager uploadManager = this.constructUploadManager(getAuthDetailsProvider());

				UploadManager.UploadRequest uploadDetails = this.constructUploadRequest(depositId, working);
				UploadManager.UploadResponse response = uploadManager.upload(uploadDetails);
				LOGGER.info("Oracle response:" + response.toString());
				break;
			} catch (Exception e) {
				LOGGER.error("Upload failed. " + "Retrying in " + OracleObjectStorageClassic.retryTime + " mins " + e.getMessage());
				if (r == (OracleObjectStorageClassic.maxRetries - 1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageClassic.retryTime);
			}
		}
		
		return depositId;
	}

	@Override
	public void delete(String path, File working, Progress progress) {
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

		try {
			this.client = new ObjectStorageClient(getAuthDetailsProvider());
			DeleteObjectRequest request =
					DeleteObjectRequest.builder()
							.bucketName(OracleObjectStorageClassic.bucketName)
							.namespaceName(OracleObjectStorageClassic.nameSpaceName)
							.objectName(path)
							.build();
			this.client.deleteObject(request);
            LOGGER.info("Delete Successful from Oracle Cloud Storage");
		} catch (Exception e) {
			LOGGER.error("Object does not exists in Oracle Cloud Storage " + e.getMessage());
		}
	}

	/*private String getContainerName() throws Exception {
		Properties prop = this.getProperties();
		String contName = prop.getProperty(OracleObjectStorageClassic.CONTAINER_NAME);
		return (contName != null) ? contName : OracleObjectStorageClassic.DEFAULT_CONTAINER_NAME;
	}*/

	private static AuthenticationDetailsProvider getAuthDetailsProvider() throws Exception {
		ConfigFileReader.ConfigFile config = getProperties();
		AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);
		if (provider == null) {
			LOGGER.debug("Failed to get provider");
			throw new Exception("Failed to get provider");
		}
		//logger.debug("TenantId '" + provider.getTenantId() + "'");
		//logger.debug("UserId '" + provider.getUserId() + "'");
		//logger.debug("Fingerprint '" + provider.getFingerprint() + "'");
		//logger.debug("KeyId '" + provider.getKeyId() + "'");
		//logger.debug("PrivateKey '" + provider.getPrivateKey() + "'");
		//logger.debug("Get AuthDetailsProvider End");
		return provider;
	}

	private static ConfigFileReader.ConfigFile getProperties() throws Exception {
		ConfigFileReader.ConfigFile retVal =
				ConfigFileReader.parse(OracleObjectStorageClassic.CONFIG_FILE_PATH, OracleObjectStorageClassic.PROFILE);
		if (retVal == null) {
			LOGGER.debug("Problem getting the Oracle config");
			throw new Exception("Oracle Config is null");
		}
		
		return retVal;
	}

	private UploadManager constructUploadManager(AuthenticationDetailsProvider provider) {
		this.client = new ObjectStorageClient(provider);

		UploadConfiguration uploadConfiguration =
				UploadConfiguration.builder()
						.allowMultipartUploads(true)
						.allowParallelUploads(true)
						.build();

		UploadManager uploadManager = new UploadManager(this.client, uploadConfiguration);
		return uploadManager;
	}

	private UploadManager.UploadRequest constructUploadRequest(String depositId, File working) {

		Map<String, String> metadata = null;
		String contentType = null;
		String contentEncoding = null;
		String contentLanguage = null;

		PutObjectRequest request =
				PutObjectRequest.builder()
						.bucketName(OracleObjectStorageClassic.bucketName)
						.namespaceName(OracleObjectStorageClassic.nameSpaceName)
						.objectName(depositId)
						.contentType(contentType)
						.contentLanguage(contentLanguage)
						.contentEncoding(contentEncoding)
						.opcMeta(metadata)
						.build();

		UploadManager.UploadRequest uploadDetails =
				UploadManager.UploadRequest.builder(working).allowOverwrite(true).build(request);
		return uploadDetails;
	}

	public static boolean checkConfig() {
		try {
			getAuthDetailsProvider();
			return true;
		} catch (Exception ex) {
			LOGGER.warn("Problem getting Oracle Config from[{}]", CONFIG_FILE_PATH, ex);
			return false;
		}
	}
	@Override
	public Logger getLogger() {
		return LOGGER;
	}
}
