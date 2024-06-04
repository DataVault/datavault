package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.Tar;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public abstract class DepositUtils {

    public static HashMap<String, UserStore> setupUserFileStores(StorageClassNameResolver resolver,
                                                                 Map<String, Map<String, String>> userFileStoreProperties,
                                                                 Map<String, String> userFileStoreClasses) {

        HashMap<String, UserStore> userStores = new HashMap<>();

        for (String storageID : userFileStoreClasses.keySet()) {
            String storageClass = userFileStoreClasses.get(storageID);
            try {
                Map<String, String> storageProperties = userFileStoreProperties.get(storageID);

                // Connect to the user storage devices
                UserStore userStore = StorageClassUtils.createStorage(
                        storageClass,
                        storageProperties,
                        UserStore.class,
                        resolver);
                userStores.put(storageID, userStore);
                log.info("Connected to user store: " + storageID + ", class: " + storageClass);
            } catch (RuntimeException ex) {
                String msg = "Deposit failed: could not access UserStore filesystem : " + storageClass;
                throw new RuntimeException(msg, ex);
            }
        }
        return userStores;
    }


    public static HashMap<String, ArchiveStore> setupArchiveStores(StorageClassNameResolver resolver, List<org.datavaultplatform.common.model.ArchiveStore> archiveFileStores) {
        HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
        // Connect to the archive storage(s). Look out! There are two classes called archiveStore.
        for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores) {
            String storageClass = archiveFileStore.getStorageClass();
            try {
                Map<String, String> storageProperties = archiveFileStore.getProperties();
                ArchiveStore archiveStore = StorageClassUtils.createStorage(
                        storageClass,
                        storageProperties,
                        ArchiveStore.class,
                        resolver);
                archiveStores.put(archiveFileStore.getID(), archiveStore);
            } catch (RuntimeException ex) {
                String msg = "Deposit failed: could not access ArchiveStore filesystem : " + storageClass;
                throw new RuntimeException(msg, ex);
            }
        }
        return archiveStores;
    }

    public static File createDir(Path path) {
        File dir = path.toFile();
        dir.mkdirs();
        return dir;
    }

    public static void initialLogging(Context context) {
        log.info("Deposit job - performAction()");
        log.info("chunking: "+context.isChunkingEnabled());
        log.info("chunks byte size: "+context.getChunkingByteSize());
        log.info("encryption: "+context.isEncryptionEnabled());
        log.info("encryption mode: "+context.getEncryptionMode());
        log.info("validate multiple: " + context.isMultipleValidationEnabled());
    }

    public static File createTar(Context context, String bagID, File bagDir) throws Exception {
        File tarFile = getTarFile(context, bagID);
        Tar.createTar(bagDir, tarFile);
        return tarFile;
    }
    
    public static int getNumberOfChunks(Map<String,String> properties){
        int numOfChunks =  0;
        if (properties.get(PropNames.NUM_OF_CHUNKS) != null) {
            numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
        }
        return numOfChunks;
    }

    public static Class<? extends Event> getFinalPackageEvent(boolean isChunkingEnabled, boolean isEncryptionEnabled){
        Class<? extends Event> result = ComputedDigest.class;
        if (isChunkingEnabled && !isEncryptionEnabled) {
            result = ComputedChunks.class;
        }
        if (isEncryptionEnabled) {
            result = ComputedEncryption.class;
        }
        log.info("FinalPackageEvent chunking?[{}] encryption?[{}] event[{}]",isChunkingEnabled, isChunkingEnabled, result.getName());
        return result;
    }

    public static File getTarFile(Context context, String bagID) {
        String tarFileName = bagID + ".tar";
        Path tarPath = context.getTempDir().resolve(tarFileName);
        return tarPath.toFile();
    }
    
    /** TODO - gotta check this **/
    public static File getChunkTarFile(Context context, String bagID, int chunkNumber) {
        String tarFileName = bagID + ".tar" + chunkNumber;
        Path tarPath = context.getTempDir().resolve(tarFileName);
        return tarPath.toFile();
    }

    public static boolean directoriesExist(List<Path> directories) {
        Assert.isTrue(directories != null, "directories cannot be null");
        return directories.stream().allMatch(getDirectoryPathChecks());
    }

    public static boolean filesExist(List<Path> files) {
        Assert.isTrue(files != null, "files cannot be null");
        return files.stream().allMatch(getFilePathChecks());
    }

    protected static Predicate<Path> getDirectoryPathChecks() {
        Predicate<Path> p1 = Files::exists;
        Predicate<Path> p2 = Files::isDirectory;
        Predicate<Path> p3 = Files::isReadable;
        Predicate<Path> p4 = Files::isWritable;
        return Stream.of(p1,p2,p3,p4).reduce(Predicate::and).get();
    }
    
    protected static Predicate<Path> getFilePathChecks() {
        Predicate<Path> p1 = Files::exists;
        Predicate<Path> p2 = Files::isRegularFile;
        Predicate<Path> p3 = Files::isReadable;
        return Stream.of(p1,p2,p3).reduce(Predicate::and).get();
    }

}
