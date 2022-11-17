package org.datavaultplatform.common.storage.impl;

import lombok.extern.slf4j.Slf4j;

/**
 * pre dv5...
 *
 * The storageClassName String 'org.datavaultplatform.common.storage.impl.SFTPFileSystem'
 * - was used to create instances of this class via "reflection". That approach was not flexible.
 *
 * with dv5...
 *
 * We have to keep using the String 'org.datavaultplatform.common.storage.impl.SFTPFileSystem'
 * - it will be in the DV database.
 * This "class" is NOT used but the String representation of its "class name" is.
 * {@code @See} org.datavaultplatform.common.storage.StorageConstants#SFTP_FILE_SYSTEM
 *
 * This class is left here as a placeholder.
 * You cannot create instances of this class.
 * You cannot create subclasses of this class.
 *
 * With dv5, StorageClassNameResolver takes the String 'org.datavaultplatform.common.storage.impl.SFTPFileSystem'
 * and decides which SFTP Driver to return based on configuration. This is more flexible.
 * {@code @See} org.datavaultplatform.common.util.StorageClassNameResolver
 */
@Slf4j
public abstract class SFTPFileSystem {
  private SFTPFileSystem(){}
}