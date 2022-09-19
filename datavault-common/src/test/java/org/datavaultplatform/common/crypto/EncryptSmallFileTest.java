package org.datavaultplatform.common.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;
import sun.misc.IOUtils;

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
    assertTrue(Arrays.equals(getContents(origFile), getContents(testFile)));
  }


  @SneakyThrows
  private byte[] getContents(File file) {
    try(FileInputStream fis = new FileInputStream(file)) {
      return IOUtils.readAllBytes(fis);
    }
  }

}
