package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.bagish.Checksummer;
import org.datavaultplatform.common.bagish.SupportedAlgorithm;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

@ExtendWith(MockitoExtension.class)
public class EncryptSmallFileTest extends BaseTempKeyStoreTest {

  @TempDir
  File tempDir;

  File origFile;
  File testFile;

  @BeforeEach
  @SneakyThrows
  void setup() {
    origFile = new File(tempDir, "orig.txt");
    FileUtils.writeStringToFile(origFile, "Hello World", StandardCharsets.UTF_8);
    testFile = new File(tempDir, "test.txt");
  }



  @Test
  @SneakyThrows
  void testSmallFileEncryptAndDecrypt() {

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

  @SneakyThrows
  static boolean doFilesHaveSameHash(File f1, File f2) {
    Checksummer summer = new Checksummer();
    String hash1 = summer.computeFileHash(f1, SupportedAlgorithm.MD5);
    String hash2 = summer.computeFileHash(f2, SupportedAlgorithm.MD5);
    return hash1.equals(hash2);
  }
}
