package org.datavaultplatform.worker.operations;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.bagish.Checksummer;
import org.datavaultplatform.common.bagish.ManifestWriter;
import org.datavaultplatform.common.bagish.SupportedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A set of methods to package files in a Bagit erm Bag. This is a replacement for Packager that doesn't use the LoC Bagit libs.
 */

public class PackagerV2 {
    private static final Logger log = LoggerFactory.getLogger(PackagerV2.class);
    
    private static final String metadataDirName = "metadata";
    private static final String depositMetaFileName = "deposit.json";
    private static final String vaultMetaFileName = "vault.json";
    private static final String fileTypeMetaFileName = "filetype.json";
    private static final String externalMetaFileName = "external.txt";

    private Checksummer checkSummer;


    public PackagerV2() {
        checkSummer = new Checksummer();
    }
    
    /**
     * Create a bag from an existing directory
     * @param dir The existing directory
     * @throws Exception if anything unexpected happens
     */
    public void createBag(File dir) throws Exception {

        addBagitFile(dir);
        // todo : add a bag-info.txt file?

        createManifest(dir);

    }

    private void addBagitFile(File dir) throws IOException {
        File bagit = new File(dir, "bagit.txt");
        bagit.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(bagit));

        bw.write("BagIt-Version: 0.97");
        bw.newLine();
        bw.write("Tag-File-Character-Encoding: UTF-8");
        bw.newLine();

        bw.close();
    }

    private void createManifest(File dir) throws IOException {
        // Pass the bagDir to the ManifestWriter
        ManifestWriter mf = new ManifestWriter(dir);
        // But start walking at the subdirectory bagDir/data
        Files.walkFileTree(dir.toPath().resolve("data"), mf);
        mf.close();
    }

    /**
     * Add vault/deposit metadata
     * @param bagDir The bag
     * @param depositMetadata The depost metadata
     * @param vaultMetadata The vault metadata
     * @param fileTypeMetadata The file type metadata
     * @param externalMetadata The external metadata
     */
    public void addMetadata(File bagDir,
                                      String depositMetadata,
                                      String vaultMetadata,
                                      String fileTypeMetadata,
                                      String externalMetadata) throws Exception {

            Path bagPath = bagDir.toPath();

            // Create an empty "metadata" directory
            Path metadataDirPath = bagPath.resolve(metadataDirName);
            File metadataDir = metadataDirPath.toFile();
            metadataDir.mkdir();

            File tagManifest = bagPath.resolve("tagmanifest-md5.txt").toFile();
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
     * @return True
     * @throws IOException if an IOException has occurred
     */
    private void addMetaFile(File tagManifest, Path metadataDirPath, String metadataFileName, String metadata, SupportedAlgorithm alg) throws Exception {
        File metadataFile = metadataDirPath.resolve(metadataFileName).toFile();
        FileUtils.writeStringToFile(metadataFile, metadata, StandardCharsets.UTF_8);
        String hash = checkSummer.computeFileHash(metadataFile, alg);
        FileUtils.writeStringToFile(tagManifest, hash + "  " + metadataDirName + "/" + metadataFileName + "\r\n", true);
    }

}
