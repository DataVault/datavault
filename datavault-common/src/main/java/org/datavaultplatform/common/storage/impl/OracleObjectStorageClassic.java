package org.datavaultplatform.common.storage.impl;

		import com.oracle.bmc.ConfigFileReader;
		import com.oracle.bmc.Region;
		import com.oracle.bmc.auth.AuthenticationDetailsProvider;
		import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
		import com.oracle.bmc.objectstorage.ObjectStorage;
		import com.oracle.bmc.objectstorage.ObjectStorageAsync;
		import com.oracle.bmc.objectstorage.ObjectStorageAsyncClient;
		import com.oracle.bmc.objectstorage.ObjectStorageClient;
		import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
		import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
		import com.oracle.bmc.objectstorage.transfer.UploadManager;
		import com.oracle.bmc.responses.AsyncHandler;
		import org.datavaultplatform.common.io.Progress;
		import org.datavaultplatform.common.storage.ArchiveStore;
		import org.datavaultplatform.common.storage.Device;
		import org.datavaultplatform.common.storage.Verify;
		import org.slf4j.Logger;
		import org.slf4j.LoggerFactory;

		import java.io.File;
		import java.util.Map;
		import java.util.concurrent.CountDownLatch;
		import java.util.concurrent.ExecutionException;

public class OracleObjectStorageClassic extends Device implements ArchiveStore {

	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static String DEFAULT_CONTAINER_NAME = "datavault-container-edina";
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/.oci/config";
	private static final String PROFILE = "DEFAULT";
	//private FileTransferManager manager = null;
	private ObjectStorageAsync client = null;
	//private ObjectStorage client = null;
	private UploadManager manager = null;
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
	public void retrieve(String path, File working, Progress progress) throws Exception {
		// TODO: Remember the retry stuff
	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		// use async
		// I can see a lot of methods in the classic filetransfermanager that we might have used instead of store
		// storeMultipleFilesAsyn etc dunno if that is faster or will fit in with how things work
		// but might be better if we can do some or all the chunks at once for Oracle

		logger.info("Oracle Gen 2 Store method start 1");
		// TODO: Remember the retries stuff
		//this.client = new ObjectStorageAsyncClient(this.getAuthDetailsProvider());
		ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
		ConfigFileAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
		//AuthenticationDetailsProvider auth = this.getAuthDetailsProvider();
		if (provider == null) {
			logger.debug("Provider is null");
			throw new Exception("Provider is null");
		} else {
			logger.debug("Provider is not null");
		}

		logger.debug("TenantId '" + provider.getTenantId() + "'");
		logger.debug("UserId '" + provider.getUserId() + "'");
		logger.debug("Fingerprint '" + provider.getFingerprint() + "'");
		logger.debug("KeyId '" + provider.getKeyId() + "'");
		logger.debug("PrivateKey '" + provider.getPrivateKey() + "'");
		logger.debug("Passphrase '" + provider.getPassphraseCharacters() + "'");

		//this.client = new ObjectStorageClient(provider);
		//this.client = new ObjectStorageAsyncClient(provider);

		logger.debug("Initialised client");
		try {
			ObjectStorageAsync client = new ObjectStorageAsyncClient(provider);

			logger.debug("Initialised client");
			if (client == null) {
				logger.debug("Client is null");
				throw new Exception("Client is null");
			} else {
				logger.debug("Client is not null");
			}
			logger.info("Oracle Gen 2 Store method start 2");
			//this.client.setRegion(Region.UK_LONDON_1);
			/*client.setRegion(Region.UK_LONDON_1);
			GetNamespaceResponse namespaceResponse =
					client.getNamespace(GetNamespaceRequest.builder().build());
			String namespaceName = namespaceResponse.getValue();
			logger.info("Using namespace: " + namespaceName);*/


			ResponseHandler<GetNamespaceRequest, GetNamespaceResponse> namespaceHandler = new ResponseHandler<>();
			client.getNamespace(GetNamespaceRequest.builder().build(), namespaceHandler);
			GetNamespaceResponse namespaceResponse = namespaceHandler.waitForCompletion();
			String namespaceName = namespaceResponse.getValue();
			logger.info("Using namespace:" + namespaceName);
		} catch (Exception ee) {
			logger.debug("Caught Exception from ObjectStorage");
			throw ee;
		}

		/*String contentType = "";
		String contentLanguage = "";
		String contentEncoding = "";
		Map<String, String> metadata = new HashedMap();
		logger.info("Oracle Gen 2 Store method start 3");
		GetNamespaceResponse namespaceResponse = this.client.getNamespace(GetNamespaceRequest.builder().build());;

		String namespaceName = namespaceResponse.getValue();
		logger.info("Oracle Gen 2 Store method start 4");
		UploadConfiguration uploadConfiguration = UploadConfiguration.builder().allowMultipartUploads(true).allowParallelUploads(true).build();

		this.manager = new UploadManager(this.client, uploadConfiguration);
		logger.info("Oracle Gen 2 Store method mid 1");
		ConfigFileReader.ConfigFile config = this.getProperties();
		PutObjectRequest request =
				PutObjectRequest.builder()
						.bucketName("datavault-local-container")
						.namespaceName(namespaceName)
						.objectName(depositId)
						.contentType(contentType)
						.contentLanguage(contentLanguage)
						.contentEncoding(contentEncoding)
						.opcMeta(metadata)
						.build();
		logger.info("Oracle Gen 2 Store method mid 2");
		UploadManager.UploadRequest uploadDetails =
				UploadManager.UploadRequest.builder(working).allowOverwrite(false).build(request);
		logger.info("Oracle Gen 2 Store method mid 3");
		UploadManager.UploadResponse response = this.manager.upload(uploadDetails);
		logger.info("Upload completed successfully.");
		logger.info("Upload result:" + response.toString());

		*/
		logger.info("Oracle Gen 2 Store method finish");
		return depositId;
	}

