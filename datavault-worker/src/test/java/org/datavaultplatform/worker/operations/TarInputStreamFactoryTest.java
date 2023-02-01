package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class TarInputStreamFactoryTest {

  public static final long MEGABYTE = 1024 * 1024;

  public static final long GIGABYTE = 1024 * MEGABYTE;

  public static final long TERABYTE = 1024 * GIGABYTE;

  File tempSourceFile;

  @BeforeEach
  @SneakyThrows
  void setup() {
    tempSourceFile = File.createTempFile("temp2", ".txt");
    tempSourceFile.deleteOnExit();
  }

  @Test
  void testConstructorArgs() {
    assertThrows(IllegalArgumentException.class, () -> new TarFileInputStreamFactory(-1L));
  }

  @Test
  @SneakyThrows
  void testLargeFile() {
    File large50mbFile = new ClassPathResource("big_data/50MB_file").getFile();
    InputStream is = new TarFileInputStreamFactory().getInputStream(large50mbFile);
    long copied = copyToBlackHole(is);
    assertEquals(50000000L, copied);
  }

  @Test
  @SneakyThrows
  void testNormalFile() {

    File temp = File.createTempFile("temp1", ".txt");
    try(FileWriter fw = new FileWriter(temp)){
      fw.write("test");
    }
    InputStream is = new TarFileInputStreamFactory().getInputStream(temp);
    long copied = copyToBlackHole(is);
    assertEquals(4, copied);
  }


  @Test
  @SneakyThrows
  void testFakedFileSmall() {
    File temp = File.createTempFile("temp2", ".txt");
    InputStream is = new TarFileInputStreamFactory(2112L).getInputStream(temp);
    long copied = copyToBlackHole(is);
    assertEquals(2112, copied);
  }
  @Test
  @SneakyThrows
  void testFakedFileZeroSize() {
    InputStream is = new TarFileInputStreamFactory(0L).getInputStream(tempSourceFile);
    long copied = copyToBlackHole(is);
    assertEquals(0, copied);
  }

  @Test
  @SneakyThrows
  void testFakedFile1MB() {
    InputStream is = new TarFileInputStreamFactory(MEGABYTE).getInputStream(tempSourceFile);
    long copied = copyToBlackHole(is);
    assertEquals(MEGABYTE, copied);
  }

  @Test
  @SneakyThrows
  void testFakedFile1GB() {
    InputStream is = new TarFileInputStreamFactory(GIGABYTE).getInputStream(tempSourceFile);
    long copied = copyToBlackHole(is);
    assertEquals(GIGABYTE, copied);
  }
  @Test
  @SneakyThrows
  void testFakedFile512GB() {
    InputStream is = new TarFileInputStreamFactory(512 * GIGABYTE).getInputStream(tempSourceFile);
    long copied = copyToBlackHole(is);
    assertEquals(512 * GIGABYTE, copied);
  }

  @Test
  @SneakyThrows
  void testFakedFile1TB() {
    InputStream is = new TarFileInputStreamFactory(TERABYTE).getInputStream(tempSourceFile);
    long copied = copyToBlackHole(is);
    assertEquals(TERABYTE, copied);
  }

  @SneakyThrows
  private long copyToBlackHole(InputStream is) {
    OutputStream os = new BlackHoleOutputStream();
    long copied = IOUtils.copyLarge(is, os);
    return copied;
  }

}
