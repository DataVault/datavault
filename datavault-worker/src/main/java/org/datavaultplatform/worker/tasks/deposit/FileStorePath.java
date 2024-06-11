package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public record FileStorePath(String storageID, String storagePath) {

    public FileStorePath(String fileStorePath) {
        this(getStorageID(fileStorePath), getStoragePath(fileStorePath));
        log.info("Deposit file: [{}]", fileStorePath);
        log.info("Deposit storageID: [{}]", storageID);
        log.info("Deposit storagePath: [{}]", storagePath);
    }

    private static String getStorageID(String fileStorePath){
        Assert.isTrue(fileStorePath != null, "The fileStorePath cannot be null");
        String cleansed = fileStorePath.trim();
        String storageID = cleansed.substring(0, cleansed.indexOf('/'));
        return storageID;
    }
    
    private static String getStoragePath(String fileStorePath){
        Assert.isTrue(fileStorePath != null, "The fileStorePath cannot be null");
        String cleansed = fileStorePath.trim();
        String storagePath = cleansed.substring(cleansed.indexOf('/') + 1);
        return storagePath;
    }
}