	@Override
	public void delete(String path, File working, Progress progress) throws Exception {

	}

	private String getContainerName() throws Exception {
		//Properties prop = this.getProperties();
		//String contName = prop.getProperty(OracleObjectStorageGen2.CONTAINER_NAME);
		//return (contName != null) ? contName : OracleObjectStorageGen2.DEFAULT_CONTAINER_NAME;
		return null;
	}

	private AuthenticationDetailsProvider getAuthDetailsProvider() throws Exception {
//        Properties prop = this.getProperties();
//        FileTransferAuth retVal = new FileTransferAuth(
//                prop.getProperty(OracleObjectStorageGen2.USER_NAME),
//                prop.getProperty(OracleObjectStorageGen2.PASSWORD).toCharArray(),
//                prop.getProperty(OracleObjectStorageGen2.SERVICE_NAME),
//                prop.getProperty(OracleObjectStorageGen2.SERVICE_URL),
//                prop.getProperty(OracleObjectStorageGen2.IDENTITY_DOMAIN)
//        );
//        return retVal;
		logger.debug("Get AuthDetailsProvider Start");
		ConfigFileReader.ConfigFile config = this.getProperties();

		AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);
		if (provider == null) {
			logger.debug("Failed to get provider");
			throw new Exception("Failed to get provider");
		}
		logger.debug("Get AuthDetailsProvider End");
		return provider;
	}

	private ConfigFileReader.ConfigFile getProperties() throws Exception {
//        Properties retVal = new Properties();
//        try (InputStream is = new FileInputStream(OracleObjectStorageGen2.CONFIG_FILE_PATH)) {
//            retVal.load(is);
//        } catch (Exception e) {
//            logger.info("Failed to read Occ properties file.");
//            throw e;
//        }
//
//        return retVal;

		ConfigFileReader.ConfigFile retVal = ConfigFileReader.parse(OracleObjectStorageClassic.CONFIG_FILE_PATH, OracleObjectStorageClassic.PROFILE);
		if (retVal == null) {
			logger.debug("Problem getting the Oracle config");
			throw new Exception("Oracle Config is null");
		}

		logger.debug("Got the Oracle config");
		return retVal;
	}

	private static class ResponseHandler<IN, OUT> implements AsyncHandler<IN, OUT> {
		private OUT item;
		private Throwable failed = null;
		private CountDownLatch latch = new CountDownLatch(1);

		private OUT waitForCompletion() throws Exception {
			logger.debug("Waiting for completion");
			latch.await();
			if (failed != null) {
				if (failed instanceof Exception) {
					throw (Exception) failed;
				}
				throw (Error) failed;
			}
			logger.debug("Waited for completion");
			return item;
		}

		@Override
		public void onSuccess(IN request, OUT response) {
			item = response;
			latch.countDown();
		}

		@Override
		public void onError(IN request, Throwable error) {
			logger.debug("Error in RespnseHandler");
			failed = error;
			latch.countDown();
		}
	}
}

