package org.datavaultplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;

@Slf4j
public class StorageClassNameResolver {

  public static final String CLASS_NAME_SFTP_FILE_SYSTEM = SFTPFileSystem.class.getName();
  public static final String CLASS_NAME_SFTP_FILE_SYSTEM_JSCH = SFTPFileSystem.class.getName();
  public static final String CLASS_NAME_SFTP_FILE_SYSTEM_SSHD = SFTPFileSystemSSHD.class.getName();

  public static final StorageClassNameResolver FIXED_JSCH = new StorageClassNameResolver(true);
  public static final StorageClassNameResolver FIXED_SSHD = new StorageClassNameResolver(false);
  private final String resolvedSftpFileSystem;

  public StorageClassNameResolver(boolean sftpDriverUseJSch) {
    this.resolvedSftpFileSystem = sftpDriverUseJSch ? CLASS_NAME_SFTP_FILE_SYSTEM_JSCH : CLASS_NAME_SFTP_FILE_SYSTEM_SSHD;
    log.info("sftpDriverUseJsch[{}]resolvedSftpFileSysten[]",sftpDriverUseJSch,resolvedSftpFileSystem);
  }

  public String resolveStorageClassName(String storageClassName) {
    if (CLASS_NAME_SFTP_FILE_SYSTEM.equals(storageClassName)) {
      return resolvedSftpFileSystem;
    } else {
      return storageClassName;
    }
  }
}
