package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public class TarFileInputStreamFactory {

  public static final long ONE_GIGABYTE = 1024 * 1024 * 1024;
  private final Long fileSize;

  public TarFileInputStreamFactory(Long fileSize) {
    Assert.isTrue(fileSize == null || fileSize >= 0, "non-null file size cannot be negative");
    this.fileSize = fileSize;
  }

  public TarFileInputStreamFactory() {
    this(null);
  }

  @SneakyThrows
  public InputStream getInputStream(File f) {
    if (fileSize == null) {
      return new FileInputStream(f);
    } else {
      return new InputStream() {
        long counter = fileSize;

        @Override
        public int read(byte[] b) {
          if (counter == 0) {
            return -1;
          }
          int read = (int) Math.min(counter, b.length);
          Arrays.fill(b, 0, read, (byte)1);
          counter -= read;
          return read;
        }

        @Override
        public synchronized int read() throws IOException {
          if (counter == 0) {
            return -1;
          }
          counter--;
          return 1;
        }
      };
    }
  }
}