/*
public class OracleObjectStorageGen2 extends Device implements ArchiveStore {

	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageGen2.class);
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
	private static int retryTime = OracleObjectStorageGen2.defaultRetryTime;
	private static int maxRetries = OracleObjectStorageGen2.defaultMaxRetries;

	*/
/*
	 * Add local jars to mvn
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/ftm-api-2.4.2.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=ftm-api -Dversion=1.0 -Dpackaging=jar
	 * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/low-level-api-core-1.14.19.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=low-level-api-core -Dversion=1.0 -Dpackaging=jar
	 *
	 * javax.json-1.0.4.jar has been added to the common project pom file and when I tried to add the other jars from the sample code to the pom the validator said they were
	 * already managed ( log4j-1.2.17.jar, slf4j-api-1.7.7.jar, slf4j-log4j12-1.7.7.jar)
	 *//*

	public OracleObjectStorageGen2(String name, Map<String, String> config) throws Exception {
		super(name, config);
		super.depositIdStorageKey = true;
		String retryKey = "occRetryTime";
		String maxKey = "occMaxRetries";

		if (config.containsKey(retryKey)) {
			try {
				OracleObjectStorageGen2.retryTime = Integer.parseInt(config.get(retryKey));
			} catch (NumberFormatException nfe) {
				OracleObjectStorageGen2.retryTime = OracleObjectStorageGen2.defaultRetryTime;
			}
		}
		if (config.containsKey(maxKey)) {
			try {
				OracleObjectStorageGen2.maxRetries = Integer.parseInt(config.get(maxKey));
			} catch (NumberFormatException nfe) {
				OracleObjectStorageGen2.maxRetries = OracleObjectStorageGen2.defaultMaxRetries;
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
		for (int r = 0; r < OracleObjectStorageGen2.maxRetries; r++) {
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
				if (r == (OracleObjectStorageGen2.maxRetries - 1)) {
					throw ce;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
			} catch (Exception e) {
				logger.error("Download failed. " + e.getMessage());
				if (r == (OracleObjectStorageGen2.maxRetries - 1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
			} finally {
				if (this.manager != null) {
					this.manager.shutdown();
				}
			}
		}

	}

	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		for (int r = 0; r < OracleObjectStorageGen2.maxRetries; r++) {
			try {
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
				logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageGen2.retryTime + " mins " + ce.getMessage());
				if (r == (OracleObjectStorageGen2.maxRetries - 1)) {
					throw ce;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
			} catch (Exception e) {
				logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageGen2.retryTime + " mins " + e.getMessage());
				if (r == (OracleObjectStorageGen2.maxRetries - 1)) {
					throw e;
				}
				TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
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
		} catch (ObjectNotFound ce) {
			logger.error("Object does not exists in Oracle Cloud Storage " + ce.getMessage());
		} finally {
			if (this.manager != null) {
				this.manager.shutdown();
			}
		}
	}

	private String getContainerName() throws Exception {
		Properties prop = this.getProperties();
		String contName = prop.getProperty(OracleObjectStorageGen2.CONTAINER_NAME);
		return (contName != null) ? contName : OracleObjectStorageGen2.DEFAULT_CONTAINER_NAME;
	}

	private FileTransferAuth getTransferAuth() throws Exception {
		Properties prop = this.getProperties();
		FileTransferAuth retVal = new FileTransferAuth(
				prop.getProperty(OracleObjectStorageGen2.USER_NAME),
				prop.getProperty(OracleObjectStorageGen2.PASSWORD).toCharArray(),
				prop.getProperty(OracleObjectStorageGen2.SERVICE_NAME),
				prop.getProperty(OracleObjectStorageGen2.SERVICE_URL),
				prop.getProperty(OracleObjectStorageGen2.IDENTITY_DOMAIN)
		);
		return retVal;
	}

	private Properties getProperties() throws Exception {
		Properties retVal = new Properties();
		try (InputStream is = new FileInputStream(OracleObjectStorageGen2.PROPERTIES_FILE_PATH)) {
			retVal.load(is);
		} catch (Exception e) {
			logger.info("Failed to read Occ properties file.");
			throw e;
		}

		return retVal;
	}

}*/
