package org.datavaultplatform.worker.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RetrieveTest {

  @Test
  @SneakyThrows
  void testThrowCheckSumError() {
    File mFile = mock(File.class);
    when(mFile.getCanonicalPath()).thenReturn("/tmp/some/file.1.tar");
    Exception ex = assertThrows(Exception.class, () -> Retrieve.throwChecksumError("<actualCS>", "<expectedCS>",
        mFile, "test"));
    assertEquals("Checksum failed:test:(actual)<actualCS> != (expected)<expectedCS>:/tmp/some/file.1.tar", ex.getMessage());
  }
}
