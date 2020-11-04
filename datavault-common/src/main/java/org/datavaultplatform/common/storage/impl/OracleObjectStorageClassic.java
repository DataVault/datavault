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
//import com.sun.tools.internal.jxc.ConfigReader;
		import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
		import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
		import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
		import com.oracle.bmc.objectstorage.transfer.UploadManager;
		import com.oracle.bmc.responses.AsyncHandler;
		import oracle.cloudstorage.ftm.*;
		import oracle.cloudstorage.ftm.exception.ClientException;
		import oracle.cloudstorage.ftm.exception.ObjectExists;
		import oracle.cloudstorage.ftm.exception.ObjectNotFound;
		import org.apache.commons.collections.map.HashedMap;
		import org.datavaultplatform.common.io.Progress;
		import org.datavaultplatform.common.storage.ArchiveStore;
		import org.datavaultplatform.common.storage.Device;
		import org.datavaultplatform.common.storage.Verify;
		import org.slf4j.Logger;
		import org.slf4j.LoggerFactory;

		import java.io.File;
		import java.io.FileInputStream;
		import java.io.InputStream;
		import java.util.Map;
		import java.util.Properties;
		import java.util.concurrent.CountDownLatch;
		import java.util.concurrent.TimeUnit;

public class OracleObjectStorageClassic extends Device implements ArchiveStore {

	private static final Logger logger = LoggerFactory.getLogger(OracleObjectStorageClassic.class);
	private static String DEFAULT_CONTAINER_NAME = "datavault-container-edina";
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/.oci/config";
	private static final String PROFILE = "DEFAULT";
	//private FileTransferManager manager = null;
	//private ObjectStorageAsync client = null;
	private ObjectStorage client = null;
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
		this.client = new ObjectStorageClient(this.getAuthDetailsProvider());
		if (this.client == null) {
			logger.debug("Client is null");
			throw new Exception("Client is null");
		} else {
			logger.debug("Client is not null");
		}
		logger.info("Oracle Gen 2 Store method start 2");
		this.client.setRegion(Region.UK_LONDON_1);

		String contentType = "";
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
			latch.await();
			if (failed != null) {
				if (failed instanceof Exception) {
					throw (Exception) failed;
				}
				throw (Error) failed;
			}
			return item;
		}

		@Override
		public void onSuccess(IN request, OUT response) {
			item = response;
			latch.countDown();
		}

		@Override
		public void onError(IN request, Throwable error) {
			failed = error;
			latch.countDown();
		}
	}
}
