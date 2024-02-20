package org.datavaultplatform.common.bagish;

import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.datavaultplatform.common.storage.Verify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.datavaultplatform.common.bagish.SupportedAlgorithm.MD5;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class CheckSummerTest {

  public static final String TEST_CONTENTS = "test file contents";

  @TempDir
  File tempDir;

  @SneakyThrows
  File writetoTempFile(String contents) {
    File file = new File(tempDir, "temp.txt");
    try (FileWriter fw = new FileWriter(file)) {
      fw.write(contents);
    }
    return file;
  }

  @Test
  @SneakyThrows
  void testArgChecks() {
    IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
        () -> new Checksummer().computeFileHash(new File("/"), MD5));
    assertEquals("[/] is not a file", ex1.getMessage());

    IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
        () -> new Checksummer().computeFileHash(new File("/doesNotExist"), MD5));
    assertEquals("The file [/doesNotExist] does not exist", ex2.getMessage());

    File notReadable = File.createTempFile("temp", ".txt");
    notReadable.setReadable(false);
    IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
        () -> new Checksummer().computeFileHash(notReadable, MD5));
    assertTrue(ex3.getMessage().endsWith("is not readable"));

    File testFile = writetoTempFile(TEST_CONTENTS);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new Checksummer().computeFileHash(testFile, null));
    assertEquals("The SupportedAlgorithm cannot be null", ex.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
      "MD5,    891bcd3700619af5151bf95b836ff9b1",
      "SHA1,   cbaedccfded0c768295aae27c8e5b3a0025ef340",
      "SHA256, c4fa968a745586faaa030054f51fb1cafd5e9ae25fa6b137ac6477715fdc81b1",
      "SHA512, f2bb7acec79f2ce98adb0968dc9d41d344135463a486221dc8302684d7138455f72c94040d4096327e2d9f14f31c4db1b9044173572dfc5c1f03c8066adc44ff",
  })
  void testCheckSum(SupportedAlgorithm algorithm, String expectedHash) throws Exception {
    File testFile = writetoTempFile(TEST_CONTENTS);
    Checksummer summer = new Checksummer();
    assertEquals(expectedHash, summer.computeFileHash(testFile, algorithm));
  }

  @ParameterizedTest
  @CsvSource({
      "MD5,    d41d8cd98f00b204e9800998ecf8427e",
      "SHA1,   da39a3ee5e6b4b0d3255bfef95601890afd80709",
      "SHA256, e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      "SHA512, cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
  })
  void testCheckSumEmptyFile(SupportedAlgorithm algorithm, String expectedHash) throws Exception {
    File testFile = writetoTempFile("");
    Checksummer summer = new Checksummer();
    assertEquals(expectedHash, summer.computeFileHash(testFile, algorithm));
  }

  @Test
  void checkIOException() {
    File testFile = writetoTempFile(TEST_CONTENTS);
    try (MockedStatic<DigestUtils> utilities = Mockito.mockStatic(DigestUtils.class)) {
      utilities.when(() -> DigestUtils.md5Hex(any(InputStream.class)))
          .thenThrow(new IOException("oops"));

      Checksummer summer = new Checksummer();

      IOException io = assertThrows(IOException.class, () -> summer.computeFileHash(testFile, MD5));
      assertEquals("oops", io.getMessage());
    }

  }

  @Test
  void testChecksummerAgainstVerify() throws Exception {
    File testFile = writetoTempFile(TEST_CONTENTS);
    String digest1 = Verify.getDigest(testFile);
    Checksummer summer = new Checksummer();
    String digest2 = summer.computeFileHash(testFile, SupportedAlgorithm.SHA1).toUpperCase();
    assertEquals(digest1, digest2);
  }

}
