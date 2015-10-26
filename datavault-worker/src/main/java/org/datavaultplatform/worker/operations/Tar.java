package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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
    
    // Extract the contents of a TAR archive to a directory.
    public static File unTar(File input, Path outputDir) throws Exception {

        FileInputStream fis = new FileInputStream(input);
        BufferedInputStream bis = new BufferedInputStream(fis);
        TarArchiveInputStream tar = new TarArchiveInputStream(bis);
        
        File topDir = null;
        
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
                FileOutputStream fos = new FileOutputStream(entryFile);
                IOUtils.copyLarge(tar, fos, 0, entry.getSize());
                fos.close();
            }
        }
        
        tar.close();
        
        return topDir;
    }
}
