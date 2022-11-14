package org.datavaultplatform.worker.operations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * Set of methods to tar a dir / manipulate a tarred dir
 */
public class Tar {
    
    /**
     * Create a TAR archive of a directory.
     * @param dir The dir to be tarred
     * @param output The tarred file
     * @return True
     * @throws Exception if anything unexpected happens
     */
    public static boolean createTar(File dir, File output) throws Exception {
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(
            new BufferedOutputStream(new FileOutputStream(output)))) {
            tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFileToTar(tar, dir, "");
        }
        return true;
    }
    
    /**
     * Recursively add a file or directory to a TAR archive.
     * @param tar The Tar stream
     * @param f The file to be added
     * @param base The base path
     * @throws Exception if anything unexpected happens
     */
    private static void addFileToTar(TarArchiveOutputStream tar, File f, String base) throws Exception {
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tar.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            try( FileInputStream in = new FileInputStream(f)) {
                IOUtils.copy(in, tar);
            }
            tar.closeArchiveEntry();
        } else {
            tar.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTar(tar, child, entryName + "/");
                }
            }
        }
    }
    
    /**
     * Extract the contents of a TAR archive to a directory.
     * @param input The tar archive
     * @param outputDir The extract dir
     * @return The top dir of the extract
     * @throws Exception if anything unexpected happens
     */
    public static File unTar(File input, Path outputDir) throws Exception {

        File topDir = null;

        try (TarArchiveInputStream tar = new TarArchiveInputStream(
            new BufferedInputStream(new FileInputStream(input)))) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {

                Path path = outputDir.resolve(entry.getName());
                File entryFile = path.toFile();

                if (entry.isDirectory()) {
                    // Create a directory
                    entryFile.mkdir();

                    if (topDir == null) {
                        topDir = entryFile;
                    }
                } else {
                    // Extract a single file
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        IOUtils.copyLarge(tar, fos, 0, entry.getSize());
                    }
                }
            }
        }

        return topDir;
    }
    
}
