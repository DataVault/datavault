package org.datavaultplatform.common.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.FileCopy;
import org.datavaultplatform.common.io.Progress;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Holds functionality that is common to both LocalFileSystem and MultiLocalFileSystem
 */
@Slf4j
public class FileSystemUtils {

    private static final String EMPTY_STRING = "";

    public static Path getAbsolutePath(String filePath, String location) {

        // Join the requested path to the root of the filesystem.
        // In future this path handling should be part of a filesystem-specific driver.
        Path base = Paths.get(location);
        Path absolute;

        try {
            if (filePath.isEmpty()) {
                absolute = base;
            } else {
                // A leading '/' would cause the path to be treated as absolute
                while (filePath.startsWith(File.separator)) {
                    filePath = filePath.replaceFirst(File.separator, EMPTY_STRING);
                }

                absolute = base.resolve(filePath);
                absolute = Paths.get(absolute.toFile().getCanonicalPath());
            }

            if (isValidSubPath(absolute, location)) {
                return absolute;
            } else {
                // Path is invalid (doesn't exist in base)!
                return null;
            }
        } catch (Exception e) {
            log.error("unexpected exception",e);
            return null;
        }
    }

    private static boolean isValidSubPath(Path path, String location) {

        // Check if the path is valid with respect to the base path.
        // For example, we don't want to allow path traversal ("../../abc").

        try {
            Path base = Paths.get(location);
            Path canonicalBase = Paths.get(base.toFile().getCanonicalPath());
            Path canonicalPath = Paths.get(path.toFile().getCanonicalPath());

            return canonicalPath.startsWith(canonicalBase);
        }
        catch (Exception e) {
            log.error("unexpected exception",e);
            return false;
        }
    }

    public static long getUsableSpace(File file) {
        return file.getUsableSpace();
    }

    public static void delete(String path, String location) throws Exception {
        Path absolutePath = getAbsolutePath(path, location);
        Files.deleteIfExists(absolutePath);
    }

    public static String store(String path, File working, Progress progress, String location) throws  Exception{
        Path absolutePath = getAbsolutePath(path, location);
        File retrieveFile = new File(absolutePath.resolve(working.getName()).toUri());

        if (working.isFile()) {
            FileCopy.copyFile(progress, working, retrieveFile);
        } else if (working.isDirectory()) {
            FileCopy.copyDirectory(progress, working, retrieveFile);
        }

        return working.getName();
    }

    public static void retrieve(String path, File working, Progress progress, String location) throws Exception {
        Path absolutePath = getAbsolutePath(path, location);
        File file = absolutePath.toFile();
        if (!file.exists()) {
            throw new RuntimeException(String.format("The file [%s] does not exist", file));
        }

        if (file.isFile()) {
            FileCopy.copyFile(progress, file, working);
        } else if (file.isDirectory()) {
            FileCopy.copyDirectory(progress, file, working);
        }
    }

}
