package org.datavaultplatform.common.crypto;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class EncryptSmallFileTest extends BaseTempKeyStoreTest {

  @TempDir
  File tempDir;

  @Test
  @SneakyThrows
  void testSmallFileEncryptAndDecrypt() {

    File origFile = new File(tempDir, "orig.txt");
    FileUtils.writeStringToFile(origFile, "Hello World", StandardCharsets.UTF_8);
    File testFile = new File(tempDir, "test.txt");

    assertTrue(new File(keyStorePath).exists());
    Context mContext = Mockito.mock(Context.class);
    when(mContext.getEncryptionMode()).thenReturn(AESMode.GCM);

    Files.copy(origFile, testFile);
    assertTrue(origFile.exists());
    assertTrue(testFile.exists());
    checkSameFile(origFile, testFile);

    byte[] iv = Encryption.encryptFile(mContext, testFile);
    assertEquals(96, iv.length);

    checkNotSameFile(origFile, testFile);

    Encryption.decryptFile(mContext, testFile, iv);

    checkSameFile(origFile, testFile);

    verify(mContext, times(2)).getEncryptionMode();
    verifyNoMoreInteractions(mContext);
  }

  private void checkNotSameFile(File origFile, File testFile) {
    assertFalse(Arrays.equals(getContents(origFile), getContents(testFile)));
  }

  private void checkSameFile(File origFile, File testFile) {
    assertArrayEquals(getContents(origFile), getContents(testFile));
  }

  @SneakyThrows
  private byte[] getContents(File file) {
    try (FileInputStream fis = new FileInputStream(file)) {
      return IOUtils.toByteArray(fis);
    }
  }

  @SneakyThrows
  private File createRandomFile(int size) {
    File result = File.createTempFile("test", ".bin");
    try (RandomAccessFile raf = new RandomAccessFile(result, "rw")) {
      for (int i = 0; i < size; i++) {
        byte random = (byte) (Math.random() * 256);
        raf.writeByte(random);
      }
    }
    return result;
  }

  @SneakyThrows
  boolean doesNewEncryptionWorkWithSmallFileOfGivenSize(int size) {
    try {

      File orig = createRandomFile(size);
      File copy = File.createTempFile("test", ".copy");
      Files.copy(orig, copy);
      assertEquals(size, orig.length());
      assertEquals(size, copy.length());

      Context mContext = Mockito.mock(Context.class);
      when(mContext.getEncryptionMode()).thenReturn(AESMode.GCM);

      byte[] iv = Encryption.encryptFile(mContext, copy);
      return true;
    } catch (CryptoException ex) {
      return false;
    }
  }

  /**
   * This test was added because the OLD crypto code had problems with small files.
   * Where the ( fileSize mod 1024 ) was < 1.
   * 16 is the GCM block size.
   * 1024 was the encryption buffer size in the old code.
   * @param numOf1024ByteBlocks
   */
  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void testNewEncryptionWorkWithSmallFiles(int numOf1024ByteBlocks) {
    int base = 1024 * numOf1024ByteBlocks;
    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base));

    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 1));
    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 15));

    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 16));

    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 17));
    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 1023));
    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 1024));

    assertTrue(doesNewEncryptionWorkWithSmallFileOfGivenSize(base + 1025));
  }

  @Test
  @SneakyThrows
  void testEmptyFileEncryptAndDecrypt() {

    File origFile = File.createTempFile("orig", ".txt");
    File testFile = File.createTempFile("test", ".txt");

    assertTrue(new File(keyStorePath).exists());
    Context mContext = Mockito.mock(Context.class);
    when(mContext.getEncryptionMode()).thenReturn(AESMode.GCM);

    assertEquals(0, origFile.length());
    assertEquals(0, testFile.length());

    assertTrue(origFile.exists());
    assertTrue(testFile.exists());

    checkSameFile(origFile, testFile);

    //ENCRYPT
    byte[] iv = Encryption.encryptFile(mContext, testFile);
    assertEquals(96, iv.length);

    log.info("after encrypt test file size [{}]", testFile.length());

    checkNotSameFile(origFile, testFile);

    assertEquals(0, origFile.length());
    assertEquals(16, testFile.length());

    assertFalse(doFilesHaveSameHash(origFile, testFile));

    //DECRYPT
    Encryption.decryptFile(mContext, testFile, iv);

    assertEquals(0, testFile.length());
    checkSameFile(origFile, testFile);

    assertTrue(doFilesHaveSameHash(origFile, testFile));

    verify(mContext, times(2)).getEncryptionMode();
    verifyNoMoreInteractions(mContext);
  }

  @SneakyThrows
  static boolean doFilesHaveSameHash(File f1, File f2) {
    String hash1 = Verify.getDigest(f1);
    String hash2 = Verify.getDigest(f2);
    return hash1.equals(hash2);
  }

  public static String split(String value, int num) {
    char[] chars = value.toCharArray();
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      sb.append(c);
      count++;
      if (count == num && i < chars.length - 1) {
        sb.append("-");
        count = 0;
      }
    }
    return sb.toString();
  }

  @Test
  void testSplit() {
    assertEquals("", split("", 5));
    assertEquals("1", split("1", 5));
    assertEquals("12", split("12", 5));
    assertEquals("123", split("123", 5));
    assertEquals("1234", split("1234", 5));
    assertEquals("12345", split("12345", 5));
    assertEquals("12345-6", split("123456", 5));
    assertEquals("12345-67890", split("1234567890", 5));
    assertEquals("12345-67890-12", split("123456789012", 5));
  }
}
