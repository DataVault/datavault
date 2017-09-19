package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import gov.loc.repository.bagit.*;
import gov.loc.repository.bagit.Manifest.Algorithm;
import java.io.FileNotFoundException;

public class Packager {

    public static final String metadataDirName = "metadata";
    
    public static final String depositMetaFileName = "deposit.json";
    public static final String vaultMetaFileName = "vault.json";
    public static final String fileTypeMetaFileName = "filetype.json";
    public static final String externalMetaFileName = "external.txt";
    
    // Create a bag from an existing directory.
    public static boolean createBag(File dir) throws Exception {

        BagFactory bagFactory = new BagFactory();
        PreBag preBag = bagFactory.createPreBag(dir);
        Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
        
        boolean result = false;
        try {
            result = bag.verifyValid().isSuccess();
        } finally {
            bag.close();
        }
        
        return result;
    }
    
    // Validate an existing bag
    public static boolean validateBag(File dir) throws Exception {

        BagFactory bagFactory = new BagFactory();
        Bag bag = bagFactory.createBag(dir);
        
        boolean result = false;
        try {
            result = bag.verifyValid().isSuccess();
        } finally {
            bag.close();
        }
        
        return result;
    }
    
    // Add vault/deposit metadata
    public static boolean addMetadata(File bagDir,
                                      String depositMetadata,
                                      String vaultMetadata,
                                      String fileTypeMetadata,
                                      String externalMetadata) {
        
        boolean result = false;
        
        try {
            Path bagPath = bagDir.toPath();

            // Create an empty "metadata" directory
            Path metadataDirPath = bagPath.resolve(metadataDirName);
            File metadataDir = metadataDirPath.toFile();
            metadataDir.mkdir();
            
            // TODO: get the manifest file and algorithm config via bagit library?
            File tagManifest = bagPath.resolve("tagmanifest-md5.txt").toFile();
            Algorithm alg = Algorithm.MD5;
            
            // Create metadata files and compute/store hashes
            addMetaFile(tagManifest, metadataDirPath, depositMetaFileName, depositMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, vaultMetaFileName, vaultMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, fileTypeMetaFileName, fileTypeMetadata, alg);
            addMetaFile(tagManifest, metadataDirPath, externalMetaFileName, externalMetadata, alg);
            
            // Metadata files created
            result = true;
            
        } catch (IOException e) {
            System.out.println(e.toString());
            result = false;
        }
        
        return result;
    }
    
    // Add a metadata file to the bag metadata directory
    // Also adds tag information to the tag manifest
    public static boolean addMetaFile(File tagManifest, Path metadataDirPath, String metadataFileName, String metadata, Algorithm alg) throws IOException {
        
        File metadataFile = metadataDirPath.resolve(metadataFileName).toFile();
        FileUtils.writeStringToFile(metadataFile, metadata, StandardCharsets.UTF_8);
        String hash = computeFileHash(metadataFile, alg);
        FileUtils.writeStringToFile(tagManifest, hash + "  " + metadataDirName + "/" + metadataFileName + "\r\n", true);
        
        return true;
    }
    
    // Compute a hash value for file contents
    public static String computeFileHash(File file, Algorithm alg) throws FileNotFoundException, IOException {
        String hash = null;
        FileInputStream fis = new FileInputStream(file);

        if (alg == Algorithm.MD5) {
            hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        } else if (alg == Algorithm.SHA1) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha1Hex(fis);
        } else if (alg == Algorithm.SHA256) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(fis);
        } else if (alg == Algorithm.SHA512) {
            hash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(fis);
        }
        
        fis.close();
        return hash;
    }
    
    // Extract the top-level metadata files from a bag and copy to a new directory.
    public static boolean extractMetadata(File bagDir, File metaDir) {
        
        // TODO: could we use the built-in "holey" bag methods instead?
        
        boolean result = false;
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
            System.out.println(e.toString());
            result = false;
        }
        
        return result;
    }
}
