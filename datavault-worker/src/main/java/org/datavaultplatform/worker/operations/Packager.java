package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.CorruptChecksumException;
import gov.loc.repository.bagit.exceptions.FileNotInPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.InvalidBagitFileFormatException;
import gov.loc.repository.bagit.exceptions.MaliciousPathException;
import gov.loc.repository.bagit.exceptions.MissingBagitFileException;
import gov.loc.repository.bagit.exceptions.MissingPayloadDirectoryException;
import gov.loc.repository.bagit.exceptions.MissingPayloadManifestException;
import gov.loc.repository.bagit.exceptions.UnsupportedAlgorithmException;
import gov.loc.repository.bagit.exceptions.VerificationException;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;

/**
 * A set of methods to package files in a Bagit erm Bag
 */
public class Packager {
    private static final Logger log = LoggerFactory.getLogger(Packager.class);
    
    public static final String metadataDirName = "metadata";
    
    public static final String depositMetaFileName = "deposit.json";
    public static final String vaultMetaFileName = "vault.json";
    public static final String fileTypeMetaFileName = "filetype.json";
    public static final String externalMetaFileName = "external.txt";
    
    /**
     * Create a bag from an existing directory
     * @param dir The existing directory 
     * @return Boolean stating whether the created bag is valid
     * @throws Exception if anything unexpected happens
     */
    public static Bag createBag(File dir) throws Exception {        
        return BagCreator.bagInPlace(
                dir.toPath(),
            Collections.singletonList(StandardSupportedAlgorithms.MD5),
                true); // include hidden files
    }
    
    /**
     * A bag is invalid if a defined exception is thrown in the isValid method
     * of BagVerifier.
     * @param dir A bagit dir
     * @return True if bag is valid.
     * @throws Exception if anything unexpected happens
     */
    public static boolean validateBag(File dir) throws Exception {
        boolean isValid = false;
        Bag bag = new BagReader().read(dir.toPath());

        try (BagVerifier bv = new BagVerifier()) {
            bv.isValid(bag, false);
            isValid = true;
        } catch (IOException | CorruptChecksumException | InvalidBagitFileFormatException |
                 VerificationException | InterruptedException | MaliciousPathException |
                 MissingPayloadManifestException | MissingPayloadDirectoryException |
                 FileNotInPayloadDirectoryException | MissingBagitFileException |
                 UnsupportedAlgorithmException ex) {
            log.warn("Bag " + bag.getRootDir() + " is invalid: " + ex.getMessage());
        }
        
        return isValid;
    }
    
    /**
     * Add vault/deposit metadata
     * @param bagDir The bag
     * @param depositMetadata The depost metadata
     * @param vaultMetadata The vault metadata
     * @param fileTypeMetadata The file type metadata
     * @param externalMetadata The external metadata
     * @return True if the metadata is added without any exception
     */
    public static boolean addMetadata(File bagDir,
                                      String depositMetadata,
                                      String vaultMetadata,
                                      String fileTypeMetadata,
                                      String externalMetadata) {
        
        boolean result;
        
        try {
            Path bagPath = bagDir.toPath();

            // Create an empty "metadata" directory
            Path metadataDirPath = bagPath.resolve(metadataDirName);
            File metadataDir = metadataDirPath.toFile();
            metadataDir.mkdir();
            
            // TODO: get the manifest file and algorithm config via bagit library?
            File tagManifest = bagPath.resolve("tagmanifest-md5.txt").toFile();
            SupportedAlgorithm alg = StandardSupportedAlgorithms.MD5;
            
            // Create metadata files and compute/store hashes
            addMetaFile(tagManifest, metadataDirPath, depositMetaFileName, depositMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, vaultMetaFileName, vaultMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, fileTypeMetaFileName, fileTypeMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, externalMetaFileName, externalMetadata, alg);
            
            // Metadata files created
            result = true;
            
        } catch (IOException e) {
            log.error("unexpected exception", e);
            result = false;
        }
        
        return result;
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
    public static boolean addMetaFile(File tagManifest, Path metadataDirPath, String metadataFileName, String metadata, SupportedAlgorithm alg) throws IOException {
        
        File metadataFile = metadataDirPath.resolve(metadataFileName).toFile();
        FileUtils.writeStringToFile(metadataFile, metadata, StandardCharsets.UTF_8);
        String hash = computeFileHash(metadataFile, alg);
        FileUtils.writeStringToFile(tagManifest, hash + "  " + metadataDirName + "/" + metadataFileName + "\r\n", StandardCharsets.UTF_8, true);
        
        return true;
    }
    
    /**
     * Compute a hash value for file contents
     * @param file The file we want to hash
     * @param alg The algorithm we wnat to use
     * @return The hash value as a string
     * @throws FileNotFoundException if the file isn't found!
     * @throws IOException if we run into any IO issues
     */
    public static String computeFileHash(File file, SupportedAlgorithm alg) throws FileNotFoundException, IOException {
        String hash = null;
        FileInputStream fis = new FileInputStream(file);

        if (alg == StandardSupportedAlgorithms.MD5) {
            hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        } else if (alg == StandardSupportedAlgorithms.SHA1) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha1Hex(fis);
        } else if (alg == StandardSupportedAlgorithms.SHA256) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(fis);
        } else if (alg == StandardSupportedAlgorithms.SHA512) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(fis);
        }
        
        fis.close();
        return hash;
    }
    
    /**
     * Extract the top-level metadata files from a bag and copy to a new directory.
     * @param bagDir The bad dir
     * @param metaDir The new meta data dir
     * @return True if all files are copied and false if not
     */
    public static boolean extractMetadata(File bagDir, File metaDir) {
        
        // TODO: could we use the built-in "holey" bag methods instead?
        
        boolean result;
        Path bagPath = bagDir.toPath();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(bagPath)) {
            for (Path entry : stream) {
                
                /*
                Expected:
                - data (dir)
                - bag-info.txt
                - bagit.txt
                - manifest-md5.txt
                - tagmanifest-md5.txt
                - other metadata files or directories
                */
                
                String entryFileName = entry.getFileName().toString();

                if (Files.isDirectory(entry)) {
                    // Handle directories
                    if (entryFileName.equals("data")) {
                        // Create an empty "data" directory
                        Path metaDirPath = Paths.get(metaDir.toURI());
                        File emptyDataDir = metaDirPath.resolve("data").toFile();
                        emptyDataDir.mkdir();
                    } else {
                        FileUtils.copyDirectoryToDirectory(entry.toFile(), metaDir);
                    }
                
                } else if (!Files.isDirectory(entry)) {
                    // Handle files
                    FileUtils.copyFileToDirectory(entry.toFile(), metaDir);
                }
            }
            
            // All files copied
            result = true;
            
        } catch (IOException e) {
            log.error("unexpected exception", e);
            result = false;
        }
        
        return result;
    }
    
    /**
     * Execute packager as a command-line process.
     * @param args Expects a string directory argument.
     */
    public static void main(String[] args) {
        int status = -1;
        if (args.length == 1) {
            File dir = new File(args[0]);
            if(dir.isDirectory()) {
                try {
                    if(Packager.createBag(dir) != null){
                        status = 0;
                    }
                }
                catch(Exception ex) {
                    log.error("Unexpected Exception", ex);
                }
            }
            else {
                log.info("Packager expects a directory as an argument");
            }
        }
        else {
            log.info("Packager expects a single directory argument");
        }
        
        System.exit(status); 
    }
}
