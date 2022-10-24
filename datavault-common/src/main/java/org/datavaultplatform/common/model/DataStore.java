package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.datavaultplatform.common.storage.StorageConstants;

public interface DataStore {

  String getStorageClass();

  @JsonIgnore
  default boolean isOracle() {
    return StorageConstants.CLOUD_ORACLE.equals(getStorageClass());
  }
  @JsonIgnore
  default boolean isLocalFileSystem() {
    return StorageConstants.LOCAL_FILE_SYSTEM.equals(getStorageClass());
  }
  @JsonIgnore
  default boolean isSFTPFileSystem() {
    return StorageConstants.SFTP_FILE_SYSTEM.equals(getStorageClass());
  }
  @JsonIgnore
  default boolean isTivoliStorageManager() {
    return StorageConstants.TIVOLI_STORAGE_MANAGER.equals(getStorageClass());
  }
  @JsonIgnore
  default boolean isAmazonS3() {
    return StorageConstants.CLOUD_AWS_S3.equals(getStorageClass());
  }
}
