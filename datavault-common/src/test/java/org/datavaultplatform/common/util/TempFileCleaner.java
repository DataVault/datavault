package org.datavaultplatform.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class TempFileCleaner {

    public static final File SLASH_TEMP = new File("/tmp");
    public static final File OS_TEMP = new File(System.getProperty("java.io.tmpdir"));
    private static final String CURRENT_USER_NAME = System.getProperty("user.name");

    public static void cleanTempTestFiles(Instant startTime) {
        cleanSlashTemp(startTime);
        cleanOsTemp(startTime);
    }

    public static void cleanSlashTemp(Instant startTime) {
        cleanTempFilesAndDirectories(SLASH_TEMP, startTime);
    }

    public static boolean isSlashTempSameAsOsTemp() {
        return SLASH_TEMP.equals(OS_TEMP);
    }

    public static void cleanOsTemp(Instant startTime) {
        if (isSlashTempSameAsOsTemp()) {
            log.warn("OS temp directory {} is the same as /tmp, skipping", OS_TEMP);
        } else {
            cleanTempFilesAndDirectories(OS_TEMP, startTime);
        }
    }

    public static void cleanTempFilesAndDirectories(File baseTemp, Instant startTime) {
        cleanTempDirectories(baseTemp, startTime);
        cleanTempFiles(baseTemp, startTime);
    }

    public static void cleanTempDirectories(File baseTemp, Instant startTime) {
        List<File> dirs = findDirectories(baseTemp, startTime);
        for (File dir : dirs) {
            if (!Files.isWritable(dir.toPath())) {
                log.warn("Skipping directory deletion, no write permission: {}", dir.getAbsolutePath());
                continue;
            }
            try {
                FileUtils.deleteDirectory(dir);
                log.info("Deleted temp test directory: {}", dir.getAbsolutePath());
            } catch (Exception ex) {
                log.warn("Failed to delete directory: " + dir.getAbsolutePath(), ex);
            } finally {
                log.info("-");
                
            }
        }
    }

    public static void cleanTempFiles(File baseTemp, Instant startTime) {
        List<File> files = findFiles(baseTemp, startTime);
        for (File file : files) {
            if (!Files.isWritable(file.toPath())) {
                log.warn("Skipping file deletion, no write permission: {}", file.getAbsolutePath());
                continue;
            }
            try {
                var deleted = Files.deleteIfExists(file.toPath());
                log.info("Deleted temp test file?: {} /  {}", file.getAbsolutePath(), deleted);
            } catch (Exception ex) {
                log.warn("Failed to delete file: " + file.getAbsolutePath(), ex);
            } finally {
                log.info("-");
                
            }
        }
    }

    @SneakyThrows
    public static List<File> findDirectories(File baseTemp, Instant startTime) {
        if (baseTemp == null || !baseTemp.exists() || !baseTemp.isDirectory()) {
            return List.of();
        }

        List<File> dirsToDelete;
        try (Stream<Path> stream = Files.list(baseTemp.toPath())) {
            dirsToDelete = stream.filter(Files::isDirectory)
                    .filter(path -> isEligibleForDeletion(path, startTime))
                    .map(Path::toFile)
                    .toList();
        }
        
        dirsToDelete.forEach(path -> {
            log.info("XX Deleting directory: {}", path);
        });
        return dirsToDelete;
    }

    @SneakyThrows
    public static List<File> findFiles(File baseTemp, Instant startTime) {
        if (baseTemp == null || !baseTemp.exists() || !baseTemp.isDirectory()) {
            return List.of();
        }

        List<File> filesToDelete;
        try (Stream<Path> stream = Files.list(baseTemp.toPath())) {
            filesToDelete = stream.filter(Files::isRegularFile)
                    .filter(path -> isEligibleForDeletion(path, startTime))
                    .map(Path::toFile)
                    .toList();
        }

        filesToDelete.forEach(path -> {
            log.info("XX Deleting file: {}", path);
        });

        return filesToDelete;
    }

    private static boolean isEligibleForDeletion(Path path, Instant testStartTime) {
        try {
            // 1. Check ownership
            UserPrincipal owner = Files.getOwner(path);
            if (owner == null || !CURRENT_USER_NAME.equals(owner.getName())) {
                // In Unix/Docker environments, user.name might be '?' or 'root' while owner is a numeric UID.
                // Uncomment the line below to debug ownership mismatches in CI:
                // log.debug("Owner mismatch: Expected {}, but got {}", CURRENT_USER_NAME, owner.getName());
                return false;
            }

            // 2. Check creation/modification time
            // Using BasicFileAttributes is much safer and more robust across Linux/Mac than string lookups
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attr.creationTime();
            FileTime lastModifiedTime = attr.lastModifiedTime();

            Instant created = creationTime.toInstant();
            Instant modified = lastModifiedTime.toInstant();

            // Account for filesystem timestamp truncation (e.g., macOS HFS+/APFS or older Linux filesystems)
            // by giving a small tolerance window.
            Instant threshold = testStartTime.minusSeconds(2);

            // Only delete if the file was created OR modified AFTER the test started
            // (We include 'modified' because some temporary files might be reused or 
            // touched during the test rather than freshly created)
            boolean result =  !created.isBefore(threshold) || !modified.isBefore(threshold);
            if(result) {
                log.info("XX {} isEligibleForDeletion? {}", path, result);
            }
            return result;
        } catch (IOException e) {
            log.debug("Could not read attributes for {}. Assuming not eligible.", path, e);
            return false;
        }
    }
}