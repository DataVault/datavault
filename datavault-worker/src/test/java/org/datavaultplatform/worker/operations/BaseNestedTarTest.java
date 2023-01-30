package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

public abstract class BaseNestedTarTest {

  protected File referenceTarFile;
  protected File contentsDir;
  protected File tarContentsDir;

  @SneakyThrows
  void initBase() {

    //the expected tar file
    referenceTarFile = new ClassPathResource("tar/reference.tar").getFile();
    assertTrue(referenceTarFile.exists() && referenceTarFile.isFile());

    contentsDir = new ClassPathResource("tar/contents").getFile();
    assertTrue(contentsDir.exists() && contentsDir.isDirectory());

    tarContentsDir = new File(contentsDir, "tarContents");
    makeEmptyDirs(tarContentsDir);
  }

  private void makeEmptyDirs(File tarContentsDir) {

    Stream.of("EMPTY_0/",
        "N1/EMPTY_1/",
        "N1/N2/EMPTY_2/",
        "N1/N2/N3/EMPTY_3/",
        "N1/N2/N3/N4/EMPTY_4/",
        "N1/N2/N3/N4/N5/EMPTY_5/").forEach(child -> {

      File emptyDir = new File(tarContentsDir, child);
      emptyDir.mkdirs();
    });

  }

}
