package org.datavaultplatform.common.storage;

import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.S3Cloud;
import org.datavaultplatform.common.storage.impl.SFTPConnectionInfo;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserStoreTest {

  private static final StorageClassNameResolver RESOLVER = StorageClassNameResolver.FIXED_SSHD;

  @Test
  @SneakyThrows
  void testSFTPFileSystemFromFileStore() {
    HashMap<String, String> props = new HashMap<>();
    props.put(PropNames.USERNAME, "username-value");
    props.put(PropNames.PASSWORD, "password-value");
    props.put(PropNames.HOST, "host-value");
    props.put(PropNames.PORT, "2112");
    props.put(PropNames.ROOT_PATH, "/root/path");

    FileStore fs = new FileStore();
    fs.setProperties(props);
    fs.setStorageClass(StorageConstants.SFTP_FILE_SYSTEM);

    UserStore result = UserStore.fromFileStore(fs, RESOLVER);
    assertTrue(result instanceof SFTPFileSystemSSHD);
    SFTPFileSystemSSHD sftp = (SFTPFileSystemSSHD) result;
    Class<SFTPFileSystemSSHD> clazz = SFTPFileSystemSSHD.class;
    Class<SFTPConnectionInfo> infoClass = SFTPConnectionInfo.class;
    Field fConnectionInfo = clazz.getDeclaredField("connectionInfo");
    fConnectionInfo.setAccessible(true);
    SFTPConnectionInfo info = (SFTPConnectionInfo) fConnectionInfo.get(sftp);
    Field f1Password = infoClass.getDeclaredField("password");
    Field f2Username = infoClass.getDeclaredField("username");
    Field f5Host = infoClass.getDeclaredField("host");
    Field f6Port = infoClass.getDeclaredField("port");
    Field f7RootPath = infoClass.getDeclaredField("rootPath");

    f1Password.setAccessible(true);
    f2Username.setAccessible(true);
    f5Host.setAccessible(true);
    f6Port.setAccessible(true);
    f7RootPath.setAccessible(true);

    String sftpPassword = (String) f1Password.get(info);
    String sftpUsername = (String) f2Username.get(info);
    String sftpHost = (String) f5Host.get(info);
    int sftpPort = (int) f6Port.get(info);
    String sftpRootPath = (String) f7RootPath.get(info);

    assertEquals(2112, sftpPort);
    assertEquals("username-value", sftpUsername);
    assertEquals("password-value", sftpPassword);
    assertEquals("host-value", sftpHost);
    assertEquals("/root/path", sftpRootPath);
  }

  @Test
  @SneakyThrows
  void testLocalFileSystemFromFileStore(@TempDir File tempDir) {
    HashMap<String, String> props = new HashMap<>();

    // rootPath must be a directory that exists - that's why we use @TempDir
    String rootPath = tempDir.getAbsolutePath();
    props.put(PropNames.ROOT_PATH, rootPath);

    writeFile(tempDir, "temp1.txt", "temp1-content");
    writeFile(tempDir, "temp2.txt", "temp2-content");

    FileStore fs = new FileStore();
    fs.setProperties(props);
    fs.setStorageClass(LocalFileSystem.class.getName());

    UserStore result = UserStore.fromFileStore(fs, RESOLVER);
    assertTrue(result instanceof LocalFileSystem);
    LocalFileSystem local = (LocalFileSystem) result;
    Field f1RootPath = LocalFileSystem.class.getDeclaredField("rootPath");
    f1RootPath.setAccessible(true);
    assertEquals(rootPath, f1RootPath.get(local));

    assertTrue(local.isDirectory("."));
    assertFalse(local.isDirectory("bob"));

    List<FileInfo> files = local.list(".");
    assertEquals(2, files.size());

    FileInfo temp1 = find(files, "temp1.txt");
    FileInfo temp2 = find(files, "temp2.txt");

    assertEquals("temp1.txt", temp1.getName());
    assertEquals("temp2.txt", temp2.getName());
  }

  FileInfo find(List<FileInfo> infos, String filename) {
    return infos.stream().filter(fi -> fi.getName().equals(filename)).findFirst().get();
  }

  @SneakyThrows
  private void writeFile(File tempDir, String filename, String contents) {
    try (FileWriter fw = new FileWriter(tempDir.toPath().resolve(filename).toFile())) {
      fw.write(contents);
    }
  }

  @Nested
  class InvalidStorageClasses {

    @Test
    @SneakyThrows
    void testStringIsInvalidStorageClass() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        UserStore.fromFileStore(getFileStore(String.class), RESOLVER);
      });
      assertEquals(
          "The class [java.lang.String] does not inherit from [org.datavaultplatform.common.storage.UserStore]",
          ex.getMessage());
    }

    @Test
    @SneakyThrows
    void testS3CloudIsInvalidStorageClass() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        UserStore.fromFileStore(getFileStore(S3Cloud.class), StorageClassNameResolver.FIXED_SSHD);
      });
      assertEquals(
          "The class [org.datavaultplatform.common.storage.impl.S3Cloud] does not inherit from [org.datavaultplatform.common.storage.UserStore]",
          ex.getMessage());
    }

    private FileStore getFileStore(Class<?> clazz) {
      HashMap<String, String> props = new HashMap<>();
      FileStore fs = new FileStore();
      fs.setProperties(props);
      fs.setStorageClass(clazz.getName());
      return fs;
    }

  }
}
