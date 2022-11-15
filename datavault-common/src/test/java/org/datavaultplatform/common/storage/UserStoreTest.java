package org.datavaultplatform.common.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.S3Cloud;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserStoreTest {

  StorageClassNameResolver resolver = StorageClassNameResolver.FIXED_JSCH;

  @Test
  @SneakyThrows
  void testSFTPFileSystemFromFileStore() {
    HashMap<String, String> props = new HashMap<>();
    props.put(PropNames.PASSWORD, "password-value");
    props.put(PropNames.PASSPHRASE, "passphrase-value");
    props.put(PropNames.PRIVATE_KEY, toBase64("private-key-value"));
    props.put(PropNames.IV, toBase64("iv-value"));
    props.put(PropNames.HOST, "host-value");
    props.put(PropNames.PORT, "2112");
    props.put(PropNames.ROOT_PATH, "/root/path");

    FileStore fs = new FileStore();
    fs.setProperties(props);
    fs.setStorageClass(SFTPFileSystem.class.getName());

    UserStore result = UserStore.fromFileStore(fs, resolver);
    assertTrue(result instanceof SFTPFileSystem);
    SFTPFileSystem sftp = (SFTPFileSystem) result;
    Class<SFTPFileSystemJSch> clazz = SFTPFileSystemJSch.class;
    Field f1Password = clazz.getDeclaredField("password");
    Field f2Passphrase = clazz.getDeclaredField("passphrase");
    Field f3PrivateKey = clazz.getDeclaredField("encPrivateKey");
    Field f4EncIV = clazz.getDeclaredField("encIV");
    Field f5Host = clazz.getDeclaredField("host");
    Field f6Port = clazz.getDeclaredField("port");
    Field f7RootPath = clazz.getDeclaredField("rootPath");

    f1Password.setAccessible(true);
    f2Passphrase.setAccessible(true);
    f3PrivateKey.setAccessible(true);
    f4EncIV.setAccessible(true);
    f5Host.setAccessible(true);
    f6Port.setAccessible(true);
    f7RootPath.setAccessible(true);

    String sftpPassword = (String) f1Password.get(sftp);
    String sftpPassphrase = (String) f2Passphrase.get(sftp);
    byte[] sftpEncPrivateKey = (byte[]) f3PrivateKey.get(sftp);
    byte[] sftpEncIV = (byte[]) f4EncIV.get(sftp);
    String sftpHost = (String) f5Host.get(sftp);
    int sftpPort = (int) f6Port.get(sftp);
    String sftpRootPath = (String) f7RootPath.get(sftp);

    assertEquals(2112, sftpPort);
    assertEquals("password-value", sftpPassword);
    assertEquals("passphrase-value", sftpPassphrase);
    assertEquals("host-value", sftpHost);
    assertEquals("/root/path", sftpRootPath);
    assertArrayEquals(sftpEncIV, "iv-value".getBytes(StandardCharsets.UTF_8));
    assertArrayEquals(sftpEncPrivateKey, "private-key-value".getBytes(StandardCharsets.UTF_8));
  }

  private String toBase64(String raw) {
    return java.util.Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private String fromBase64(String base64) {
    return new String(java.util.Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
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

    UserStore result = UserStore.fromFileStore(fs, resolver);
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
        UserStore.fromFileStore(getFileStore(String.class), resolver);
      });
      assertEquals(
          "The class [java.lang.String] does not inherit from [org.datavaultplatform.common.storage.UserStore]",
          ex.getMessage());
    }

    @Test
    @SneakyThrows
    void testS3CloudIsInvalidStorageClass() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        UserStore.fromFileStore(getFileStore(S3Cloud.class), StorageClassNameResolver.FIXED_JSCH);
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
