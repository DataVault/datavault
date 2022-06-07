package org.datavaultplatform.common.bagish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.*;

public class ManifestWriter extends SimpleFileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(ManifestWriter.class);

    private FileWriter fw;
    private BufferedWriter bw;
    private int bagPathLength;
    private Checksummer checkSummer;



    public ManifestWriter(File bagDir) throws IOException {
        bagPathLength = bagDir.toPath().toString().length();

        // todo: change so that 'md5' is a variable representing the checksum type.
        File manifest = new File(bagDir, "manifest-md5.txt");
        manifest.createNewFile();

        fw = new FileWriter(manifest);
        bw = new BufferedWriter(fw);

        checkSummer = new Checksummer();

    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
        String hash;

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
                hash = checkSummer.computeFileHash(file.toFile(), SupportedAlgorithm.MD5);
            } catch (Exception e) {
                // Just rethrowing as an IOException so as to not break the Interface contract.
                throw new IOException(e);
            }

            // We don't want the full path, so strip off all the path in front of 'data'
            bw.write(hash + "  " + file.toString().substring(bagPathLength + 1));
            bw.newLine();

        } else {
            //logger.info("Other: " + file.toString());
        }

        return CONTINUE;
    }

    public void close() throws IOException {
        bw.close();
    }



}
