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
// http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.8.x/com/dropbox/core/DbxClient.html

public class DropBoxFileSystem extends Device {

    final String dbxAppName = "DataVault/1.0";
    private final DbxRequestConfig dbxConfig;
    private final DbxClient dbxClient;
    
    private String accessToken = null;
    
    public DropBoxFileSystem(String name, Map<String,String> config) throws IOException, DbxException  {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        accessToken = config.get("accessToken");
        
        dbxConfig = new DbxRequestConfig(dbxAppName, Locale.getDefault().toString());
        dbxClient = new DbxClient(dbxConfig, accessToken);
        
        // Verify parameters are correct.
        System.out.println("Connected to DropBox account: " + dbxClient.getAccountInfo().displayName);
    }
    
    @Override
    public List<FileInfo> list(String path) {
        
        ArrayList<FileInfo> files = new ArrayList<>();

        /*
        FileInfo info = new FileInfo(entryKey,
                                     entryAbsolutePath,
                                     entryFileName,
                                     Files.isDirectory(entry));
        files.add(info);
        */

        return files;
    }
    
    @Override
    public boolean valid(String path) {
        // Unimplemented
        return true;
    }

    @Override
    public boolean exists(String path) {
        // Unimplemented
        return true;
    }
    
    @Override
    public long getSize(String path) throws Exception {
        // Unimplemented
        return 0;
    }

    @Override
    public boolean isDirectory(String path) throws Exception {
        // Unimplemented
        return false;
    }
    
    @Override
    public String getName(String path) {
         // Unimplemented
        return "";
    }
    
    @Override
    public long getUsableSpace() throws Exception {
        // Unimplemented
        return 0;
    }

    @Override
    public void copyToWorkingSpace(String path, File working, Progress progress) throws Exception {
        
        /* Copy file or directory */
    }

    @Override
    public void copyFromWorkingSpace(String path, File working, Progress progress) throws Exception {

        /* Get file/directory properties e.g. name */
        
        /* Copy file or directory */
    }
}
