package org.datavault.worker.operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

public class Tar {
    
    // Create a TAR archive of a directory.
    public static boolean createTar(File dir, File output) throws Exception {

        FileOutputStream fos = new FileOutputStream(output);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        TarArchiveOutputStream tar = new TarArchiveOutputStream(bos);
        tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        addFileToTar(tar, dir, "");
        tar.finish();
        tar.close();
        
        return true;
    }
    
    // Recursively add a file or directory to a TAR archive.
    private static void addFileToTar(TarArchiveOutputStream tar, File f, String base) throws Exception {
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tar.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            FileInputStream in = new FileInputStream(f);
            IOUtils.copy(in, tar);
            in.close();
            tar.closeArchiveEntry();
        } else {
            tar.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null){
                for (File child : children) {
                    addFileToTar(tar, child, entryName + "/");
                }
            }
        }
    }
}
