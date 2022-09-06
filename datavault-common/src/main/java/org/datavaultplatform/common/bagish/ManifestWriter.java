package org.datavaultplatform.common.bagish;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.io.FileUtils;

import static java.nio.file.FileVisitResult.CONTINUE;

@Slf4j
public class ManifestWriter extends SimpleFileVisitor<Path> implements AutoCloseable {

    public static final String MANIFEST_FILE_NAME = "manifest-md5.txt";
    private final BufferedWriter bw;
    private final int bagPathLength;
    private final Checksummer checkSummer;
    private final Path manifestFilePath;

    private final Path manifestParentPath;
    private final File manifest;

    public ManifestWriter(File bagDir, Checksummer checkSummer) throws IOException {
        FileUtils.checkDirectoryExists(bagDir);

        bagPathLength = bagDir.toPath().toString().length();

        // todo: change so that 'md5' is a variable representing the checksum type.
        manifest = new File(bagDir, MANIFEST_FILE_NAME);
        FileUtils.checkFileExists(manifest, true);
        this.manifestFilePath = manifest.toPath();
        this.manifestParentPath = manifestFilePath.getParent();

        bw = new BufferedWriter(new FileWriter(manifest));

        this.checkSummer = checkSummer;
    }

    public File getManifest() {
        return manifest;
    }

    public ManifestWriter(File bagDir) throws IOException {
        this(bagDir, new Checksummer());
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attr) throws IOException {
        if (manifestParentPath.equals(dirPath)) {
            throw new IOException(
                String.format("The manifest file [%s] is within a manifest data directory [%s]",
                    manifestFilePath, dirPath));
        }
        return super.preVisitDirectory(dirPath, attr);
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attr) throws IOException {

        /*
           So the following code was cribbed from the Oracle Java docs, but it didn't work as I had expected.
           Symlinks are not being recognised as symlinks. I think I could have fixed that by passing some options
           in the walkFileTree call, but I decided that it was useful to have them listed in the manifest file.
           That's my excuse anyway :) .
         */

        if (attr.isSymbolicLink()) {
            //logger.info("Symbolic link: " + file.toString());

        } else if (attr.isRegularFile()) {
            //logger.info("Regular file: " + file.toString());

            try {
                String hash = checkSummer.computeFileHash(filePath.toFile(), SupportedAlgorithm.MD5);
                // We don't want the full path, so strip off all the path in front of 'data'
                bw.write(hash + "  " + filePath.toString().substring(bagPathLength + 1));
                bw.newLine();
            } catch (Exception e) {
                // Just rethrowing as an IOException so as to not break the Interface contract.
                throw new IOException(e);
            }


        } else {
            //logger.info("Other: " + file.toString());
        }

        return CONTINUE;
    }

    @Override
    public void close() throws IOException {
        if (bw != null) {
            bw.flush();
        }
        IOUtils.close(bw);
    }
}
