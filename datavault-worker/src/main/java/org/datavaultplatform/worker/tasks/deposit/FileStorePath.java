package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record FileStorePath(String storageID, String storagePath) {

    public FileStorePath(String fileStorePath) {
        this(getStorageID(fileStorePath), getStoragePath(fileStorePath));
        log.info("Deposit file: " + fileStorePath);
        log.info("Deposit storageID: " + storageID);
        log.info("Deposit storagePath: " + storagePath);
    }

    private static String getStorageID(String fileStorePath){
        String storageID = fileStorePath.substring(0, fileStorePath.indexOf('/'));
        return storageID;
    }
    
    private static String getStoragePath(String fileStorePath){
        String storagePath = fileStorePath.substring(fileStorePath.indexOf('/') + 1);
        return storagePath;
    }
}

