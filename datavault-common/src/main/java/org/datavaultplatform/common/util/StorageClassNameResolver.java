package org.datavaultplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;

@Slf4j
public class StorageClassNameResolver {

  public static final String CLASS_NAME_SFTP_FILE_SYSTEM = SFTPFileSystem.class.getName();
  public static final String CLASS_NAME_SFTP_FILE_SYSTEM_JSCH = SFTPFileSystemJSch.class.getName();
  public static final String CLASS_NAME_SFTP_FILE_SYSTEM_SSHD = SFTPFileSystemSSHD.class.getName();

  public static final StorageClassNameResolver FIXED_SSHD = new StorageClassNameResolver(true);
  public static final StorageClassNameResolver FIXED_JSCH = new StorageClassNameResolver(false);

  private final String resolvedSftpFileSystem;

  public StorageClassNameResolver(boolean sftpDriverUseApacheSSHD) {
    this.resolvedSftpFileSystem = sftpDriverUseApacheSSHD
        ? CLASS_NAME_SFTP_FILE_SYSTEM_SSHD
        : CLASS_NAME_SFTP_FILE_SYSTEM_JSCH;

    log.info("sftpDriverUseApacheSSHD[{}]resolvedSftpFileSystem[{}]",
        sftpDriverUseApacheSSHD,
        resolvedSftpFileSystem);
  }

  public String resolveStorageClassName(String storageClassName) {
    if (CLASS_NAME_SFTP_FILE_SYSTEM.equals(storageClassName)) {
      return resolvedSftpFileSystem;
    } else {
      return storageClassName;
    }
  }
}
