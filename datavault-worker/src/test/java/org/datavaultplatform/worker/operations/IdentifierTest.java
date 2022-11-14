package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Slf4j
class IdentifierTest {

  final Resource jpeg = new ClassPathResource("packager/banjo.jpg");

  final Resource pdf = new ClassPathResource("packager/item.pdf");

  @Test
  @SneakyThrows
  void testJPEG() {
      assertEquals("image/jpeg", Identifier.detectFile(jpeg.getFile()));
  }

  @Test
  @SneakyThrows
  void testPDF() {
    assertEquals("application/pdf", Identifier.detectFile(pdf.getFile()));
  }

  @Nested
  class DetectDirectory {

    HashMap<String, String> detected;

    @SneakyThrows
    DetectDirectory() {
      File directory = jpeg.getFile().getParentFile();
      detected = Identifier.detectDirectory(directory.toPath());
      assertEquals(7, detected.size());
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
        "col:n.tiff, image/tiff",
        "banjo.jpg, image/jpeg",
        "peng-uins.jpg, image/jpeg",
        "peacock butterfly.jpeg, image/jpeg",
        "gre>terthan.jpg, image/jpeg",
        "item.pdf, application/pdf",
        "ko_ala.jpg, image/jpeg"
    })
    void testDirectoryEntry(String name, String expectedType) {
      assertEquals(expectedType, detected.get(name));
    }
  }
}