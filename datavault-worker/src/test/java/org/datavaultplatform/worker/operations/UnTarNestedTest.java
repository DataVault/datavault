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
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class UnTarNestedTest {

  File sourceTarFile;

  Path tempUnTarDir;

  File expectedUnTarDirectory;

  @SneakyThrows
  @BeforeEach
  void setup() {
    //the tar file to un-tar
    sourceTarFile = new ClassPathResource("tar/filtered.tar").getFile();
    assertTrue(sourceTarFile.exists() && sourceTarFile.isFile());

    //the temp dir to un-tar into
    tempUnTarDir = Files.createTempDirectory("tempUnTarDir");
    tempUnTarDir.toFile().deleteOnExit();

    assertTrue(tempUnTarDir.toFile().exists() && tempUnTarDir.toFile().isDirectory());

    //the expected un-tar directory
    expectedUnTarDirectory = new ClassPathResource("tar/contents").getFile();
    assertTrue(expectedUnTarDirectory.exists() && expectedUnTarDirectory.isDirectory());

    File tarContentsDir = new File(expectedUnTarDirectory, "tarContents");

    File empty0 = new File(tarContentsDir, "EMPTY_0/");
    empty0.mkdir();
    File empty1 = new File(tarContentsDir, "N1/EMPTY_1/");
    empty1.mkdir();
    File empty2 = new File(tarContentsDir, "N1/N2/EMPTY_2/");
    empty2.mkdir();
    File empty3 = new File(tarContentsDir, "N1/N2/N3/EMPTY_3/");
    empty3.mkdir();
    File empty4 = new File(tarContentsDir, "N1/N2/N3/N4/EMPTY_4/");
    empty4.mkdir();
    File empty5 = new File(expectedUnTarDirectory, "N1/N2/N3/N4/N5/EMPTY_5/");
    empty5.mkdir();
  }

  @Test
  @SneakyThrows
  void untarTest() {
    File result = Tar.unTar(sourceTarFile, tempUnTarDir);
    assertTrue(result.exists() && result.isDirectory());
    assertEquals(new File(tempUnTarDir.toFile(), "tarContents"), result);

    Map<String,Long> expectedFiles = Files.walk(expectedUnTarDirectory.toPath())
        .filter(Files::isRegularFile)
        .collect(Collectors.toMap(p -> relativePath(expectedUnTarDirectory.toPath(), p), p -> p.toFile().length()));

    Map<String,Long> actualFiles = Files.walk(tempUnTarDir)
        .filter(Files::isRegularFile)
        .collect(Collectors.toMap(p -> relativePath(tempUnTarDir, p), p -> p.toFile().length()));

    assertEquals(expectedFiles.entrySet(), actualFiles.entrySet());
  }

  private String relativePath(Path base, Path file) {
    return base.relativize(file).toString();
  }

  void tearDown(){
  }
}
