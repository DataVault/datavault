package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.io.Progress;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.dropbox.core.*;
import java.io.*;
import java.util.Locale;

// Documentation:
// https://www.dropbox.com/developers-v1/core/start/java
// https://www.dropbox.com/developers-v1/core/docs
// http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.8.x/com/dropbox/core/DbxClient.html

public class DropboxFileSystem extends Device {

    final String dbxAppName = "DataVault/1.0";
    private final String PATH_SEPARATOR = "/";
    private final DbxRequestConfig dbxConfig;
    private final DbxClient dbxClient;
    
    private String accessToken = null;
    
    public DropboxFileSystem(String name, Map<String,String> config) throws IOException, DbxException  {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        accessToken = config.get("accessToken");
        
        dbxConfig = new DbxRequestConfig(dbxAppName, Locale.getDefault().toString());
        dbxClient = new DbxClient(dbxConfig, accessToken);
        
        // Verify parameters are correct.
        System.out.println("Connected to Dropbox account: " + dbxClient.getAccountInfo().displayName);
    }
    
    @Override
    public List<FileInfo> list(String path) {
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        ArrayList<FileInfo> files = new ArrayList<>();

        try {
            DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(path);
            for (DbxEntry child : listing.children) {
                
                FileInfo info = new FileInfo(child.path,
                                             child.path,
                                             child.name,
                                             child.isFolder());
                files.add(info);
            }
        } catch (DbxException e) {
            System.out.println(e.toString());
        }

        return files;
    }
    
    @Override
    public boolean valid(String path) {
        // Unimplemented
        return true;
    }

    @Override
    public boolean exists(String path) throws Exception {

        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry != null) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public long getSize(String path) throws Exception {

        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            throw new FileNotFoundException();
        } else {
            long bytes = 0;
            
            if (entry instanceof DbxEntry.File) {
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
            }
            
            return bytes;
        }
    }
    
    public void get(String path, File localFile) throws Exception {

        System.out.println("get '" + path + "' -> '" + localFile.getPath() + "'");
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            throw new FileNotFoundException();
        } else {
            if (entry instanceof DbxEntry.File) {
                
                // Download the contents of this file
                FileOutputStream outputStream = new FileOutputStream(localFile);
                try {
                    dbxClient.getFile(path, null, outputStream);
                } finally {
                    outputStream.close();
                }
                
            } else if (entry instanceof DbxEntry.Folder) {
                
                // Create the local directory
                if (!localFile.exists()) {
                    localFile.mkdirs();
                }
                
                // Loop over the children of the folder
                DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(path);
                for (DbxEntry child : listing.children) {
                    get(child.path, new File(localFile, child.name));
                }
            }
        }
    }
    
    @Override
    public boolean isDirectory(String path) throws Exception {
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry != null) {
            if (entry instanceof DbxEntry.Folder) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    @Override
    public String getName(String path) throws Exception {
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        DbxEntry entry = dbxClient.getMetadata(path);
        if (entry == null) {
            return null;
        } else {
            return entry.name;
        }
    }
    
    @Override
    public long getUsableSpace() throws Exception {
        
        DbxAccountInfo dbxAccountInfo = dbxClient.getAccountInfo();
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
        return total - (used + shared);
    }

    @Override
    public void copyToWorkingSpace(String path, File working, Progress progress) throws Exception {
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        get(path, working);
    }

    @Override
    public void copyFromWorkingSpace(String path, File working, Progress progress) throws Exception {

        // TODO: cleanup path handling - Dropbox paths MUST start with a "/"
        
        if (!path.startsWith(PATH_SEPARATOR)) {
            path = PATH_SEPARATOR + path;
        }
        
        if (!path.endsWith(PATH_SEPARATOR)) {
            path = path + PATH_SEPARATOR;
        }
        
        // Append the archive file name (e.g. tar file)
        path = path + working.getName();
        
        // TODO: This is single-file only for now ...
        
        File inputFile = working;
        FileInputStream inputStream = new FileInputStream(inputFile);
        try {
            dbxClient.uploadFile(path, DbxWriteMode.add(), inputFile.length(), inputStream);
        } finally {
            inputStream.close();
        }
    }
}
