package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.bagish.Checksummer;
import org.datavaultplatform.common.bagish.ManifestWriter;
import org.datavaultplatform.common.bagish.SupportedAlgorithm;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.process.ProcessUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

@Slf4j
public class PackagerV2Test {

  public static final String JAVA_TOOL_OPTIONS = "JAVA_TOOL_OPTIONS";
  @TempDir
  File tempDir;

  @SneakyThrows
  void writeContents(File file, String contents) {
    try (FileWriter fw = new FileWriter(file)) {
      fw.write(contents);
    }
  }

  @Nested
  class Metadata {

    @Test
    void testAddMetadata() throws Exception {
      PackagerV2 packer = new PackagerV2();
      File metaDataManifest = new File(tempDir, PackagerV2.TAG_MANIFEST_FILENAME);
      File metaDataDir = new File(tempDir, PackagerV2.metadataDirName);
      File metaDeposit = new File(metaDataDir, PackagerV2.depositMetaFileName);
      File metaVault = new File(metaDataDir, PackagerV2.vaultMetaFileName);
      File metaFileType = new File(metaDataDir, PackagerV2.fileTypeMetaFileName);
      File metaExternal = new File(metaDataDir, PackagerV2.externalMetaFileName);
      assertFalse(metaDataManifest.exists());
      assertFalse(metaDataDir.exists());

      assertFalse(metaDeposit.exists());
      assertFalse(metaVault.exists());
      assertFalse(metaFileType.exists());
      assertFalse(metaExternal.exists());

      packer.addMetadata(tempDir, "depositMD", "vaultMD", "fileTypeMD", "externalMD");
      assertTrue(metaDataManifest.exists());
      assertTrue(metaDataDir.exists());

      assertTrue(metaDeposit.exists());
      assertTrue(metaVault.exists());
      assertTrue(metaFileType.exists());
      assertTrue(metaExternal.exists());

      assertEquals("depositMD", FileUtils.readFileToString(metaDeposit, StandardCharsets.UTF_8));
      assertEquals("vaultMD", FileUtils.readFileToString(metaVault, StandardCharsets.UTF_8));
      assertEquals("fileTypeMD", FileUtils.readFileToString(metaFileType, StandardCharsets.UTF_8));
      assertEquals("externalMD", FileUtils.readFileToString(metaExternal, StandardCharsets.UTF_8));

      List<String> metaManifestLines = FileUtils.readLines(metaDataManifest,
          StandardCharsets.UTF_8);
      assertEquals(4, metaManifestLines.size());

      assertTrue(
          metaManifestLines.contains("f90dad8aa5b98ca5fa5ba206d24fd224  metadata/deposit.json"));
      assertTrue(
          metaManifestLines.contains("7726b4a4397e6c5b03faa40f70ccf8f4  metadata/filetype.json"));
      assertTrue(
          metaManifestLines.contains("50acd4180aed08849702dc61fa5f32e7  metadata/vault.json"));
      assertTrue(
          metaManifestLines.contains("281bfb4f1fba277af51bca8ec0e4953c  metadata/external.txt"));
    }
    @Test
    void testAddMetadataOverwrite() throws Exception {
      PackagerV2 packer = new PackagerV2();
      File metaDataManifest = new File(tempDir, PackagerV2.TAG_MANIFEST_FILENAME);
      File metaDataDir = new File(tempDir, PackagerV2.metadataDirName);
      metaDataDir.mkdir();

      File metaDeposit = new File(metaDataDir, PackagerV2.depositMetaFileName);
      File metaVault = new File(metaDataDir, PackagerV2.vaultMetaFileName);
      File metaFileType = new File(metaDataDir, PackagerV2.fileTypeMetaFileName);
      File metaExternal = new File(metaDataDir, PackagerV2.externalMetaFileName);

      assertFalse(metaDataManifest.exists());
      assertTrue(metaDataDir.exists());

      writeContents(metaDeposit, "md-deposit-contents");
      writeContents(metaVault, "md-vault-contents");
      writeContents(metaFileType, "md-file-type-contents");
      writeContents(metaExternal, "md-external-contents");

      assertTrue(metaDeposit.exists());
      assertTrue(metaVault.exists());
      assertTrue(metaFileType.exists());
      assertTrue(metaExternal.exists());

      packer.addMetadata(tempDir, "depositMD", "vaultMD", "fileTypeMD", "externalMD");
      assertTrue(metaDataManifest.exists());
      assertTrue(metaDataDir.exists());

      assertTrue(metaDeposit.exists());
      assertTrue(metaVault.exists());
      assertTrue(metaFileType.exists());
      assertTrue(metaExternal.exists());

      assertEquals("depositMD", FileUtils.readFileToString(metaDeposit, StandardCharsets.UTF_8));
      assertEquals("vaultMD", FileUtils.readFileToString(metaVault, StandardCharsets.UTF_8));
      assertEquals("fileTypeMD", FileUtils.readFileToString(metaFileType, StandardCharsets.UTF_8));
      assertEquals("externalMD", FileUtils.readFileToString(metaExternal, StandardCharsets.UTF_8));

      List<String> metaManifestLines = FileUtils.readLines(metaDataManifest,
          StandardCharsets.UTF_8);
      assertEquals(4, metaManifestLines.size());

      assertTrue(
          metaManifestLines.contains("f90dad8aa5b98ca5fa5ba206d24fd224  metadata/deposit.json"));
      assertTrue(
          metaManifestLines.contains("7726b4a4397e6c5b03faa40f70ccf8f4  metadata/filetype.json"));
      assertTrue(
          metaManifestLines.contains("50acd4180aed08849702dc61fa5f32e7  metadata/vault.json"));
      assertTrue(
          metaManifestLines.contains("281bfb4f1fba277af51bca8ec0e4953c  metadata/external.txt"));
    }

