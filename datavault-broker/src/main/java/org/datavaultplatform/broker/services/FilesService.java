package org.datavaultplatform.broker.services;

import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Auth;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class FilesService {

    private Device fs;

    public void setactiveDir(String activeDir) {
        
        String name = "filesystem";
        Auth auth = null; // Local file access doesn't require credentials
        HashMap<String,String> config = new HashMap<>();
        config.put("rootPath", activeDir);
        
        try {
            fs = new LocalFileSystem(name, auth, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /*
        String name = "sftp";
        Auth auth = new Auth("username", "password", null);
        HashMap<String,String> config = new HashMap<>();
        config.put("host", "example.co.uk");
        config.put("rootPath", "/home/username/");
        
        try {
            fs = new SFTPFileSystem(name, auth, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
    
    public List<FileInfo> getFilesListing(String filePath) {
        return fs.list(filePath);
    }
    
    public boolean validPath(String filePath) {
        return fs.valid(filePath);
    }
}

