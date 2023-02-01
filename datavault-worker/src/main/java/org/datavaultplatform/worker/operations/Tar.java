package org.datavaultplatform.worker.operations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * Set of methods to tar a dir / manipulate a tarred dir
 */
public abstract class Tar {

    // this ThreadLocal helps obtain the number of bytes copied into tar during test
    private static final ThreadLocal<Long> COPIED_TO_TAR_TL = ThreadLocal.withInitial(()-> 0L);

    public static boolean createTar( File dir, File file ) throws Exception {
        return createTar(false, dir, file, new TarFileInputStreamFactory(null));
    }

    public static boolean createTarUsingFakeFile( File dir, File file, long fakeFileSize ) throws Exception {
        return createTar(true, dir, file, new TarFileInputStreamFactory(fakeFileSize));
    }

    /**
     * Create a TAR archive of a directory.
     * @param dir The dir to be tarred
     * @param file (optional) The output tar file
     * @param  inputStreamFactory The input stream factory
     * @throws Exception if anything unexpected happens
     * @return true - always
     */
    private static boolean createTar(boolean isTesting, File dir, File file, TarFileInputStreamFactory inputStreamFactory) throws Exception {
        COPIED_TO_TAR_TL.remove();
        try (DatavaultTarArchiveOutputStream tar = new DatavaultTarArchiveOutputStream(getOutputStream(file))) {
            tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFileToTar(isTesting, inputStreamFactory, tar, dir, "");
        }
        return true;
    }

    @SneakyThrows
    private static OutputStream getOutputStream(File file) {
        if (file != null) {
            return new BufferedOutputStream(new FileOutputStream(file));
        } else {
            // helps prevent large tar output files during tests
            return new BlackHoleOutputStream();
        }
    }
    /**
     * Recursively add a file or directory to a TAR archive.
     * @param inputStreamFactory inputStreamFactory
     * @param tar The Tar stream
     * @param f The file to be added
     * @param base The base path
     * @throws Exception if anything unexpected happens
     */
    private static void addFileToTar(boolean isTesting, TarFileInputStreamFactory inputStreamFactory, DatavaultTarArchiveOutputStream tar, File f, String base) throws Exception {
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);

        // This testing hack allows us to FAKE the file sizes of the files going into the tar
        if (isTesting && f.isFile()) {
            tarEntry.setSize(f.length());
        }

        tar.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            try( InputStream in = inputStreamFactory.getInputStream(f)) {
                long copied = IOUtils.copyLarge(in, tar);
                addToCopied(copied);
            }
            tar.closeArchiveEntry();
        } else {
            tar.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTar(isTesting, inputStreamFactory, tar, child, entryName + "/");
                }
            }
        }
    }

    private static synchronized void addToCopied(long addToCopied) {
        long current = COPIED_TO_TAR_TL.get();
        long newCopied = current + addToCopied;
        COPIED_TO_TAR_TL.set(newCopied);
    }

    public static long getCopiedToTar() {
        return COPIED_TO_TAR_TL.get();
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
