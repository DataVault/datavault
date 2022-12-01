package org.datavaultplatform.common.storage;

import java.util.Optional;
import java.util.stream.Stream;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic;
import org.datavaultplatform.common.storage.impl.S3Cloud;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;

public interface StorageConstants {
  String CLOUD_AWS_S3 = S3Cloud.class.getName();

  //We don't use the SFTPFileSystem class any more, but we do use its class name.
  String SFTP_FILE_SYSTEM = "org.datavaultplatform.common.storage.impl.SFTPFileSystem";
  String LOCAL_FILE_SYSTEM = LocalFileSystem.class.getName();
  String TIVOLI_STORAGE_MANAGER = TivoliStorageManager.class.getName();
  String CLOUD_ORACLE = OracleObjectStorageClassic.class.getName();

  String STORAGE_PREFIX = "org.datavaultplatform.common.storage.impl.";

  static Optional<String> getStorageClass(String type) {
    return Stream.of (
            CLOUD_ORACLE, CLOUD_AWS_S3,
            TIVOLI_STORAGE_MANAGER, LOCAL_FILE_SYSTEM, SFTP_FILE_SYSTEM
        )
        .filter(storage -> storage.equals(STORAGE_PREFIX + type))
        .findFirst();
  }
}
