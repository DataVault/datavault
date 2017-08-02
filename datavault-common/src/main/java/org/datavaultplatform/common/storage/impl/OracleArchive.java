package org.datavaultplatform.common.storage.impl;

import oracle.cloudstorage.ftm.*;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.exception.ClientException;

public class OracleArchive extends Device implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(OracleArchive.class);

    public Verify.Method verificationMethod = Verify.Method.LOCAL_ONLY;

    private FileTransferAuth auth;
    private String containerName;



    public OracleArchive (String name, Map<String,String> config)  {
        super(name, config);

        auth = new FileTransferAuth(
                config.get("username"),
                config.get("password").toCharArray(),
                config.get("serviceName"),
                config.get("serviceUrl"),
                config.get("identityDomain"));

        containerName = config.get("containerName");
    }

    @Override
    public long getUsableSpace() throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        FileTransferManager manager = null;
        try {
            manager = FileTransferManager.getDefaultFileTransferManager(auth);

            DownloadConfig downloadConfig = new DownloadConfig();

            logger.info("Downloading file " + path + " from container " + containerName);
            TransferResult downloadResult = manager.download(downloadConfig, containerName, path, working);
            logger.info("Download completed. State:" + downloadResult.getState());

            TransferState ts = downloadResult.getState();
            while (ts.equals(TransferState.RestoreInProgress)) {
                logger.info("Restore in progress. % completed: " + downloadResult.getRestoreCompletedPercentage());
                Thread.sleep(1 * 60 * 1000); // Wait for 1 minute.
                downloadResult = manager.download(downloadConfig, containerName, path, working);
                ts = downloadResult.getState();
            }

            logger.info("Download Result:" + downloadResult.toString());
            logger.info("Completed synchronous downloading of file ... ");

        } catch (ClientException ce) {
            System.out.println("Operation failed. " + ce.getMessage());
        } finally {
            if (manager != null) {
                manager.shutdown();
            }
        }

    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        FileTransferManager manager = null;
        try {
            manager = FileTransferManager.getDefaultFileTransferManager(auth);

            UploadConfig uploadConfig = new UploadConfig();
            // todo  : I think a value of false prevents an object of the same name being overwritten, but this needs checked
            uploadConfig.setOverwrite(false);
            uploadConfig.setStorageClass(CloudStorageClass.Archive);

            TransferTask<TransferResult> uploadTask = manager.uploadAsync(uploadConfig, containerName, working.getName(), working);

            logger.info("Waiting for upload task to complete...");

            TransferResult uploadResult = uploadTask.getResult();

            logger.info("Task completed. State:" + uploadResult.getState());
            logger.info("Object name is " + uploadResult.getObjectName());
        } catch (ClientException ce) {
            System.out.println("Operation failed. " + ce.getMessage());
        } finally {
            if (manager != null) {
                manager.shutdown();
            }
        }

        return working.getName();
    }

    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
}
