package org.datavaultplatform.common.storage.impl;

//import com.dropbox.core.*;
//import org.apache.commons.io.IOUtils;
//import org.datavaultplatform.common.io.FileCopy;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;

import java.io.*;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
//import java.util.Locale;
import java.util.Map;

// Documentation:
// https://www.dropbox.com/developers-v1/core/start/java
// https://www.dropbox.com/developers-v1/core/docs
// http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.8.x/com/dropbox/core/DbxClient.html

public class DropboxFileSystem extends Device implements UserStore {

    /*private final String dbxAppName = "DataVault/1.0";
    private final String PATH_SEPARATOR = "/";
    private static final long FILE_COPY_BUFFER_SIZE = FileCopy.ONE_KB * 64;
    
    private final DbxRequestConfig dbxConfig;
    private final DbxClient dbxClient;
    
    private String accessToken = null;*/
    
    public DropboxFileSystem(String name, Map<String,String> config) {
        super(name, config);
        
        /*// Unpack the config parameters (in an implementation-specific way)
        accessToken = config.get("accessToken");
        
        dbxConfig = new DbxRequestConfig(dbxAppName, Locale.getDefault().toString());
        dbxClient = new DbxClient(dbxConfig, accessToken);
        
        // Verify parameters are correct.
        logger.info("Connected to Dropbox account: " + dbxClient.getAccountInfo().displayName);*/
    }
    
    @Override
    public List<FileInfo> list(String path) {

        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }*/
        
        ArrayList<FileInfo> files = new ArrayList<>();

        /*try {
            DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(path);
            for (DbxEntry child : listing.children) {
                
                FileInfo info = new FileInfo(child.path,
                                             child.path,
                                             child.name,
                                             child.isFolder());
                files.add(info);
            }
        } catch (DbxException e) {
            logger.info(e.toString());
        }*/

        return files;
    }
    
    @Override
    public boolean valid(String path) {
        // Unimplemented
        return true;
    }

    @Override
    public boolean exists(String path) {

        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry != null) {
            return true;
        } else {*/
            return false;
        //}
    }
    
    @Override
    public long getSize(String path) {

        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            throw new FileNotFoundException();
        } else {*/
            long bytes = 0;
            
            /*if (entry instanceof DbxEntry.File) {
                bytes = ((DbxEntry.File)entry).numBytes;
            
            } else if (entry instanceof DbxEntry.Folder) {
                DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(path);
                for (DbxEntry child : listing.children) {
                    if (child instanceof DbxEntry.File) {
                        bytes += ((DbxEntry.File)child).numBytes;
                    } else if (child instanceof DbxEntry.Folder) {
                        bytes += getSize(child.path);
                    }
                }
            }*/
            
            return bytes;
        //}
    }
    
    public void get(Progress progress, String path, File localFile) {

        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            throw new FileNotFoundException();
        } else {
            if (entry instanceof DbxEntry.File) {
                
                // Download the contents of this file
                getFile(progress, path, localFile);
                
            } else if (entry instanceof DbxEntry.Folder) {
                
                // Create the local directory
                if (!localFile.exists()) {
                    localFile.mkdirs();
                }
                
                // Loop over the children of the folder
                DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(path);
                for (DbxEntry child : listing.children) {
                    get(progress, child.path, new File(localFile, child.name));
                }
                
                progress.dirCount += 1;
            }
        }*/
    }
    
    private void getFile(Progress progress, String path, File localFile) {
        
        /*FileOutputStream fos = new FileOutputStream(localFile);
        DbxClient.Downloader downloader = dbxClient.startGetFile(path, null);
        InputStream is = downloader.body;
        
        try {
            long size = downloader.metadata.numBytes;
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                long copied = IOUtils.copyLarge(is, fos, 0, count);
                pos += copied;
                progress.byteCount += copied;
                progress.timestamp = System.currentTimeMillis();
            }
        } finally {
            fos.close();
            is.close();
            downloader.close();
        }
        
        progress.fileCount += 1;*/
    }
    
    public void put(Progress progress, String path, Path basePath, Path localPath) {
        
        /*DirectoryStream<Path> stream = Files.newDirectoryStream(localPath);
        
        for (Path entry : stream) {
            
            File entryFile = entry.toFile();
            
            // Compute the remote path
            String entryKey = (basePath.toUri().relativize(entry.toUri())).getPath();
            String dropboxPath = path + entryKey;
            
            // The dropbox path can't end with a trailing '/'
            if (dropboxPath.endsWith(PATH_SEPARATOR)) {
                dropboxPath = dropboxPath.substring(0, dropboxPath.length()-1);
            }
            
            if (entryFile.isDirectory()) {
                dbxClient.createFolder(dropboxPath);
                put(progress, path, basePath, entry);
                progress.dirCount += 1;
            } else {
                putFile(progress, dropboxPath, entryFile);
            }
        }*/
    }
    
    private void putFile(Progress progress, String path, File localFile) {
        
        /*FileInputStream fis = new FileInputStream(localFile);
        DbxClient.Uploader uploader = dbxClient.startUploadFile(path, DbxWriteMode.add(), localFile.length());
        OutputStream os = uploader.getBody();
        
        try {
            long size = localFile.length();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                long copied = IOUtils.copyLarge(fis, os, 0, count);
                pos += copied;
                progress.byteCount += copied;
                progress.timestamp = System.currentTimeMillis();
            }
            
            uploader.finish();
            
        } finally {
            fis.close();
            uploader.close();
        }
        
        progress.fileCount += 1;*/
    }
    
    @Override
    public boolean isDirectory(String path) {
        
        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry != null) {
            if (entry instanceof DbxEntry.Folder) {
                return true;
            } else {
                return false;
            }
        } else {*/
            return false;
        //}
    }
    
    @Override
    public String getName(String path) {
        
        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            return null;
        } else {
            return entry.name;
        }*/
        return "";
    }
    
    @Override
    public long getUsableSpace() {
        
        /*DbxAccountInfo dbxAccountInfo = dbxClient.getAccountInfo();
        DbxAccountInfo.Quota quota = dbxAccountInfo.quota;
        
        // The user's used quota outside of shared folders (bytes).
        long used = quota.normal;
        
        // The user's used quota in shared folders (bytes).
        // If the user belongs to a team,
        // this includes all usage contributed to the team's quota outside
        // of the user's own used quota (bytes).
        long shared = quota.shared;
        
        // The user's total quota allocation (bytes).
        // If the user belongs to a team,
        // the team's total quota allocation (bytes)
        long total = quota.total;
        
        // TODO: is this correct?
        return total - (used + shared);*/
        return 0;
    }

    @Override
    public void retrieve(String path, File working, Progress progress) {

        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        get(progress, path, working);*/
    }

    @Override
    public String store(String path, File working, Progress progress) {

        /*// TODO: cleanup path handling - Dropbox paths MUST start with a "/"
        
        /*if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        if (!path.endsWith(PATH_SEPARATOR)) {
            path = path + PATH_SEPARATOR;
        }
        
        if (working.isDirectory()) {
            // Create top-level directory
            path += working.getName();
            dbxClient.createFolder(path);
            path += PATH_SEPARATOR;
        }
        
        put(progress, path, working.toPath(), working.toPath());
        
        return path;*/
        return "";
    }
}
