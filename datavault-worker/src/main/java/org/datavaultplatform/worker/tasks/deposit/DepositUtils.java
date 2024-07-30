package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

        Assert.isTrue(resolver != null, "The resolver cannot be null");
        Assert.isTrue(userFileStoreProperties != null, "The userFileStoreProperties cannot be null");
        Assert.isTrue(userFileStoreClasses != null, "The userFileStoreClasses cannot be null");

        HashMap<String, UserStore> userStores = new HashMap<>();

        for (String storageID : userFileStoreClasses.keySet()) {
            Assert.isTrue(StringUtils.isNotBlank(storageID), "The storageID[%s] cannot be blank".formatted(storageID));
            String storageClass = userFileStoreClasses.get(storageID);
            Assert.isTrue(StringUtils.isNotBlank(storageClass), "The storage class[%s] for storageID[%s] cannot be blank".formatted(storageClass, storageID));
            try {
                Map<String, String> storageProperties = userFileStoreProperties.get(storageID);

                // Connect to the user storage devices
                UserStore userStore = StorageClassUtils.createStorage(
                        storageClass,
                        storageProperties,
                        UserStore.class,
                        resolver);
                userStores.put(storageID, userStore);
                log.info("Connected to user store: {}, class: {}", storageID, storageClass);
            } catch (RuntimeException ex) {
                String msg = "Deposit failed: could not access UserStore filesystem : " + storageClass;
                throw new RuntimeException(msg, ex);
            }
        }
        return userStores;
    }


    public static HashMap<String, ArchiveStore> setupArchiveStores(StorageClassNameResolver resolver, List<org.datavaultplatform.common.model.ArchiveStore> archiveFileStores) {
        Assert.isTrue(resolver != null, "The resolver cannot be null");
        Assert.isTrue(archiveFileStores != null, "The archiveFileStores cannot be null");
        HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
        // Connect to the archive storage(s). Look out! There are two classes called archiveStore.
        for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores) {
            String storageClass = archiveFileStore.getStorageClass();
            Assert.isTrue(StringUtils.isNotBlank(storageClass), "The storage class[%s] for archiveStoreId[%s] cannot be blank".formatted(storageClass, archiveFileStore.getID()));
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createDir(Path path) {
        Assert.isTrue(path != null, "The path cannot be null");
        File dir = path.toFile();
        dir.mkdirs();
        return dir;
    }

    public static void initialLogging(Context context) {
        Assert.isTrue(context != null, "The context cannot be null");
        log.info("Deposit job - performAction()");
        log.info("chunking: {}", context.isChunkingEnabled());
        log.info("chunks byte size: {}", context.getChunkingByteSize());
        log.info("encryption: {}", context.isEncryptionEnabled());
        log.info("encryption mode: {}", context.getEncryptionMode());
        log.info("validate multiple: {}", context.isMultipleValidationEnabled());
    }

    public static File createTar(Context context, String bagID, File bagDir) throws Exception {
        Assert.isTrue(context != null, "The context cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(bagID), "The bagID cannot be blank");
        Assert.isTrue(bagDir != null, "The bagDir cannot be null");
        File tarFile = getTarFile(context, bagID);
        Tar.createTar(bagDir, tarFile);
        return tarFile;
    }

    public static int getNumberOfChunks(Map<String, String> properties) {
        int numOfChunks = 0;
        if (properties != null && properties.get(PropNames.NUM_OF_CHUNKS) != null) {
            String raw = properties.get(PropNames.NUM_OF_CHUNKS);
            if (StringUtils.isNotBlank(raw)) {
                try {
                    numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
                } catch (Exception ex) {
                    log.warn("problem parsing [{}] to int", raw, ex);
                }
            } else {
                log.warn("number of chunks is blank");
            }
        }
        return numOfChunks;
    }

    public static Class<? extends Event> getFinalPackageEvent(boolean isChunkingEnabled, boolean isEncryptionEnabled) {
        Class<? extends Event> result = ComputedDigest.class;
        if (isChunkingEnabled && !isEncryptionEnabled) {
            result = ComputedChunks.class;
        }
        if (isEncryptionEnabled) {
            result = ComputedEncryption.class;
        }
        log.info("FinalPackageEvent chunking?[{}] encryption?[{}] event[{}]", isChunkingEnabled, isChunkingEnabled, result.getName());
        return result;
    }

    public static File getTarFile(Context context, String bagID) {
        Assert.isTrue(context != null, "The context cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(bagID), "The bagID cannot be blank");
        Path tempDirPath = context.getTempDir();
        Assert.isTrue(tempDirPath != null, "The tempDir cannot be null");
        String tarFileName = bagID + ".tar";
        Path tarPath = context.getTempDir().resolve(tarFileName);
        return tarPath.toFile();
    }

    public static File getChunkTarFile(Context context, String bagID, int chunkNumber) {
        Assert.isTrue(context != null, "The context cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(bagID), "The bagID cannot be blank");
        Path tempDirPath = context.getTempDir();
        Assert.isTrue(tempDirPath != null, "The tempDir cannot be null");

        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
        String tarFileName = bagID + ".tar." + chunkNumber;

        Path tarPath = tempDirPath.resolve(tarFileName);
        return tarPath.toFile();
    }

    public static boolean directoriesExist(List<Path> directories) {
        Assert.isTrue(directories != null, "directories cannot be null");
        boolean result = true;
        for (Path directory : directories) {
            boolean temp = getDirectoryPathChecks().test(directory);
            log.info("directory [{}] exists [{}]", directory, temp);
            result &= temp;
        }
        return result;
    }

    public static boolean filesExist(List<Path> filePaths) {
        Assert.isTrue(filePaths != null, "filePaths cannot be null");
        boolean result = true;
        for (Path filePath : filePaths) {
            boolean temp = getFilePathChecks().test(filePath);
            log.info("file [{}] exists [{}]", filePath, temp);
            result &= temp;
        }
        return result;
    }

    protected static Predicate<Path> getDirectoryPathChecks() {
        Predicate<Path> p1 = Files::exists;
        Predicate<Path> p2 = Files::isDirectory;
        Predicate<Path> p3 = Files::isReadable;
        Predicate<Path> p4 = Files::isWritable;
        return Stream.of(p1, p2, p3, p4).reduce(Predicate::and).get();
    }

    protected static Predicate<Path> getFilePathChecks() {
        Predicate<Path> p1 = Files::exists;
        Predicate<Path> p2 = Files::isRegularFile;
        Predicate<Path> p3 = Files::isReadable;
        return Stream.of(p1, p2, p3).reduce(Predicate::and).get();
    }

}