    @Test
    void testAddNullMetadata() throws Exception {
      PackagerV2 packer = new PackagerV2();
      File metaDataManifest = new File(tempDir, PackagerV2.TAG_MANIFEST_FILENAME);
      File metaDataDir = new File(tempDir, PackagerV2.metadataDirName);
      File metaDeposit = new File(metaDataDir, PackagerV2.depositMetaFileName);
      File metaVault = new File(metaDataDir, PackagerV2.vaultMetaFileName);
      File metaFileType = new File(metaDataDir, PackagerV2.fileTypeMetaFileName);
      File metaExternal = new File(metaDataDir, PackagerV2.externalMetaFileName);
      assertFalse(metaDataManifest.exists());
      assertFalse(metaDataDir.exists());

      assertFalse(metaDeposit.exists());
      assertFalse(metaVault.exists());
      assertFalse(metaFileType.exists());
      assertFalse(metaExternal.exists());

      packer.addMetadata(tempDir, null, null, null, null);
      assertTrue(metaDataManifest.exists());
      assertTrue(metaDataDir.exists());

      assertTrue(metaDeposit.exists());
      assertTrue(metaVault.exists());
      assertTrue(metaFileType.exists());
      assertTrue(metaExternal.exists());

      assertEquals("", FileUtils.readFileToString(metaDeposit, StandardCharsets.UTF_8));
      assertEquals("", FileUtils.readFileToString(metaVault, StandardCharsets.UTF_8));
      assertEquals("", FileUtils.readFileToString(metaFileType, StandardCharsets.UTF_8));
      assertEquals("", FileUtils.readFileToString(metaExternal, StandardCharsets.UTF_8));

      List<String> metaManifestLines = FileUtils.readLines(metaDataManifest,
          StandardCharsets.UTF_8);
      assertEquals(4, metaManifestLines.size());

      assertTrue(
          metaManifestLines.contains("d41d8cd98f00b204e9800998ecf8427e  metadata/deposit.json"));
      assertTrue(
          metaManifestLines.contains("d41d8cd98f00b204e9800998ecf8427e  metadata/filetype.json"));
      assertTrue(
          metaManifestLines.contains("d41d8cd98f00b204e9800998ecf8427e  metadata/vault.json"));
      assertTrue(
          metaManifestLines.contains("d41d8cd98f00b204e9800998ecf8427e  metadata/external.txt"));
    }

    @Test
    void testAddMetadataArgs() throws Exception {
      PackagerV2 packer = new PackagerV2();

      IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(null, "", "", "", ""));
      assertEquals("The directory cannot be null", ex1.getMessage());

      File nonExistent = new File(tempDir, "nonExistent");
      IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(nonExistent, "", "", "", ""));
      assertEquals(String.format("The directory [%s] does not exist", nonExistent),
          ex2.getMessage());

