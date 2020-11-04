package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicReference;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.responses.AsyncHandler;
import org.apache.commons.collections.map.HashedMap;
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

    /*
     * Add local jars to mvn
     * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/ftm-api-2.4.2.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=ftm-api -Dversion=1.0 -Dpackaging=jar
     * mvn install:install-file -Dfile=/Users/dspeed2/Downloads/ftm-sdk-2.4.2/libs/low-level-api-core-1.14.19.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=low-level-api-core -Dversion=1.0 -Dpackaging=jar
     *
     * javax.json-1.0.4.jar has been added to the common project pom file and when I tried to add the other jars from the sample code to the pom the validator said they were
     * already managed ( log4j-1.2.17.jar, slf4j-api-1.7.7.jar, slf4j-log4j12-1.7.7.jar)
     */
    public OracleObjectStorageGen2(String name, Map<String, String> config) throws Exception {
        super(name, config);
        super.depositIdStorageKey = true;
        String retryKey = "occRetryTime";
        String maxKey = "occMaxRetries";

        if (config.containsKey(retryKey)){
            try {
                OracleObjectStorageGen2.retryTime = Integer.parseInt(config.get(retryKey));
            } catch (NumberFormatException nfe) {
                OracleObjectStorageGen2.retryTime = OracleObjectStorageGen2.defaultRetryTime;
            }
        }
        if (config.containsKey(maxKey)){
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
                if (r == (OracleObjectStorageGen2.maxRetries -1)) {
                    throw ce;
                }
                TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
            } catch (Exception e) {
                logger.error("Download failed. " + e.getMessage());
                if (r == (OracleObjectStorageGen2.maxRetries -1)) {
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
                if (r == (OracleObjectStorageGen2.maxRetries -1)) {
                    throw ce;
                }
                TimeUnit.MINUTES.sleep(OracleObjectStorageGen2.retryTime);
            } catch (Exception e) {
                logger.error("Upload failed. " + "Retrying in " + OracleObjectStorageGen2.retryTime + " mins " + e.getMessage());
                if (r == (OracleObjectStorageGen2.maxRetries -1)) {
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

}