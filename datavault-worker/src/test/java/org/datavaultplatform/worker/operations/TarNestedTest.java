package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class TarNestedTest extends BaseNestedTarTest {

  File tempTarFile;

  @SneakyThrows
  @BeforeEach
  void setup() {
    initBase();

    //the temp file to write the tar to
    tempTarFile = File.createTempFile("tempTarFile", ".tar");
    assertTrue(tempTarFile.exists() && tempTarFile.isFile());
    tempTarFile.deleteOnExit();

  }

  @Test
  @SneakyThrows
  void tarTest() {
    //NOTE : when java compiles resources directory is ignores empty directories - we have to add via code
    //NOTE : you cannot add empty directories to git either.

    //NOTE : when tarring up a directory, there are entries for each non-empty directory too
    boolean created = Tar.createTar(tarContentsDir, tempTarFile);
    assertTrue(created);
    System.out.printf("actual tar [%s]%n", tempTarFile.getAbsolutePath());

    List<TarArchiveEntry> expectedEntries = getEntries(this.referenceTarFile);
    List<TarArchiveEntry> actualEntries = getEntries(this.tempTarFile);

    assertEquals(expectedEntries.size(), actualEntries.size());

    Map<String,Long> expectedEntryMap = expectedEntries.stream()
        .collect(Collectors.toMap(TarArchiveEntry::getName, TarArchiveEntry::getSize));

    Map<String,Long> actualEntryMap = actualEntries.stream()
        .collect(Collectors.toMap(TarArchiveEntry::getName, TarArchiveEntry::getSize));

    assertEquals(expectedEntryMap, actualEntryMap);

    List<String> actualPaths = actualEntries.stream()
        .map(TarArchiveEntry::getName)
        .map(Object::toString)
        .map(this::removeTrailingSlash)
        .sorted()
        .collect(Collectors.toList());

    List<String> expectedPaths = Stream.of(

        "tarContents/",
        "tarContents/.hidden",
        "tarContents/empty.txt",
        "tarContents/1of3.txt",
        "tarContents/2of3.txt",
        "tarContents/3of3.txt",

        "tarContents/JUST_HIDDEN/",
        "tarContents/JUST_HIDDEN/.hidden",

        "tarContents/C0/",
        "tarContents/C0/empty.txt",

        "tarContents/C1/",
        "tarContents/C1/empty.txt",
        "tarContents/C1/1of1.txt",
        "tarContents/C1/.hidden1",

        "tarContents/C2/",
        "tarContents/C2/empty.txt",
        "tarContents/C2/.hidden2",
        "tarContents/C2/1of2.txt",
        "tarContents/C2/2of2.txt",

        "tarContents/C3/",
        "tarContents/C3/empty.txt",
        "tarContents/C3/.hidden3",
        "tarContents/C3/1of3.txt",
        "tarContents/C3/2of3.txt",
        "tarContents/C3/3of3.txt",

        "tarContents/N1/",
        "tarContents/N1/N2/",
        "tarContents/N1/N2/N3/",
        "tarContents/N1/N2/N3/N4/",
        "tarContents/N1/N2/N3/N4/N5/",

        "tarContents/N1/N2/N3/N4/N5/.hidden",
        "tarContents/N1/N2/N3/N4/N5/empty.txt",
        "tarContents/N1/N2/N3/N4/N5/2of3.txt",
        "tarContents/N1/N2/N3/N4/N5/1of3.txt",
        "tarContents/N1/N2/N3/N4/N5/3of3.txt",

        "tarContents/EMPTY_0",
        "tarContents/N1/EMPTY_1",
        "tarContents/N1/N2/EMPTY_2",
        "tarContents/N1/N2/N3/EMPTY_3",
        "tarContents/N1/N2/N3/N4/EMPTY_4",
        "tarContents/N1/N2/N3/N4/N5/EMPTY_5"

    ).map(this::removeTrailingSlash).sorted().collect(Collectors.toList());


    assertEquals(expectedPaths, actualPaths);
  }

  private String removeTrailingSlash(String path) {
    if (path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }

  @SneakyThrows
  private List<TarArchiveEntry> getEntries(File tarFile){
    List<TarArchiveEntry> entries = new ArrayList<>();
    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(tarFile))) {
      TarArchiveEntry entry = tarIn.getNextTarEntry();
      while (entry != null) {
        entries.add(entry);
        entry = tarIn.getNextTarEntry();
      }
    }
    return entries;
  }

}