      File temp = new File(tempDir, "temp");
      temp.createNewFile();
      IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(temp, "", "", "", ""));
      assertEquals(String.format("[%s] is not a directory", temp), ex3.getMessage());

      tempDir.setReadable(false);
      tempDir.setWritable(false);
      IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The directory [%s] is not readable", tempDir), ex4.getMessage());

      tempDir.setReadable(true);
      tempDir.setWritable(false);
      IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The directory [%s] is not writable", tempDir), ex5.getMessage());

      tempDir.setWritable(true);
      File metadata = new File(tempDir, "metadata");
      metadata.createNewFile();
      IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("[%s] is not a directory", metadata), ex6.getMessage());

      metadata.delete();
      metadata = new File(tempDir, "metadata");
      metadata.mkdir();
      metadata.setReadable(false);
      metadata.setWritable(false);
      IllegalArgumentException ex7 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The directory [%s] is not readable", metadata), ex7.getMessage());

      metadata.setReadable(true);
      IllegalArgumentException ex8 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The directory [%s] is not writable", metadata), ex8.getMessage());

      metadata.setWritable(true);

      File tagManifest = new File(tempDir, PackagerV2.TAG_MANIFEST_FILENAME);
      tagManifest.mkdir();
      IllegalArgumentException ex9 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("[%s] is not a file", tagManifest), ex9.getMessage());

      tagManifest.delete();

      tagManifest = new File(tempDir, PackagerV2.TAG_MANIFEST_FILENAME);
      tagManifest.createNewFile();
      tagManifest.setReadable(false);
      tagManifest.setWritable(false);
      IllegalArgumentException ex10 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The file [%s] is not readable", tagManifest), ex10.getMessage());

      tagManifest.setReadable(true);
      IllegalArgumentException ex11 = assertThrows(IllegalArgumentException.class,
          () -> packer.addMetadata(tempDir, "", "", "", ""));
      assertEquals(String.format("The file [%s] is not writable", tagManifest), ex11.getMessage());
    }

  }

  @Nested
  class CreateBag {

    @Test
    void testCreateBagArgs() throws IOException {
      PackagerV2 packer = new PackagerV2();
      IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(null));
      assertEquals("The directory cannot be null", ex1.getMessage());

      File nonExistent = new File(tempDir, "doesNotExist.txt");
      IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(nonExistent));
      assertEquals(String.format("The directory [%s] does not exist", nonExistent),
          ex2.getMessage());

      File testFile = new File(tempDir, "testFile.txt");
      testFile.createNewFile();
      IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testFile));
      assertEquals(String.format("[%s] is not a directory", testFile), ex3.getMessage());

      File testDir = new File(tempDir, "testDir1");
      testDir.mkdir();
      testDir.setReadable(false);
      testDir.setWritable(false);

      IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testDir));
      assertEquals(String.format("The directory [%s] is not readable", testDir), ex4.getMessage());

      testDir.setReadable(true);

      IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testDir));
      assertEquals(String.format("The directory [%s] is not writable", testDir), ex5.getMessage());

      testDir.setWritable(true);
      IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testDir));
      assertEquals(String.format("The directory [%s/data] does not exist", testDir),
          ex6.getMessage());

      File testDataDir = new File(testDir, "data");
      testDataDir.mkdir();
      testDataDir.setReadable(false);

      IllegalArgumentException ex7 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testDir));
      assertEquals(String.format("The directory [%s/data] is not readable", testDir),
          ex7.getMessage());

      testDataDir.delete();
      testDataDir = new File(testDir, "data");
      testDataDir.createNewFile();

      IllegalArgumentException ex8 = assertThrows(IllegalArgumentException.class,
          () -> packer.createBag(testDir));
      assertEquals(String.format("[%s/data] is not a directory", testDir),
          ex8.getMessage());
    }

    @Test
    @SneakyThrows
    void testCreateBag() {
      File dataDir = new File(tempDir, PackagerV2.DATA_DIR_NAME);
      dataDir.mkdir();

      File data1 = new File(dataDir, "one.txt");
      writeContents(data1, "one 1");

      File data2 = new File(dataDir, "two.txt");
      writeContents(data2, "two 2");

      File data3 = new File(dataDir, "three.txt");
      writeContents(data3, "three 3");

      assertTrue(data1.exists());
      assertTrue(data2.exists());
      assertTrue(data3.exists());

      File manifest = new File(tempDir, ManifestWriter.MANIFEST_FILE_NAME);
      assertFalse(manifest.exists());

      Checksummer mChecksummer = Mockito.mock(Checksummer.class);
      Mockito.when(mChecksummer.computeFileHash(data1, SupportedAlgorithm.MD5))
          .thenReturn("hash-1");
      Mockito.when(mChecksummer.computeFileHash(data2, SupportedAlgorithm.MD5))
          .thenReturn("hash-2");
      Mockito.when(mChecksummer.computeFileHash(data3, SupportedAlgorithm.MD5))
          .thenReturn("hash-3");

      File bagitFile = new File(tempDir, PackagerV2.BAGIT_FILE_NAME);
      assertFalse(bagitFile.exists());

      PackagerV2 packer = new PackagerV2(mChecksummer);

      packer.createBag(tempDir);
      assertTrue(manifest.exists());

      List<String> manifestLines = FileUtils.readLines(manifest, StandardCharsets.UTF_8);
      assertEquals(3, manifestLines.size());

      assertTrue(manifestLines.contains("hash-1  data/one.txt"));
      assertTrue(manifestLines.contains("hash-2  data/two.txt"));
      assertTrue(manifestLines.contains("hash-3  data/three.txt"));

      Mockito.verify(mChecksummer).computeFileHash(data1, SupportedAlgorithm.MD5);
      Mockito.verify(mChecksummer).computeFileHash(data2, SupportedAlgorithm.MD5);
      Mockito.verify(mChecksummer).computeFileHash(data3, SupportedAlgorithm.MD5);

      Mockito.verifyNoMoreInteractions(mChecksummer);

      List<String> bagitLines = FileUtils.readLines(bagitFile, StandardCharsets.UTF_8);
      assertEquals(2, bagitLines.size());
      assertEquals("BagIt-Version: 0.97", bagitLines.get(0));
      assertEquals("Tag-File-Character-Encoding: UTF-8", bagitLines.get(1));

    }

    @Nested
    class PackagerV2AsProcess {

      File dataDir;
      File file1;
      File file2;
      File file3;

      File bagit;
      File manifest;

      @BeforeEach
      void setup() {
        dataDir = new File(tempDir, PackagerV2.DATA_DIR_NAME);
        dataDir.mkdir();
        file1 = new File(dataDir, "one.txt");
        file2 = new File(dataDir, "two.txt");
        file3 = new File(dataDir, "three.txt");
        bagit = new File(tempDir, "bagit.txt");
        manifest = new File(tempDir, "manifest-md5.txt");
        writeContents(file1, "one 1");
        writeContents(file2, "two 2");
        writeContents(file3, "three 3");

        assertFalse(bagit.exists());
        assertFalse(manifest.exists());
      }

      @Test
      @SneakyThrows
      void testPackagerV2AsProcessSuccess() {
        ProcessBuilder builder = ProcessUtils.exec(PackagerV2.class, Collections.emptyList(), List.of(tempDir.getCanonicalPath()));
        builder.environment().remove(JAVA_TOOL_OPTIONS);
        Process p  = builder.start();
        int status = p.waitFor();
        String output = String.join("", IOUtils.readLines(p.getInputStream(), StandardCharsets.UTF_8));
        String error = String.join("", IOUtils.readLines(p.getErrorStream(), StandardCharsets.UTF_8));
        log.info("output[{}]", output);
        log.info("error[{}]", error);
        assertEquals(0, status);
        assertTrue(bagit.exists());
        assertTrue(manifest.exists());
        assertTrue(StringUtils.isBlank(error));
        assertTrue(output.contains("added manifest file"));
        assertTrue(output.contains("added bagit file"));
      }

      @Test
      @SneakyThrows
      void testPackagerV2AsProcessFail() {
        ProcessBuilder builder = ProcessUtils.exec(PackagerV2.class, Collections.emptyList(), List.of(dataDir.getCanonicalPath()));
        builder.environment().remove(JAVA_TOOL_OPTIONS);
        Process p  = builder.start();
        int status = p.waitFor();
        String output = String.join("", IOUtils.readLines(p.getInputStream(), StandardCharsets.UTF_8));
        String error = String.join("", IOUtils.readLines(p.getErrorStream(), StandardCharsets.UTF_8));
        log.info("output[{}]", output);
        log.info("error[{}]", error);
        assertNotEquals(0, status);
        assertFalse(bagit.exists());
        assertFalse(manifest.exists());
        assertFalse(StringUtils.isBlank(error));
        assertFalse(output.contains("added manifest file"));
        assertFalse(output.contains("added bagit file"));

        String message = String.format("java.lang.IllegalArgumentException: The directory [%s/data] does not exist", dataDir.getCanonicalPath());
        assertTrue(error.contains(message));
      }
    }
  }
}
