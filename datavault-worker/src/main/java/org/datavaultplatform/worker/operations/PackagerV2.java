package org.datavaultplatform.worker.operations;


import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.bagish.Checksummer;
import org.datavaultplatform.common.bagish.ManifestWriter;
import org.datavaultplatform.common.bagish.SupportedAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A set of methods to package files in a Bagit erm Bag. This is a replacement for Packager that doesn't use the LoC Bagit libs.
 */

@Slf4j
public class PackagerV2 {

  public static final String metadataDirName = "metadata";
  public static final String depositMetaFileName = "deposit.json";
  public static final String vaultMetaFileName = "vault.json";
  public static final String fileTypeMetaFileName = "filetype.json";
  public static final String externalMetaFileName = "external.txt";
  public static final String TAG_MANIFEST_FILENAME = "tagmanifest-md5.txt";
  public static final String DATA_DIR_NAME = "data";
  public static final String BAGIT_FILE_NAME = "bagit.txt";

  private final Checksummer checkSummer;

    public PackagerV2(Checksummer checkSummer) {
      this.checkSummer = checkSummer;
    }

    public PackagerV2() {
      this(new Checksummer());
    }

    /**
     * Create a bag from an existing directory
     * @param bagDir The existing directory
     * @throws Exception if anything unexpected happens
     */
    public void createBag(File bagDir) throws Exception {
        FileUtils.checkDirectoryExists(bagDir);
        addBagitFile(bagDir);
        // todo : add a bag-info.txt file?

        createManifest(bagDir);

    }

    private void addBagitFile(File bagDir) throws IOException {
        File bagit = new File(bagDir, BAGIT_FILE_NAME);
        bagit.createNewFile();

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(bagit))){
          bw.write("BagIt-Version: 0.97");
          bw.newLine();
          bw.write("Tag-File-Character-Encoding: UTF-8");
          bw.newLine();
        }
    }

    private void createManifest(File bagDir) throws IOException {
        // Pass the bagDir to the ManifestWriter
        File dataDir = new File(bagDir, DATA_DIR_NAME);
        FileUtils.checkDirectoryExists(dataDir);

        try(ManifestWriter mf = new ManifestWriter(bagDir, checkSummer)) {
          // Start walking at the subdirectory <bagDir>/data
          Files.walkFileTree(dataDir.toPath(), mf);
        }
      }

    /**
     * Add vault/deposit metadata
     * @param bagDir The bag
     * @param depositMetadata The deposit metadata
     * @param vaultMetadata The vault metadata
     * @param fileTypeMetadata The file type metadata
     * @param externalMetadata The external metadata
     */
    public void addMetadata(File bagDir,
                                      String depositMetadata,
                                      String vaultMetadata,
                                      String fileTypeMetadata,
                                      String externalMetadata) throws Exception {


      FileUtils.checkDirectoryExists(bagDir);

      Path bagPath = bagDir.toPath();

      // Create an empty "metadata" directory
      Path metadataDirPath = bagPath.resolve(metadataDirName);
      File metadataDir = metadataDirPath.toFile();
      FileUtils.checkDirectoryExists(metadataDir, true);

      File tagManifest = bagPath.resolve(TAG_MANIFEST_FILENAME).toFile();
      FileUtils.checkFileExists(tagManifest, true);

      SupportedAlgorithm alg = SupportedAlgorithm.MD5;

      // Create metadata files and compute/store hashes
      addMetaFile(tagManifest, metadataDirPath, depositMetaFileName, depositMetadata, alg);
      addMetaFile(tagManifest, metadataDirPath, vaultMetaFileName, vaultMetadata, alg);
      addMetaFile(tagManifest, metadataDirPath, fileTypeMetaFileName, fileTypeMetadata, alg);
      addMetaFile(tagManifest, metadataDirPath, externalMetaFileName, externalMetadata, alg);
    }

    /**
     * Add a metadata file to the bag metadata directory
     * Also adds tag information to the tag manifest
     * @param tagManifest The tag manifest file
     * @param metadataDirPath The path to the meta data dir
     * @param metadataFileName The metadata file name
     * @param metadata The meta data
     * @param alg The algorithm applied to the bag
     * @throws IOException if an IOException has occurred
     */
    private void addMetaFile(File tagManifest, Path metadataDirPath, String metadataFileName, String metadata, SupportedAlgorithm alg) throws Exception {
      // write metadata file
      File metadataFile = metadataDirPath.resolve(metadataFileName).toFile();
      if (metadataFile.exists() && metadataFile.length() > 0) {
        log.warn("Overwriting metadata file [{}]", metadataFile);
      }
      org.apache.commons.io.FileUtils.writeStringToFile(metadataFile, metadata, StandardCharsets.UTF_8);

      // write entry for metadata file in metadata-manifest file
      String hash = checkSummer.computeFileHash(metadataFile, alg);
      String lineAndNewLine = String.format("%s  %s%s%s%n", hash, metadataDirName, File.separator, metadataFileName);
      org.apache.commons.io.FileUtils.writeStringToFile(tagManifest, lineAndNewLine, StandardCharsets.UTF_8, true);
    }

  /**
   * Execute packager as a command-line process.
   * @param args Expects a string directory argument.
   */
  public static void main(String[] args) {
    int status = -1;
    if (args.length == 1) {
      try {
        File dir = new File(args[0]);
        System.out.printf("Attempting to create bag files for directory [%s]%n", dir);
        PackagerV2 packagerV2 = new PackagerV2();
        packagerV2.createBag(dir);
        status = 0;
      } catch (Exception ex) {
        ex.printStackTrace(System.err);
      }
    } else {
      System.out.println("Packager expects a single directory argument");
    }

    System.exit(status);
  }
}
