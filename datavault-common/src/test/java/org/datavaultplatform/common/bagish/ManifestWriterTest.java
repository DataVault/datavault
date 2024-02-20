package org.datavaultplatform.common.bagish;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ManifestWriterTest {

  @TempDir
  File tempDir;

  @Captor
  ArgumentCaptor<SupportedAlgorithm> argAlg;

  @Captor
  ArgumentCaptor<File> argFile;

  @Mock
  Checksummer mSummer;

  @Test
  @SneakyThrows
  void testArgs() {
    IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
        () -> new ManifestWriter(null));
    assertEquals("The directory cannot be null", ex1.getMessage());

    File tempFile = Files.createTempFile(tempDir.toPath(), "test", ".txt").toFile();
    IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
        () -> new ManifestWriter(tempFile));
    assertEquals(String.format("[%s] is not a directory", tempFile), ex2.getMessage());
    tempFile.delete();

    File tempProblemDir = new File(tempDir, "problemDir");
    tempProblemDir.mkdir();
    tempProblemDir.setReadable(false);
    IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
        () -> new ManifestWriter(tempProblemDir));
    assertEquals(String.format("The directory [%s] is not readable", tempProblemDir),
        ex3.getMessage());

    tempProblemDir.setReadable(true);
    tempProblemDir.setWritable(false);
    IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
        () -> new ManifestWriter(tempProblemDir));
    assertEquals(String.format("The directory [%s] is not writable", tempProblemDir),
        ex4.getMessage());

    tempProblemDir.delete();

    assertTrue(tempDir.exists());
    File bagit = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
    bagit.mkdir();
    IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class,
        () -> new ManifestWriter(tempDir));
    assertEquals(String.format("[%s] is not a file", bagit), ex5.getMessage());
  }

  @Test
  void testManifestOfEmptyDirectory() throws IOException {
    File manifestFile = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
    File dataDir = new File(tempDir, "data");
    dataDir.mkdir();
    assertFalse(manifestFile.exists());
    ManifestWriter mw = new ManifestWriter(tempDir);
    Files.walkFileTree(dataDir.toPath(), mw);
    assertTrue(manifestFile.exists());
    assertEquals(0, manifestFile.length());
  }

  @SneakyThrows
  @Test
  void testPlaceManifestFileInManifestDataDirectory() {
    File simpleFile = new File(tempDir, "simple.txt");
    try (FileWriter fw = new FileWriter(simpleFile)) {
      fw.write("This is a test");
    }
    File manifestFile = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
    assertFalse(manifestFile.exists());
    ManifestWriter mw = new ManifestWriter(tempDir);
    IOException ex = assertThrows(IOException.class,
        () -> Files.walkFileTree(tempDir.toPath(), mw));
    assertEquals(String.format("The manifest file [%s] is within a manifest data directory [%s]",
        manifestFile, tempDir), ex.getMessage());
  }

  @SneakyThrows
  @Test
  void testSimpleManifestGeneration() {
    File dataDir = new File(tempDir, "data");
    dataDir.mkdir();
    File simpleFile = new File(dataDir, "simple.txt");
    try (FileWriter fw = new FileWriter(simpleFile)) {
      fw.write("This is a test");
    }
    File manifestFile = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
    assertFalse(manifestFile.exists());

    try (ManifestWriter mw = new ManifestWriter(tempDir)) {
      Files.walkFileTree(dataDir.toPath(), mw);
    }
    assertTrue(manifestFile.exists());

    List<String> lines = IOUtils.readLines(new FileReader(manifestFile));
    assertEquals(1, lines.size());
    assertEquals("ce114e4501d2f4e2dcea3e17b546f339  data/simple.txt", lines.get(0));
  }

  @SneakyThrows
  @Test
  void testNestedFilesManifestGeneration() {
    File dataDir = new File(tempDir, "data");
    dataDir.mkdir();
    Path dataDirPath = dataDir.toPath();

    Mockito.when(mSummer.computeFileHash(argFile.capture(), argAlg.capture())).thenAnswer(
        invocation -> {
          File file = (File) invocation.getArguments()[0];
          return String.format("hash4[%s]", dataDirPath.relativize(file.toPath()));
        });

    createTestFiles(dataDirPath, "zero.txt", "lvl1/one.txt", "lvl1/lvl2/two.txt",
        "lvl1/lvl2/lvl3/three.txt");
    File manifestFile = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
    assertFalse(manifestFile.exists());

    try (ManifestWriter mw = new ManifestWriter(tempDir, mSummer)) {
      Files.walkFileTree(dataDirPath, mw);
    }
    assertTrue(manifestFile.exists());

    List<String> lines = IOUtils.readLines(new FileReader(manifestFile));
    assertEquals(4, lines.size());
    assertTrue(lines.contains("hash4[zero.txt]  data/zero.txt"));
    assertTrue(lines.contains("hash4[lvl1/one.txt]  data/lvl1/one.txt"));
    assertTrue(lines.contains("hash4[lvl1/lvl2/two.txt]  data/lvl1/lvl2/two.txt"));
    assertTrue(lines.contains("hash4[lvl1/lvl2/lvl3/three.txt]  data/lvl1/lvl2/lvl3/three.txt"));

    List<String> filenames = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      File file = argFile.getAllValues().get(i);
      SupportedAlgorithm alg = argAlg.getAllValues().get(i);
      assertEquals(SupportedAlgorithm.MD5, alg);
      Mockito.verify(mSummer).computeFileHash(file, alg);
      filenames.add(file.getAbsolutePath());
    }
    Mockito.verifyNoMoreInteractions(mSummer);
    filenames.sort(Comparator.comparing(String::length));
    assertTrue(filenames.get(0).endsWith("data/zero.txt"));
    assertTrue(filenames.get(1).endsWith("data/lvl1/one.txt"));
    assertTrue(filenames.get(2).endsWith("data/lvl1/lvl2/two.txt"));
    assertTrue(filenames.get(3).endsWith("data/lvl1/lvl2/lvl3/three.txt"));
  }

  @SneakyThrows
  private void createTestFiles(Path dataDirPath, String... testFilePaths) {
    for (String testFilePath : testFilePaths) {
      Path path = dataDirPath.resolve(testFilePath);
      Files.createDirectories(path.getParent());
      File tempFile = Files.createFile(path).toFile();
      try (FileWriter fw = new FileWriter(tempFile)) {
        String contents = "test content for " + tempFile.getName();
        fw.write(contents);
      }
    }
  }
}
