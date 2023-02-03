package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnTarNestedTest extends BaseNestedTarTest {

  Path tempUnTarDir;

  @SneakyThrows
  @BeforeEach
  void setup() {
    initBase();

    //the temp dir to un-tar into
    tempUnTarDir = Files.createTempDirectory("tempUnTarDir");
    tempUnTarDir.toFile().deleteOnExit();

    assertTrue(tempUnTarDir.toFile().exists() && tempUnTarDir.toFile().isDirectory());
  }

  @Test
  @SneakyThrows
  void unTarTest() {
    File result = Tar.unTar(referenceTarFile, tempUnTarDir);
    assertTrue(result.exists() && result.isDirectory());
    assertEquals(new File(tempUnTarDir.toFile(), "tarContents"), result);

    Map<String,Long> expectedFiles = Files.walk(contentsDir.toPath())
        .filter(Files::isRegularFile)
        .collect(Collectors.toMap(p -> relativePath(contentsDir.toPath(), p), p -> p.toFile().length()));

    Map<String,Long> actualFiles = Files.walk(tempUnTarDir)
        .filter(Files::isRegularFile)
        .collect(Collectors.toMap(p -> relativePath(tempUnTarDir, p), p -> p.toFile().length()));

    assertEquals(expectedFiles.entrySet(), actualFiles.entrySet());
  }

  private String relativePath(Path base, Path file) {
    return base.relativize(file).toString();
  }

}
