package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;

import java.io.File;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.TransferTask;
import oracle.cloudstorage.ftm.UploadConfig;
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


    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        String objectName = null;

        FileTransferManager manager = null;
        try {
            manager = FileTransferManager.getDefaultFileTransferManager(auth);

            UploadConfig uploadConfig = new UploadConfig();
            uploadConfig.setOverwrite(true);
            uploadConfig.setStorageClass(CloudStorageClass.Standard);
            TransferTask<TransferResult> uploadTask = manager.uploadAsync(uploadConfig, containerName, null, working);

            logger.info("Waiting for upload task to complete...");
            TransferResult uploadResult = uploadTask.getResult();
            logger.info("Task completed. State:" + uploadResult.getState());
            objectName = uploadResult.getObjectName();
            logger.info("Object name is " + objectName);
        } catch (ClientException ce) {
            System.out.println("Operation failed. " + ce.getMessage());
        } finally {
            if (manager != null) {
                manager.shutdown();
            }
        }

        // todo : Is this a useful thing to return??
        return objectName;
    }

    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
}
