package org.datavaultplatform.broker.controllers;

import org.apache.commons.lang3.StringUtils;

public record StorageIdAndRetrievePath(String storageID, String retrievePath) {

    public static StorageIdAndRetrievePath fromFullPath(String fullPath){
        String storageID;
        String retrievePath;
        if (!fullPath.contains("/")) {
            // A request to retrieve the whole share/device
            storageID = fullPath;
            retrievePath = "/";

        } else {
            // A request to retrieve a sub-directory
            storageID = fullPath.substring(0, fullPath.indexOf("/"));
            retrievePath = fullPath.replaceFirst(storageID + "/", "");
            if(StringUtils.isBlank(retrievePath)){
                retrievePath = "/";
            }
        }
        return new StorageIdAndRetrievePath(storageID, retrievePath);
    }
}

