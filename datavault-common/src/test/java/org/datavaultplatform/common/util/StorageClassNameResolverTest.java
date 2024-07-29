package org.datavaultplatform.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StorageClassNameResolverTest {

  static abstract class BaseTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "", "not-sftp-system"})
    void testNonSftpFileSystem(String className) {
      assertEquals(className, getResolver().resolveStorageClassName(className));
    }

    @Test
    void testSftpFileSystem() {
      assertEquals(getExpected().getName(), getResolver().resolveStorageClassName(
          StorageConstants.SFTP_FILE_SYSTEM));
    }

    abstract StorageClassNameResolver getResolver();

    abstract Class<? extends SFTPFileSystemDriver> getExpected();
  }

  abstract static class BaseExpectJschTest extends BaseTest {

    @Override
    Class<? extends SFTPFileSystemDriver> getExpected() {
      return SFTPFileSystemJSch.class;
    }
  }

  abstract static class BaseExpectSSHDTest extends BaseTest {

    @Override
    Class<? extends SFTPFileSystemDriver> getExpected() {
      return SFTPFileSystemSSHD.class;
    }
  }

  @Nested
  class FixedJsch extends BaseExpectJschTest {

    @Override
    StorageClassNameResolver getResolver() {
      return StorageClassNameResolver.FIXED_JSCH;
    }
  }

  @Nested
  class ConfiguredJsch extends BaseExpectJschTest {

    @Override
    StorageClassNameResolver getResolver() {
      return new StorageClassNameResolver(false);
    }
  }

  @Nested
  class FixedSSHD extends BaseExpectSSHDTest {

    @Override
    StorageClassNameResolver getResolver() {
      return StorageClassNameResolver.FIXED_SSHD;
    }
  }

  @Nested
  class ConfiguredSSHD extends BaseExpectSSHDTest {

    @Override
    StorageClassNameResolver getResolver() {
      return new StorageClassNameResolver(true);
    }
  }
}
